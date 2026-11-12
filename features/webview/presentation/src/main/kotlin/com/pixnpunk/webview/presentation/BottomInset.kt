package com.pixnpunk.webview.presentation

private const val BOTTOM_INSET_PROPERTY = "--qsp-bottom-inset"

internal fun String.withBottomInset(insetPx: Int): String =
    if (insetPx <= 0) this
    else replaceFirst("</head>", "<style>:root{$BOTTOM_INSET_PROPERTY:${insetPx}px}</style></head>")

internal fun bottomInsetScript(insetPx: Int): String =
    "document.documentElement.style.setProperty('$BOTTOM_INSET_PROPERTY', '${insetPx.coerceAtLeast(0)}px');"
