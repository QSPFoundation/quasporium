package com.pixnpunk.game.presentation.di

import com.pixnpunk.game.data.di.gameDataModule
import com.pixnpunk.game.domain.di.gameDomainModule

val featureGameModule = listOf(
    gameDataModule,
    gameDomainModule
)