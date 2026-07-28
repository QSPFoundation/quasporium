package com.pixnpunk.game.data.di

import com.libqsp.jni.QSPLib
import com.libqsplegacy.jni.QSPLegacyLib
import com.pixnpunk.game.data.GameAudioComponent
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.game.data.impl.QSPLegacyLibImpl
import com.pixnpunk.game.data.impl.QSPLibImpl
import com.pixnpunk.game.domain.repository.AudioPlayer
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.onClose

val gameDataModule = module {
    singleOf(::GameAudioComponent) { bind<AudioPlayer>() } onClose { it?.close() }
    singleOf(::QSPLibImpl) { bind<QSPLib>() }
    singleOf(::QSPLegacyLibImpl) { bind<QSPLegacyLib>() }

    factory {
        GameNativeEngine(
            context = get(),
            audioPlayer = get(),
            libNewFactory = { engineThis ->
                get<QSPLibImpl> { parametersOf(engineThis) }
            },
            libOldFactory = { engineThis ->
                get<QSPLegacyLibImpl> { parametersOf(engineThis) }
            },
            settingsRepository = get()
        )
    }
}
