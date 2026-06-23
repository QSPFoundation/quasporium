package com.pixnpunk.webview.data.repository

import android.content.Context
import android.util.Base64
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.utils.ColorUtil.toHexColorStr
import com.pixnpunk.webview.domain.model.HtmlConfig
import com.pixnpunk.webview.domain.repository.HtmlProcessorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File

class HtmlProcessorRepositoryImpl(
    private val context: Context
) : HtmlProcessorRepository {
    private val execLinkMatcher: Regex =
        Regex("href=\"exec:([\\s\\S]*?)\"", RegexOption.IGNORE_CASE)

    private val template by lazy {
        context.assets.open("webview-template.html").bufferedReader().use { it.readText() }
    }

    override suspend fun formatGameHtml(
        rawBody: String,
        config: HtmlConfig
    ): String = withContext(Dispatchers.IO) {
        val document = Jsoup.parse(
            applyTemplate(rawBody, config)
        ).apply {
            outputSettings().prettyPrint(false)
        }

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

    private fun applyTemplate(body: String, config: HtmlConfig): String {
        var fixBody = body.replace("\n", "<br>")
        var customFontFaceRule = ""

        var cssFontFamily = when (config.typeface) {
            FontTypeface.SANS_SERIF -> "sans-serif"
            FontTypeface.SERIF -> "serif"
            FontTypeface.MONOSPACE -> "monospace"
            else -> "sans-serif"
        }

        if (config.isUseHtml) {
            if (fixBody.contains("\\\"")) {
                fixBody = fixBody.replace("\\\"", "'")
            }

            if (execLinkMatcher.containsMatchIn(fixBody)) {
                fixBody = execLinkMatcher.replace(fixBody) { match ->
                    """href="exec:${
                        Base64.encodeToString(
                            match.groupValues[2].replace("\\", "/").encodeToByteArray(),
                            Base64.NO_WRAP
                        )
                    }"""
                }
            }
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
            .replace("REPLACETEXT", fixBody)
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