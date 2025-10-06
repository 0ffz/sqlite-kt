package me.dvyy.sqlite.connection

import androidx.sqlite.SQLiteConnection
import me.dvyy.sqlite.statement.CachingSqliteStatement
import me.dvyy.sqlite.statement.NamedColumnSqliteStatement

/**
 * SQLiteConnection that caches a few prepared statements.
 *
 * Assumes consumers will not close (finalize) statements.
 */
class PrepareCachingSQLiteConnection(
    val sqliteConnection: SQLiteConnection,
    private val prepareCacheSize: Int = 16,
) : SQLiteConnection by sqliteConnection {
    private val preparedStrings = Array(prepareCacheSize) { "" }

    // NoSQLiteStatement to avoid a null box
    private val preparedStatements = Array(prepareCacheSize) {
        CachingSqliteStatement(NoSQLiteStatement)
    }
    private var index = 0

    override fun prepare(sql: String): NamedColumnSqliteStatement {
        val cached = preparedStrings.indexOf(sql)
        if (cached != -1) {
            val prepared = preparedStatements[cached]
            return CachingSqliteStatement(prepared)
        }

        val prepared = CachingSqliteStatement(sqliteConnection.prepare(sql))
        val nameCaching = NamedColumnSqliteStatement(prepared)
        val nextIndex = index++ % prepareCacheSize
        preparedStatements[nextIndex].finalizeStatement()
        preparedStatements[nextIndex] = prepared
        preparedStrings[nextIndex] = sql
        return nameCaching
    }

    override fun close() {
        preparedStatements.forEach { it.finalizeStatement() }
        sqliteConnection.close()
    }
}
