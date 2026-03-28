package com.pixnpunk.game.data.di

import com.pixnpunk.game.data.GameAudioComponent
import com.pixnpunk.game.data.NativeViewModel
import com.pixnpunk.game.domain.repository.AudioPlayer
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val gameDataModule = module {
    singleOf(::NativeViewModel)

    factoryOf(::GameAudioComponent) {
        bind<AudioPlayer>()
    }
}