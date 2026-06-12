package me.dvyy.sqlite.datastore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun LongArray.toUuid() = Uuid.fromLongs(this[0], this[1])

@OptIn(ExperimentalUuidApi::class)
object UuidSerializer : KSerializer<Uuid> {
    val surrogate = LongArraySerializer()
    override val descriptor: SerialDescriptor = surrogate.descriptor

    override fun deserialize(decoder: Decoder): Uuid {
        val array = decoder.decodeSerializableValue(surrogate)
        require(array.size == 2) { "UUID expected a long array of size 2, but found ${array.size}" }
        return array.toUuid()
    }

    override fun serialize(encoder: Encoder, value: Uuid) {
        encoder.encodeSerializableValue(surrogate, value.toLongs { t, b -> longArrayOf(t, b) })
    }
}

object JsonElementAsStringSerializer : KSerializer<JsonElement> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("json", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): JsonElement {
        return Json.parseToJsonElement(decoder.decodeString())
    }

    override fun serialize(
        encoder: Encoder,
        value: JsonElement,
    ) {
        encoder.encodeString(value.toString())
    }
}

object JsonElementAsCborSerializer : KSerializer<JsonElement> {
    override val descriptor: SerialDescriptor = ByteArraySerializer().descriptor
    override fun deserialize(decoder: Decoder): JsonElement {
        return Cbor.decodeFromByteArray(
            JsonElement.serializer(),
            decoder.decodeSerializableValue(ByteArraySerializer())
        )
    }

    override fun serialize(encoder: Encoder, value: JsonElement) {
        encoder.encodeSerializableValue(ByteArraySerializer(), Cbor.encodeToByteArray(value))
    }
}
