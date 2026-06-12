package me.dvyy.sqlite.datastore

import me.dvyy.sqlite.WriteTransaction
import org.intellij.lang.annotations.Language


class ViewBuilder(
    val from: JsonTable,
    val name: String,
    val where: String? = null,
) {
    val columns = mutableListOf<Column>()

    fun text(name: String) {
        columns += Column(name, SqliteDataType.TEXT)
    }

    fun integer(name: String) {
        columns += Column(name, SqliteDataType.INTEGER)
    }

    fun real(name: String) {
        columns += Column(name, SqliteDataType.REAL)
    }

    fun blob(name: String) {
        columns += Column(name, SqliteDataType.BLOB)
    }

    fun build() = JsonView(name, from, columns, where)
}

class JsonView(
    val name: String,
    val from: JsonTable,
    val columns: List<Column>,
    val where: String? = null,
) {
    fun viewStatement(from: String) = """
        SELECT
        id,
        ${columns.joinToString(",\n") { it.toStatement() }}
        FROM $from
        WHERE data != jsonb('null') ${if (where != null) "AND $where" else ""}
    """.trimIndent()

    context(tx: WriteTransaction)
    fun create() {
        tx.exec("CREATE VIEW IF NOT EXISTS $name AS ${viewStatement(from.name)}")
    }

    override fun toString(): String = name
}

data class TableIndex(
    val nameSuffix: String,
    val unique: Boolean,
    val index: String,
) {
    fun createStatement(tableName: String): String {
        return "CREATE ${if (unique) "UNIQUE " else ""}INDEX auto_${tableName}_$nameSuffix ON $tableName($index)"
    }
}

class JsonTableBuilder {
    val indexes = mutableListOf<TableIndex>()
    fun index(
        name: String,
        @Language(
            "SQLite",
            prefix = "CREATE INDEX example ON example(",
            suffix = ")"
        ) index: String,
        unique: Boolean = false,
    ) {
        indexes.add(TableIndex(name, unique, index))
    }

    fun build(): List<TableIndex> = indexes.toList()
}

fun jsonTable(
    name: String,
    indexes: JsonTableBuilder.() -> Unit = {},
): JsonTable {
    return JsonTable(name, JsonTableBuilder().apply(indexes).build())
}

fun keyedJsonTable(
    name: String,
    indexes: JsonTableBuilder.() -> Unit = {},
): KeyedJsonTable {
    return KeyedJsonTable(name, JsonTableBuilder().apply(indexes).build())
}

fun view(
    name: String,
    table: JsonTable,
    @Language("SQLite")
    where: String? = null,
    block: ViewBuilder.() -> Unit,
): JsonView {
    return ViewBuilder(table, name, where).apply(block).build()
}
