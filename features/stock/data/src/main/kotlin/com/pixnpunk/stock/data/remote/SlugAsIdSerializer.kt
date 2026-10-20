package com.pixnpunk.stock.data.remote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SlugAsIdSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SlugAsId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) =
        decoder.decodeString().substringBefore('-').toLongOrNull() ?: -1

    override fun serialize(encoder: Encoder, value: Long) =
        encoder.encodeString(value.toString())
}
