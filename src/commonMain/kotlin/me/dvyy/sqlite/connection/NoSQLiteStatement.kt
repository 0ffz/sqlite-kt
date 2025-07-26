package me.dvyy.sqlite.connection

import androidx.sqlite.SQLiteStatement

class NoSQLiteStatementAccessException: IllegalStateException("Tried to use a NoSQLiteStatement")

object NoSQLiteStatement: SQLiteStatement {
    override fun bindBlob(index: Int, value: ByteArray) {
        throw NoSQLiteStatementAccessException()
    }

    override fun bindDouble(index: Int, value: Double) {
        throw NoSQLiteStatementAccessException()
    }

    override fun bindLong(index: Int, value: Long) {
        throw NoSQLiteStatementAccessException()
    }

    override fun bindText(index: Int, value: String) {
        throw NoSQLiteStatementAccessException()
    }

    override fun bindNull(index: Int) {
        throw NoSQLiteStatementAccessException()
    }

    override fun getBlob(index: Int): ByteArray {
        throw NoSQLiteStatementAccessException()
    }

    override fun getDouble(index: Int): Double {
        throw NoSQLiteStatementAccessException()
    }

    override fun getLong(index: Int): Long {
        throw NoSQLiteStatementAccessException()
    }

    override fun getText(index: Int): String {
        throw NoSQLiteStatementAccessException()
    }

    override fun isNull(index: Int): Boolean {
        throw NoSQLiteStatementAccessException()
    }

    override fun getColumnCount(): Int = 0

    override fun getColumnName(index: Int): String {
        throw NoSQLiteStatementAccessException()
    }

    override fun getColumnType(index: Int): Int {
        throw NoSQLiteStatementAccessException()
    }

    override fun step(): Boolean {
        throw NoSQLiteStatementAccessException()
    }

    override fun reset() {
        throw NoSQLiteStatementAccessException()
    }

    override fun clearBindings() {
        throw NoSQLiteStatementAccessException()
    }

    override fun close() {
    }
}
