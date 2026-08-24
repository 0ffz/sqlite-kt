package me.dvyy.sqlite.datastore

import me.dvyy.sqlite.WriteTransaction

enum class SqliteDataType {
    NULL, INTEGER, REAL, TEXT, BLOB
}

open class KeyedJsonTable(
    name: String,
    indexes: List<TableIndex>,
) : JsonTable(name, indexes) {
    context(tx: WriteTransaction)
    override fun create() {
        tx.exec(
            """
            CREATE TABLE IF NOT EXISTS $name (
                id BLOB,
                key BLOB,
                data BLOB CHECK (json_valid(data, 4)),
                json_key TEXT GENERATED ALWAYS AS (json(key)) VIRTUAL,
                json_data TEXT GENERATED ALWAYS AS (json(data)) VIRTUAL,
                PRIMARY KEY (id, key)
            ) STRICT;
            """.trimIndent()
        )
        createIndexes(name, indexes)
    }

}

open class JsonTable(
    val name: String,
    val indexes: List<TableIndex>,
) {
    init {
        // Ensure indexes have unique names
        val names = indexes.map { it.nameSuffix }
        require(names == names.distinct())
    }


    context(tx: WriteTransaction)
    open fun create() {
        tx.exec(
            """
            CREATE TABLE IF NOT EXISTS $name (
                id BLOB PRIMARY KEY,
                data BLOB CHECK (json_valid(data, 4)),
                json_data TEXT GENERATED ALWAYS AS (json(data)) VIRTUAL
            ) STRICT;
            """.trimIndent()
        )
        createIndexes(name, indexes)
    }

    override fun toString(): String {
        return name
    }

    companion object {

        context(tx: WriteTransaction)
        fun createIndexes(name: String, indexes: List<TableIndex>) {
            fun getName(index: TableIndex) = "auto_${name}_${index.nameSuffix}"

            val existingIndexes =
                tx.select("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name=?", name)
                    .map { getText(0) }
                    .filter { it.startsWith("auto_${name}_") }
                    .toSet()
            val existingStatements = existingIndexes.associateWith { indexName ->
                tx.select("SELECT sql FROM sqlite_master WHERE type='index' AND name=?", indexName)
                    .map { getText(0) }
                    .firstOrNull() ?: ""
            }
            val newIndexes = indexes
                .associate { getName(it) to it.createStatement(tableName = name) }
                .toMutableMap()

            existingStatements.forEach { (name, statement) ->
                if (newIndexes[name] != statement) tx.exec("DROP INDEX $name")
                else newIndexes.remove(name)
            }
            newIndexes.forEach { (_, statement) -> tx.exec(statement) }
        }
    }
}
