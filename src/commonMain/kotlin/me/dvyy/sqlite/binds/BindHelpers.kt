package me.dvyy.sqlite.binds

import androidx.sqlite.SQLiteStatement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun SQLiteStatement.bindUuid(index: Int, uuid: Uuid) = bindBlob(index, uuid.toByteArray())

@OptIn(ExperimentalUuidApi::class)
fun SQLiteStatement.getUuid(index: Int) = Uuid.fromByteArray(getBlob(index))

@OptIn(ExperimentalUuidApi::class)
fun SQLiteStatement.bindAny(index: Int, value: Any) = when (value) {
    is Int -> bindInt(index, value)
    is Long -> bindLong(index, value)
    is String -> bindText(index, value)
    is Boolean -> bindBoolean(index, value)
    is Float -> bindFloat(index, value)
    is Double -> bindDouble(index, value)
    is ByteArray -> bindBlob(index, value)
    is Uuid -> bindUuid(index, value)
    else -> error("Could not bind parameter of type ${value::class.simpleName}")
}
