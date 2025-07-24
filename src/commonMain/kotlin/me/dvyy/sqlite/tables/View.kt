package me.dvyy.sqlite.tables

import me.dvyy.sqlite.WriteTransaction
import org.intellij.lang.annotations.Language

open class View(
    override val name: String,
    @param:Language("SQLite")
    val selectStatement: String,
    override val involves: Set<TableReading>,
) : TableReading {

    context(tx: WriteTransaction)
    override fun create() {
        val existing = tx.getOrNull(
            "SELECT sql FROM sqlite_master WHERE type = 'view' AND name = '$name'"
        ) { getText(0) }
        val statement = "CREATE VIEW $name AS $selectStatement"
        if (existing != statement) {
            if (existing != null) tx.exec("DROP VIEW $name")
            tx.exec(statement)
        }
    }

    override fun toString() = name
}
