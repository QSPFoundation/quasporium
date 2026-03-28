package com.pixnpunk.game.data.mapper

import com.libqsp.jni.QSPLib
import com.libsdhqs.jni.QSLibSDH
import com.libsnxqs.jni.QSLibSNX
import com.pixnpunk.game.domain.model.LibRawItem

fun QSPLib.ListItem.toLibRawItem() = LibRawItem(
    text = name.orEmpty(),
    image = image.orEmpty()
)

fun QSPLib.ObjectItem.toLibRawItem() = LibRawItem(
    text = name.orEmpty(),
    image = image.orEmpty()
)

fun QSLibSNX.ListItem.toLibRawItem() = LibRawItem(
    text = text.orEmpty(),
    image = image.orEmpty()
)

fun QSLibSDH.ListItem.toLibRawItem() = LibRawItem(
    text = text.orEmpty(),
    image = image.orEmpty()
)