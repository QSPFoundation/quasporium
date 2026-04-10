package com.pixnpunk.utils

import android.util.Base64
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.utils.Base64Util.encodeBase64
import com.pixnpunk.utils.ColorUtil.toHexColorStr
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import java.util.regex.Pattern

object HtmlUtil {
    private val EXEC_PATTERN: Pattern =
        Pattern.compile("href=\"exec:([\\s\\S]*?)\"", Pattern.CASE_INSENSITIVE)
    private val HTML_PATTERN: Pattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>")

    private const val PAGE_HEAD_TEMPLATE: String = """
            <!DOCTYPE html>
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <style type="text/css">
                body {
                  margin: 0;
                  padding: 0.5em;
                  color: QSPTEXTCOLOR;
                  background-color: QSPBACKCOLOR;
                  font-size: QSPFONTSIZE;
                  font-family: QSPFONTSTYLE;
                  overflow-wrap: break-word;
                  word-wrap: break-word;
                }
        
                a { color: QSPLINKCOLOR; }
                a:link { color: QSPLINKCOLOR; }
                
                img {
                  max-width: 100%;
                  height: auto;
                  content-visibility: auto;
                }
        
                video, iframe {
                    max-width: 100%;
                }
        
                table {
                    max-width: 100%;
                }
                
                ::-webkit-scrollbar {
                    width: 6px;
                    height: 6px;
                }
                ::-webkit-scrollbar-track {
                    background: transparent;
                }
                ::-webkit-scrollbar-thumb {
                   background: rgba(128, 128, 128, 0.4);
                   border-radius: 3px;
                }
            </style>
            </head>
        """
    private const val PAGE_BODY_TEMPLATE: String = "<body>REPLACETEXT</body>"

    fun appendPageTemplate(settings: SettingsEntity, body: String): String {
        var fixBody = body.replaceIndent("<br>")

        if (settings.isUseHtml) {
            if (fixBody.contains("\\\"")) {
                fixBody = fixBody.replace("\\\"", "'")
            }

            if (EXEC_PATTERN.matcher(fixBody).find()) {
                fixBody = fixBody.encodeExec()
            }
        }

        val pageHeadTemplate = PAGE_HEAD_TEMPLATE
            .replace("QSPTEXTCOLOR", settings.textColor.toHexColorStr())
            .replace("QSPBACKCOLOR", settings.backColor.toHexColorStr())
            .replace("QSPLINKCOLOR", settings.linkColor.toHexColorStr())
            .replace("QSPFONTSTYLE", settings.typeface.name.lowercase())
            .replace("QSPFONTSIZE", settings.fontSize.toString())

        return pageHeadTemplate + PAGE_BODY_TEMPLATE.replace("REPLACETEXT", fixBody)
    }

    /**
     * Bring the HTML code `html` obtained from the library to
     * HTML code acceptable for display in [android.webkit.WebView].
     */
    fun String.getCleanHtmlAndMedia(settings: SettingsEntity): String =
        Jsoup.parse(this).apply {
            body().handleImagesInHtml(settings)
            body().handleVideosInHtml(settings)
        }.html()

    fun String.getCleanHtmlRemMedia(): String =
        Jsoup.parse(this).apply {
            body().select("img").remove()
            body().select("video").remove()
        }.html()

    fun String.getSrcDir(): String =
        Jsoup.parse(this)
            .select("img")
            .first()
            ?.attr("src")
            ?: ""

    fun String.isContainsHtmlTags(): Boolean =
        HTML_PATTERN.matcher(this).find()

    fun String.removeImgTags(): String =
        Jsoup.parse(this).apply {
            select("img").remove()
        }.html()

    /**
     * Remove HTML tags from the `html` string and return the resulting string.
     */
    fun String.removeHtmlTags(): String {
        val result = StringBuilder()
        var fromIdx = 0

        while (fromIdx < this.length) {
            val idx = this.indexOf('<', fromIdx)
            if (idx == -1) {
                result.append(this.substring(fromIdx))
                break
            }
            result.append(this, fromIdx, idx)
            val endIdx = this.indexOf('>', idx + 1)
            if (endIdx == -1) {
                return Jsoup.clean(this, Safelist.none())
            }
            fromIdx = endIdx + 1
        }

        return result.toString()
    }

    private fun String.encodeExec(): String {
        val matcher = EXEC_PATTERN.matcher(this)
        val buffer = StringBuffer()

        while (matcher.find()) {
            val path = matcher.group(1) ?: continue
            val encodedExec = path.normalizePathsInExec().encodeBase64(Base64.NO_WRAP)
            matcher.appendReplacement(buffer, "href=\"exec:$encodedExec\"")
        }

        matcher.appendTail(buffer)
        return buffer.toString()
    }

    private fun String.normalizePathsInExec(): String {
        return this.replace("\\", "/")
    }

    private fun Element.handleImagesInHtml(settings: SettingsEntity) {
        this.select("img").forEach { img ->
            img.attr("loading", "lazy")

            if (settings.isUseFullscreenImages) {
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

            if (settings.isAutoWidthImage && settings.isAutoHeightImage) {
                styleBuilder.append(" display: inline; height: auto; max-width: 100%;")
            } else {
                if (!settings.isAutoWidthImage) {
                    styleBuilder.append(" max-width: ${settings.imageWidth}px;")
                }
                if (!settings.isAutoHeightImage) {
                    styleBuilder.append(" max-height: ${settings.imageHeight}px;")
                }
            }

            img.attr("style", styleBuilder.toString())
        }
    }

    private fun Element.handleVideosInHtml(settings: SettingsEntity) {
        this.select("video").forEach { video ->
            val oldStyle = video.attr("style")
            if (!oldStyle.contains("max-width")) {
                video.attr("style", "$oldStyle max-width: 100%; height: auto;")
            }

            video.attr("loop", "")
            video.attr("playsinline", "")
            video.attr("webkit-playsinline", "")

            if (!settings.isVideoMute) {
                video.removeAttr("muted")
                video.attr("controls", "")
            } else {
                video.attr("autoplay", "")
                video.attr("muted", "")
                video.removeAttr("controls")
            }
        }
    }
}
