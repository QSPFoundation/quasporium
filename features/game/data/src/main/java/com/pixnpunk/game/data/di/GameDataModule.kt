package com.pixnpunk.game.data.di

import com.libqsp.jni.QSPLib
import com.libsdhqs.jni.QSLibSDH
import com.pixnpunk.game.data.GameAudioComponent
import com.pixnpunk.game.data.GameNativeEngine
import com.pixnpunk.game.data.impl.QSLibSDHImpl
import com.pixnpunk.game.data.impl.QSPLibImpl
import com.pixnpunk.game.domain.repository.AudioPlayer
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val gameDataModule = module {
    factoryOf(::GameAudioComponent) { bind<AudioPlayer>() }

    factoryOf(::QSPLibImpl) { bind<QSPLib>() }
    factoryOf(::QSLibSDHImpl) { bind<QSLibSDH>() }

    factory {
        GameNativeEngine(
            context = get(),
            audioPlayer = get(),
            libByteFactory = { engineThis ->
                get<QSPLibImpl> { parametersOf(engineThis) }
            },
            libSeedhartaFactory = { engineThis ->
                get<QSLibSDHImpl> { parametersOf(engineThis) }
            }
        )
    }
}