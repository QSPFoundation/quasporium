package com.pixnpunk.game.data.mapper

import com.libqsp.jni.QSPLib
import com.libqsplegacy.jni.QSPLegacyLib
import com.pixnpunk.game.domain.model.LibRawItem

fun QSPLib.ListItem.toLibRawItem() = LibRawItem(
    text = name.orEmpty(),
    image = image.orEmpty()
)

fun QSPLib.ObjectItem.toLibRawItem() = LibRawItem(
    text = name.orEmpty(),
    image = image.orEmpty()
)

fun QSPLegacyLib.ListItem.toLibRawItem() = LibRawItem(
    text = text.orEmpty(),
    image = image.orEmpty()
)
