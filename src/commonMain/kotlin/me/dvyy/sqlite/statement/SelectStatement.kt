package me.dvyy.sqlite.statement

import me.dvyy.sqlite.Transaction

class SelectStatement(
    val sql: String,
    val parameters: Array<out Any>,
) {
    context(tx: Transaction)
    inline fun <T> prepare(block: NamedColumnSqliteStatement.() -> T): T = tx.prepare(sql) {
        bindParams(*parameters)
        block()
    }

    context(tx: Transaction)
    inline fun forEach(
        statement: NamedColumnSqliteStatement.() -> Unit,
    ) {
        prepare {
            while (step()) {
                statement()
            }
        }
    }

    context(tx: Transaction)
    inline fun <T> map(
        statement: NamedColumnSqliteStatement.() -> T,
    ): List<T> = buildList {
        this@SelectStatement.forEach {
            add(statement())
        }
    }

    context(tx: Transaction)
    inline fun <T> first(
        statement: NamedColumnSqliteStatement.() -> T,
    ): T = prepare {
        if (step()) statement()
        else error("Tried getting first row from empty result set")
    }

    context(tx: Transaction)
    inline fun <T> firstOrNull(
        statement: NamedColumnSqliteStatement.() -> T,
    ): T? = prepare {
        if (step()) statement()
        else null
    }
}
