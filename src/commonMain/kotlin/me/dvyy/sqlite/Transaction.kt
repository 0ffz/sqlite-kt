package me.dvyy.sqlite

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import me.dvyy.sqlite.binds.NamedColumnSqliteStatement
import me.dvyy.sqlite.binds.bindAny
import org.intellij.lang.annotations.Language
import kotlin.coroutines.RestrictsSuspension

@RestrictsSuspension
open class Transaction(
    @PublishedApi
    @JvmField
    internal val connection: SQLiteConnection,
) {
    inline fun <T> prepare(
        @Language("SQLite") sql: String,
        statement: NamedColumnSqliteStatement.() -> T,
    ): T {
        return connection.prepare(sql).use {
            statement(NamedColumnSqliteStatement(it))
        }
    }

    inline fun SQLiteStatement.bindParams(vararg params: Any) {
        params.forEachIndexed { i, it -> bindAny(i + 1, it) }
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
