package me.dvyy.sqlite.datastore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import kotlin.uuid.Uuid

open class KeyedDataStore<K, T>(
    override val table: KeyedJsonTable,
    val keySerializer: KSerializer<K>,
    val serializer: KSerializer<T>,
    val json: Json = DataStore.defaultJson,
) : DataStoreLike {
    fun encodeKey(key: K): String = json.encodeToString(keySerializer, key)

    val tableName get() = table.name

    context(tx: Transaction)
    operator fun get(
        id: Uuid,
        key: K,
    ): T? {
        val encodedKey = encodeKey(key)
        return tx.getOrNull(
            "SELECT json(data) FROM $table WHERE id = ? AND key = jsonb(?) AND data IS NOT null",
            id,
            encodedKey
        ) {
            json.decodeFromString(serializer, getText(0))
        }
    }

    /**
     * Update data for a given [id], or create a row if it doesn't already exist.
     */
    context(tx: WriteTransaction)
    fun upsert(
        id: Uuid,
        key: K,
        data: JsonElement,
    ): Uuid {
        val encodedKey = encodeKey(key)
        val updated = tx.select(
            """
            UPDATE $table 
            SET data = jsonb(:data)
            WHERE id = :id AND key = jsonb(:key) AND data IS NOT null
            """.trimIndent(), data.toString(), id, encodedKey
        ).anyChanged()

        if (!updated) tx.exec(
            """
            INSERT INTO $table (id, key, data) 
            VALUES (:id, jsonb(:key), jsonb(:data))
            """.trimIndent(),
            id, encodedKey, data.toString()
        )

        return id
    }

    context(tx: WriteTransaction)
    operator fun set(
        id: Uuid,
        key: K,
        data: T,
    ): Uuid {
        upsert(id, key, json.encodeToJsonElement(serializer, data))
        return id
    }

    context(tx: Transaction)
    fun contains(id: Uuid, key: K): Boolean {
        val encodedKey = encodeKey(key)
        return tx.select(
            "SELECT 1 FROM $table WHERE id = ? AND key = jsonb(?) AND data IS NOT null LIMIT 1",
            id,
            encodedKey
        ).firstOrNull { true } ?: false
    }
}