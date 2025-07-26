package me.dvyy.sqlite

import me.dvyy.sqlite.connection.PrepareCachingSQLiteConnection
import me.dvyy.sqlite.statement.NamedColumnSqliteStatement
import me.dvyy.sqlite.statement.SelectStatement
import me.dvyy.sqlite.statement.bindParams
import org.intellij.lang.annotations.Language
import kotlin.coroutines.RestrictsSuspension

@RestrictsSuspension
open class Transaction(
    @PublishedApi
    @JvmField
    internal val connection: PrepareCachingSQLiteConnection,
) {
    inline fun <T> prepare(
        @Language("SQLite") sql: String,
        statement: NamedColumnSqliteStatement.() -> T,
    ): T {
        return statement((connection.prepare(sql)))
    }


    inline fun <T> getSingle(
        @Language("SQLite") sql: String,
        vararg parameters: Any,
        statement: NamedColumnSqliteStatement.() -> T,
    ): T = prepare(sql) {
        bindParams(*parameters)
        step()
        statement()
    }

    inline fun <T> getOrNull(
        @Language("SQLite") sql: String,
        vararg parameters: Any,
        statement: NamedColumnSqliteStatement.() -> T,
    ): T? = prepare(sql) {
        bindParams(*parameters)
        if (!step()) return null
        statement()
    }

    inline fun <T> getList(
        @Language("SQLite") sql: String,
        vararg parameters: Any,
        statement: NamedColumnSqliteStatement.() -> T,
    ): List<T> = buildList {
        prepare(sql) {
            bindParams(*parameters)
            while (step()) {
                add(statement())
            }
        }
    }

    fun select(
        @Language("SQLite") sql: String,
        vararg parameters: Any,
    ): SelectStatement = SelectStatement(sql, parameters)

    inline fun <T> forEach(@Language("SQLite") sql: String, statement: NamedColumnSqliteStatement.() -> T) {
        prepare(sql) {
            while (step()) {
                statement()
            }
        }
    }

    fun exec(@Language("SQLite") sql: String, vararg parameters: Any) {
        prepare(sql) {
            bindParams(*parameters)
            step()
        }
    }
}
