package com.pixnpunk.webview.di

import com.pixnpunk.webview.data.di.webViewDataModule
import com.pixnpunk.webview.domain.di.webViewDomainModule

val featureWebViewModule = listOf(
    webViewDataModule,
    webViewDomainModule
)
