package com.pixnpunk.game.data.di

import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.game.data.audio.AudioPlayerInteractorImpl
import com.pixnpunk.game.data.engine.GameEngineInteractorImpl
import com.pixnpunk.game.data.engine.jni.NativeEngineBridge
import com.pixnpunk.game.data.engine.jni.QspEngineBridgeImpl
import com.pixnpunk.game.data.engine.jni.QspLegacyEngineBridgeImpl
import com.pixnpunk.game.data.save.SaveGameInteractorImpl
import com.pixnpunk.game.data.vfs.FileSystemProvider
import com.pixnpunk.game.data.vfs.impl.FileSystemProviderImpl
import com.pixnpunk.game.domain.repository.AudioPlayerInteractor
import com.pixnpunk.game.domain.repository.GameEngineInteractor
import com.pixnpunk.game.domain.repository.SaveGameInteractor
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

val gameDataModule = module {
    singleOf(::AudioPlayerInteractorImpl) {
        bind<AudioPlayerInteractor>()
    } onClose { it?.close() }

    single<NativeEngineBridge>(
        named(NativeLibAuthors.BYTE)
    ) { QspEngineBridgeImpl() }
    single<NativeEngineBridge>(
        named(NativeLibAuthors.SEEDHARTHA)
    ) { QspLegacyEngineBridgeImpl(context = get()) }
    factory<GameEngineInteractor> { (author: NativeLibAuthors) ->
        GameEngineInteractorImpl(
            bridgeProvider = inject(named(author)),
            vfsProvider = get(),
            audioService = get(),
            logger = get()
        )
    }

    singleOf(::FileSystemProviderImpl) {
        bind<FileSystemProvider>()
    }

    factory<SaveGameInteractor> { (engine: GameEngineInteractor) ->
        SaveGameInteractorImpl(
            context = get(),
            vfsProvider = get(),
            saveDao = get(),
            engineInteractor = engine
        )
    }
}
