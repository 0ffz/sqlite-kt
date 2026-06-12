package me.dvyy.sqlite.datastore

data class Column(
    val name: String,
    val type: SqliteDataType,
) {
    fun toStatement() = "cast(data ->> '$.$name' as $type) as $name"
}