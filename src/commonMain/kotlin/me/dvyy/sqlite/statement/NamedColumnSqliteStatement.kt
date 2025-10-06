package me.dvyy.sqlite.statement

import androidx.sqlite.SQLiteStatement

open class NamedColumnSqliteStatement(
    private val statement: SQLiteStatement,
) : SQLiteStatement by statement {
    private val columnIndices = mutableMapOf<String, Int>()

    init {
        repeat(getColumnCount()) { i ->
            columnIndices[getColumnName(i)] = i
        }
    }

    fun indexOf(column: String): Int = columnIndices.getOrDefault(column, -1)

    fun getInt(column: String): Int = getInt(indexOf(column))

    fun getLong(column: String): Long = getLong(indexOf(column))

    fun getText(column: String): String = getText(indexOf(column))

    fun getBoolean(column: String): Boolean = getBoolean(indexOf(column))

    fun getFloat(column: String): Float = getFloat(indexOf(column))

    fun getDouble(column: String): Double = getDouble(indexOf(column))

    fun getBlob(column: String): ByteArray = getBlob(indexOf(column))
}
