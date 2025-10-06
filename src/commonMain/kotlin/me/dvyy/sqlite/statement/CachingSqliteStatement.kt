package me.dvyy.sqlite.statement

import androidx.sqlite.SQLiteStatement

class CachingSqliteStatement(
    private val statement: SQLiteStatement,
) : NamedColumnSqliteStatement(statement) {
    override fun close() {
        reset()
        clearBindings()
    }

    fun finalizeStatement() {
        statement.close()
    }
}