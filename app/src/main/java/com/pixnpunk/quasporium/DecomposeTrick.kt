package com.pixnpunk.quasporium

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.connectOnBackPressedCallback
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.essenty.statekeeper.getSerializableContainer
import com.arkivanov.essenty.statekeeper.putSerializableContainer

@Composable
fun <T> rememberRetainedComponent(
    key: String = "ComposableComponent",
    factory: (ComponentContext) -> T
): T {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val holder =
        viewModel(key = key) {
            val handle = createSavedStateHandle()
            val ctx =
                RetainedComponentContext(handle.get<Bundle>(key)?.getSerializableContainer(key))
            handle.setSavedStateProvider(key) {
                Bundle().apply { putSerializableContainer(key, ctx.stateKeeper.save()) }
            }
            Holder(factory(ctx), ctx)
        }

    DisposableEffect(lifecycle) {
        val observer = LifecycleAdapter(holder.componentContext.lifecycle)
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    if (onBackPressedDispatcher != null) {
        DisposableEffect(lifecycleOwner, onBackPressedDispatcher) {
            val onBackPressedCallback = holder.componentContext.onBackPressedCallback
            onBackPressedDispatcher.addCallback(lifecycleOwner, onBackPressedCallback)
            onDispose(onBackPressedCallback::remove)
        }
    }

    return holder.instance
}

private class LifecycleAdapter(
    private val lifecycle: LifecycleRegistry
) : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
        lifecycle.create()
    }

    override fun onStart(owner: LifecycleOwner) {
        lifecycle.start()
    }

    override fun onResume(owner: LifecycleOwner) {
        lifecycle.resume()
    }

    override fun onPause(owner: LifecycleOwner) {
        lifecycle.pause()
    }

    override fun onStop(owner: LifecycleOwner) {
        lifecycle.stop()
    }
}

private class RetainedComponentContext(savedState: SerializableContainer?) : ComponentContext {
    override val lifecycle: LifecycleRegistry = LifecycleRegistry()
    override val stateKeeper: StateKeeperDispatcher = StateKeeperDispatcher(savedState)
    override val instanceKeeper: InstanceKeeperDispatcher = InstanceKeeperDispatcher()
    override val backHandler: BackDispatcher = BackDispatcher()
    val onBackPressedCallback: OnBackPressedCallback = backHandler.connectOnBackPressedCallback()

    override val componentContextFactory: ComponentContextFactory<ComponentContext> =
        ComponentContextFactory(::DefaultComponentContext)
}

private class Holder<out T>(
    val instance: T,
    val componentContext: RetainedComponentContext,
) : ViewModel(componentContext.lifecycle::destroy, componentContext.instanceKeeper::destroy)