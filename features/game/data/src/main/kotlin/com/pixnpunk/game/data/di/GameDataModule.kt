package com.pixnpunk.game.data.di

import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.game.data.GameAudioComponent
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
    singleOf(::GameAudioComponent) {
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
            bridge = get(named(author)),
            vfsProvider = get(),
            audioService = get()
        )
    }

    singleOf(::FileSystemProviderImpl) {
        bind<FileSystemProvider>()
    }

    factory<SaveGameInteractor> { (engine: GameEngineInteractor) ->
        SaveGameInteractorImpl(
            vfsProvider = get(),
            saveSlotDao = get(),
            engineInteractor = engine
        )
    }
}
