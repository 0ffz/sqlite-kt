package me.dvyy.sqlite.datastore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Given a serializable [component], converts it to json, applies [jsonMerge] and converts back
 * to the serializable object type.
 */
fun <T> jsonMerge(serializer: KSerializer<T>, component: T?, jsonDiff: JsonElement): T {
    if (component == null) return Json.decodeFromJsonElement(serializer, jsonDiff)
    val base = Json.encodeToJsonElement(serializer, component)
    return Json.decodeFromJsonElement(serializer, base.plus(jsonDiff))
}

fun <T> jsonSubtract(serializer: KSerializer<T>, new: T, old: T): JsonElement {
    return Json.encodeToJsonElement(serializer, new) - Json.encodeToJsonElement(serializer, old)
}

/**
 * Merges two json elements, with values on [other] overriding existing values as needed
 */
operator fun JsonElement.plus(other: JsonElement): JsonElement = when {
    this is JsonObject && other is JsonObject -> {
        JsonObject(this.toMutableMap().apply {
            other.forEach { (key, value) ->
                this[key]?.let { existing ->
                    this[key] = existing.plus(value)
                } ?: run {
                    this[key] = value
                }
            }
        })
    }

//    this is JsonArray && other is JsonArray -> {
//        JsonArray(this.toMutableList().apply {
//            addAll(other)
//        })
//    }

    else -> other
}

/**
 * Returns a diff element that contains only keys that are different between [this] and [other], i.e. a diff element.
 */
operator fun JsonElement.minus(other: JsonElement): JsonElement = when {
    this is JsonObject && other is JsonObject -> {
        JsonObject(buildMap {
            this@minus.forEach { (key, value) ->
                other[key]?.let { otherValue ->
                    if (value != otherValue) {
                        this[key] = value.minus(otherValue)
                    }
                } ?: run {
                    this[key] = value
                }
            }
        })
    }

    this != other -> this
    else -> JsonObject(emptyMap())
}
