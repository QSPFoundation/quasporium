package com.pixnpunk.webview

import android.content.Context
import android.util.Base64
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.utils.Base64Util.encodeBase64
import com.pixnpunk.utils.ColorUtil.toHexColorStr
import com.pixnpunk.webview.model.HtmlConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.util.regex.Pattern

object HtmlEngine : KoinComponent {
    private val execLinkMatcher: Regex =
        Regex("href=\"exec:([\\s\\S]*?)\"", RegexOption.IGNORE_CASE)
    private val HTML_PATTERN: Pattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>")

    private val template by lazy {
        get<Context>().assets.open("webview-template.html").bufferedReader().use { it.readText() }
    }

    fun appendPageTemplate(
        config: HtmlConfig,
        body: String,
        autoscroll: Boolean = false,
        restoreScrollY: Int = 0
    ): String {
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
                        match.groupValues[2]
                            .replace("\\", "/")
                            .encodeBase64(Base64.NO_WRAP)
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

        return template
            .replace("QSPTEXTCOLOR", config.textColor.toHexColorStr())
            .replace("QSPBACKCOLOR", config.backColor.toHexColorStr())
            .replace("QSPLINKCOLOR", config.linkColor.toHexColorStr())
            .replace("QSPCUSTOMFONTFACE", customFontFaceRule)
            .replace("QSPFONTSTYLE", cssFontFamily)
            .replace("QSPFONTSIZE", config.fontSize.toString())
            .replace("BODYSTYLE", bodyStyle)
            .replace("REPLACETEXT", fixBody)
            .replace("SCROLLSCRIPT", scrollScript)
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

    fun String.isContainsHtmlTags(): Boolean =
        HTML_PATTERN.matcher(this).find()

    fun String.getCleanHtmlAndMedia(
        config: HtmlConfig
    ): String =
        Jsoup.parse(this).apply {
            outputSettings().prettyPrint(false)
            body().apply {
                handleImagesInHtml(config)
                handleVideosInHtml(config)
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