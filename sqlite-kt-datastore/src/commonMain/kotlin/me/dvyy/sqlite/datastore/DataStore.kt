package me.dvyy.sqlite.datastore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.statement.NamedColumnSqliteStatement
import me.dvyy.sqlite.statement.getUuid
import org.intellij.lang.annotations.Language
import kotlin.uuid.Uuid

/**
 * CRUD operations for [JsonTable].
 *
 * @property table The table to perform crud operations on.
 */
open class DataStore<T>(
    override val table: JsonTable,
    val serializer: KSerializer<T>,
    val json: Json = defaultJson,
) : DataStoreLike {
    context(tx: Transaction)
    operator fun get(
        id: Uuid,
    ): T? = tx.getOrNull("SELECT json(data) FROM $table WHERE id = ? AND data IS NOT null", id) {
        json.decodeFromString(serializer, getText(0))
    }

    context(tx: Transaction)
    fun <T> jsonGet(
        id: Uuid,
        jsonPath: String,
        statement: NamedColumnSqliteStatement.() -> T,
    ): T? = tx.getOrNull(
        "SELECT data ->> ? FROM $table WHERE id = ? AND data IS NOT null",
        jsonPath,
        id
    ) {
        statement()
    }

    context(tx: Transaction)
    fun getJsonElement(id: Uuid) =
        tx.getSingle("SELECT json(data) FROM $table WHERE id = ? AND data IS NOT null", id) {
            json.parseToJsonElement(getText(0))
        }

    context(tx: WriteTransaction)
    fun create(
        id: Uuid,
        data: T,
    ): Uuid {
        create(id, json.encodeToJsonElement(serializer, data))
        return id
    }

    context(tx: WriteTransaction)
    fun create(
        id: Uuid,
        data: JsonElement,
    ): Uuid {
        tx.exec(
            """
            INSERT OR REPLACE INTO $table (id, data) 
            SELECT :id, jsonb(:data)
            WHERE NOT EXISTS (SELECT 1 FROM $table WHERE id = :id AND data IS NOT null)
            """.trimIndent(), id, data.toString()
        )
        return id
    }

    context(tx: WriteTransaction)
    fun upsert(
        id: Uuid,
        data: T,
    ): Uuid {
        upsert(id, json.encodeToJsonElement(serializer, data))
        return id
    }

    context(tx: WriteTransaction)
    operator fun set(id: Uuid, data: T): Uuid = upsert(id, data)

    /**
     * Update data for a given [id], or create a row if it doesn't already exist.
     */
    context(tx: WriteTransaction)
    fun upsert(
        id: Uuid,
        data: JsonElement,
    ): Uuid {
        val updated = tx.select(
            """
            UPDATE $table 
            SET data = jsonb(:data)
            WHERE id = :id AND data IS NOT null
            """.trimIndent(), data.toString(), id
        ).anyChanged()

        if (!updated) tx.exec(
            """
            INSERT INTO $table (id, data) 
            VALUES (:id, jsonb(:data))
            """.trimIndent(),
            id, data.toString()
        )

        return id
    }

    context(tx: WriteTransaction)
    fun patch(
        id: Uuid,
        data: T,
    ) = patch(id, json.encodeToString(serializer, data))

    context(tx: WriteTransaction)
    fun delete(id: Uuid) {
        tx.exec("UPDATE $table SET data = null WHERE id = ?", id)
    }

    context(tx: WriteTransaction)
    fun jsonSet(id: Uuid, path: String, value: String) {
        tx.exec(
            "UPDATE $table SET data = jsonb_set(data, ?, jsonb(?)) WHERE id = ?",
            path, value, id
        )
    }

    context(ctx: WriteTransaction)
    fun update(uuid: Uuid, block: T.() -> Unit) {

    }


    context(tx: Transaction)
    operator fun contains(id: Uuid): Boolean {
        return tx.select("SELECT 1 FROM $table WHERE id = ? AND data IS NOT null LIMIT 1", id)
            .firstOrNull { true } ?: false
    }

    context(tx: WriteTransaction)
    fun patch(
        id: Uuid,
        @Language("JSON") patchString: String,
    ) {
        tx.exec(
            """
            UPDATE $table SET
            data = jsonb_patch(data, jsonb(?))
            WHERE id = ?
            """.trimIndent(),
            patchString, id,
        )
    }

    context(tx: Transaction)
    fun query(
        @Language("SQLite", prefix = "SELECT * FROM blank WHERE ") query: String,
        vararg parameters: Any,
    ): Pair<Uuid, T>? {
        return tx.select("SELECT id, json(data) FROM $table WHERE $query", *parameters)
            .firstOrNull {
                getUuid(0) to json.decodeFromString(serializer, getText(1))
            }
    }

    context(tx: Transaction)
    fun forEach(block: (Uuid, T) -> Unit) = tx
        .forEach("SELECT id, json(data) FROM $table WHERE data IS NOT null") {
            block(getUuid(0), json.decodeFromString(serializer, getText(1)))
        }

    companion object {
        val defaultJson = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }
}