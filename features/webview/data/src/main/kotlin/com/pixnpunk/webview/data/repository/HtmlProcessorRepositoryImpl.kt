package com.pixnpunk.webview.data.repository

import android.content.Context
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.utils.Base64Util.encodeBase64
import com.pixnpunk.utils.ColorUtil.toHexColorStr
import com.pixnpunk.webview.domain.model.HtmlConfig
import com.pixnpunk.webview.domain.repository.HtmlProcessorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import kotlin.io.encoding.Base64

class HtmlProcessorRepositoryImpl(
    private val context: Context
) : HtmlProcessorRepository {

    private val execLinkMatcher: Regex =
        Regex(
            pattern = "href\\s*=\\s*([\"'])exec:(.*?)\\1",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
    private val template by lazy {
        context.assets.open("webview-template.html").bufferedReader().use { it.readText() }
    }

    override suspend fun formatGameHtml(
        rawBody: String,
        config: HtmlConfig
    ): String = withContext(Dispatchers.IO) {
        var preCleanedBody = rawBody.replace("\n", "<br>")

        if (config.isUseHtml) {
            if (preCleanedBody.contains("\\\"")) {
                preCleanedBody = preCleanedBody.replace(
                    "\\\"", "'"
                )
            }
            preCleanedBody = preCleanedBody.encodeExecLinks()
        }

        val document = Jsoup.parse(
            applyTemplate(
                bodyContent = preCleanedBody,
                config = config
            )
        ).apply { outputSettings().prettyPrint(false) }

        if (config.isImgLoadDis) {
            document.body().select("img, video").remove()
        } else {
            document.body().apply {
                handleImagesInHtml(config)
                handleVideosInHtml(config)
            }
        }

        document.html()
    }

    private fun applyTemplate(
        bodyContent: String,
        config: HtmlConfig
    ): String {
        var customFontFaceRule = ""

        var cssFontFamily = when (config.typeface) {
            FontTypeface.SANS_SERIF -> "sans-serif"
            FontTypeface.SERIF -> "serif"
            FontTypeface.MONOSPACE -> "monospace"
            else -> "sans-serif"
        }

        if (config.isUseCustomFont && config.customFontPath.isNotEmpty()) {
            val fontFile = File(config.customFontPath).name
            customFontFaceRule = """
                @font-face {
                    font-family: 'AppCustomFont';
                    src: url('https://appassets.androidplatform.net/fonts/$fontFile');
                }
            """.trimIndent()
            cssFontFamily = "'AppCustomFont', sans-serif"
        }

        return template
            .replace("QSPTEXTCOLOR", config.textColor.toHexColorStr())
            .replace("QSPBACKCOLOR", config.backColor.toHexColorStr())
            .replace("QSPLINKCOLOR", config.linkColor.toHexColorStr())
            .replace("QSPFONTSIZE", config.fontSize.toString())
            .replace("QSPCUSTOMFONTFACE", customFontFaceRule)
            .replace("QSPFONTSTYLE", cssFontFamily)
            .replace("REPLACETEXT", bodyContent)
    }

    private fun String.encodeExecLinks(): String {
        if (!this.contains("exec:", ignoreCase = true)) return this
        return execLinkMatcher.replace(this) { match ->
            val quote = match.groupValues[1]
            val command = match.groupValues[2]

            "href=${quote}exec:${
                command
                    .replace('\\', '/')
                    .encodeBase64()
            }${quote}"
        }
    }

    private fun Element.handleImagesInHtml(
        config: HtmlConfig
    ) = select("img").forEach { img ->
        img.attr("loading", "lazy")

        if (config.isUseFullscreenImages) {
            val parent = img.parent()
            val isInsideLink = parent != null && parent.tagName() == "a"
            if (!isInsideLink && !img.hasAttr("onclick")) {
                img.attr("onclick", "img.onClickImage(this.src);")
            }
        }

        val styleBuilder = StringBuilder()
        val oldStyle = img.attr("style")
        if (oldStyle.isNotBlank()) {
            styleBuilder.append(oldStyle)
            if (!oldStyle.trim().endsWith(";")) styleBuilder.append(";")
        }

        if (config.isAutoWidthImage && config.isAutoHeightImage) {
            styleBuilder.append(" display: inline; height: auto; max-width: 100%;")
        } else {
            if (!config.isAutoWidthImage) {
                styleBuilder.append(" max-width: ${config.imageWidth}px;")
            }
            if (!config.isAutoHeightImage) {
                styleBuilder.append(" max-height: ${config.imageHeight}px;")
            }
        }

        img.attr("style", styleBuilder.toString())
    }

    private fun Element.handleVideosInHtml(
        config: HtmlConfig
    ) = select("video").forEach { video ->
        val oldStyle = video.attr("style")
        if (!oldStyle.contains("max-width")) {
            video.attr("style", "$oldStyle max-width: 100%; height: auto;")
        }

        video.attr("loop", "")
        video.attr("playsinline", "")
        video.attr("webkit-playsinline", "")

        if (!config.isVideoMute) {
            video.removeAttr("muted")
            video.attr("controls", "")
        } else {
            video.attr("autoplay", "")
            video.attr("muted", "")
            video.removeAttr("controls")
        }
    }
}
