package com.pixnpunk.utils

import org.jsoup.Jsoup

object HtmlUtil {

    fun String.getSrcDir(): String =
        Jsoup.parse(this)
            .selectFirst("img")
            ?.attr("src")
            ?: ""

    fun String.removeImgTags(): String =
        Jsoup.parse(this).apply {
            select("img").remove()
        }.html()

}
