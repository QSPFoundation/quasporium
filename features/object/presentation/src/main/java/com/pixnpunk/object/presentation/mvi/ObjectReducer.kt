package com.pixnpunk.`object`.presentation.mvi

import com.arkivanov.mvikotlin.core.store.Reducer

object ObjectReducer : Reducer<ObjectStore.State, ObjectStore.Message> {
    override fun ObjectStore.State.reduce(msg: ObjectStore.Message): ObjectStore.State {
        return when (msg) {
            is ObjectStore.Message.UpdateObjects -> copy(objects = msg.objects)
        }
    }
}
