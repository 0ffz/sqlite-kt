package me.dvyy.sqlite.connection

import androidx.sqlite.SQLiteConnection
import me.dvyy.sqlite.statement.NamedColumnSqliteStatement

/**
 * SQLiteConnection that caches a few prepared statements.
 *
 * Assumes consumers will not close (finalize) statements.
 */
class PrepareCachingSQLiteConnection(
    val sqliteConnection: SQLiteConnection,
    private val prepareCacheSize: Int = 4,
) : SQLiteConnection by sqliteConnection {
    private val preparedStrings = Array(prepareCacheSize) { "" }

    // NoSQLiteStatement to avoid a null box
    private val preparedStatements = Array(prepareCacheSize) {
        NamedColumnSqliteStatement(NoSQLiteStatement)
    }
    private var index = 0

    override fun prepare(sql: String): NamedColumnSqliteStatement {
        val cached = preparedStrings.indexOf(sql)
        if (cached != -1) {
            val prepared = preparedStatements[cached]
            prepared.reset()
            return prepared
        }

        val prepared = NamedColumnSqliteStatement(sqliteConnection.prepare(sql))
        val nextIndex = index++ % prepareCacheSize
        preparedStatements[nextIndex].close()
        preparedStatements[nextIndex] = prepared
        preparedStrings[nextIndex] = sql
        return prepared
    }

    override fun close() {
        preparedStatements.forEach { it.close() }
        sqliteConnection.close()
    }
}
