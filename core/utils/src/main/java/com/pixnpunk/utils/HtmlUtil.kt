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
    private const val PAGE_BODY_TEMPLATE: String = "<body BODYSTYLE>REPLACETEXTSCROLLSCRIPT</body>"

    fun appendPageTemplate(
        settings: SettingsEntity,
        body: String,
        autoscroll: Boolean = false,
        restoreScrollY: Int = 0
    ): String {
        var fixBody = body.replace("\n", "<br>")

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

        val needsInitialScroll = autoscroll || restoreScrollY > 0
        val bodyStyle = if (needsInitialScroll) {
            "style=\"visibility:hidden\""
        } else {
            ""
        }
        val scrollScript = if (needsInitialScroll) buildInitialScrollScript(
            autoscroll = autoscroll,
            restoreScrollY = restoreScrollY
        ) else ""

        val pageBody = PAGE_BODY_TEMPLATE
            .replace("BODYSTYLE", bodyStyle)
            .replace("REPLACETEXT", fixBody)
            .replace("SCROLLSCRIPT", scrollScript)

        return pageHeadTemplate + pageBody
    }

    private fun buildInitialScrollScript(
        autoscroll: Boolean,
        restoreScrollY: Int
    ): String {
        val scrollExpr = when {
            autoscroll -> "window.scrollTo(0, document.documentElement.scrollHeight);"
            restoreScrollY > 0 -> "window.scrollTo(0, $restoreScrollY);"
            else -> ""
        }
        val chase = if (autoscroll) "true" else "false"
        return """
            <script>
            (function () {
              var chase = $chase;
              var revealed = false;
              var stableTimer = null;

              function applyScroll() { $scrollExpr }
              
              function reveal() {
                if (revealed) return;
                revealed = true;
                if (document.body) document.body.style.visibility = 'visible';
              }
              
              function scheduleReveal(delay) {
                if (stableTimer) clearTimeout(stableTimer);
                stableTimer = setTimeout(function () {
                  applyScroll();
                  reveal();
                }, delay);
              }

              function hookImages() {
                var imgs = document.images || [];
                for (var i = 0; i < imgs.length; i++) {
                  var img = imgs[i];
                  if (chase && img.getAttribute('loading') === 'lazy') {
                    img.setAttribute('loading', 'eager');
                  }
                  if (img.complete) continue;
                  var onDone = function () {
                    applyScroll();
                    if (chase) scheduleReveal(120);
                  };
                  img.addEventListener('load', onDone, { once: true });
                  img.addEventListener('error', onDone, { once: true });
                }
              }

              function init() {
                hookImages();
                applyScroll();

                if (chase && typeof ResizeObserver !== 'undefined' && document.documentElement) {
                  var ro = new ResizeObserver(function () {
                    applyScroll();
                    scheduleReveal(120);
                  });
                  ro.observe(document.documentElement);
                  if (document.body) ro.observe(document.body);
                }

                scheduleReveal(chase ? 250 : 60);
              }

              if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', init);
              } else {
                init();
              }
              window.addEventListener('load', function () {
                hookImages();
                applyScroll();
                scheduleReveal(120);
              });

              setTimeout(function () {
                applyScroll();
                reveal();
              }, 2000);
            })();
            </script>
        """.trimIndent()
    }

    /**
     * Bring the HTML code `html` obtained from the library to
     * HTML code acceptable for display in [android.webkit.WebView].
     */
    fun String.getCleanHtmlAndMedia(settings: SettingsEntity): String =
        Jsoup.parse(this).apply {
            outputSettings().prettyPrint(false)
            body().apply {
                handleImagesInHtml(settings)
                handleVideosInHtml(settings)
            }
        }.html()

    fun String.getCleanHtmlRemMedia(): String =
        Jsoup.parse(this).apply {
            outputSettings().prettyPrint(false)
            body().apply {
                select("img").remove()
                select("video").remove()
            }
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
