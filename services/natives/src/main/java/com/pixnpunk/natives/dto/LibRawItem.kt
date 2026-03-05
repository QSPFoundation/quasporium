package com.pixnpunk.natives.dto

import com.libqsp.jni.QSPLib
import com.libsdhqs.jni.QSLibSDH
import com.libsnxqs.jni.QSLibSNX

data class LibRawItem(val text: String, val image: String)

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