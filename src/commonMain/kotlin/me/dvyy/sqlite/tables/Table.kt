package me.dvyy.sqlite.tables

import me.dvyy.sqlite.WriteTransaction
import org.intellij.lang.annotations.Language

open class Table(
    @param:Language("SQLite")
    val createStatement: String,
) : TableReading {
    override val involves: Set<TableReading> = setOf(this)

    override val name: String = nameRegex.find(createStatement)!!.groupValues[1]
    override fun toString(): String = name

    context(tx: WriteTransaction)
    override fun create() {
        tx.exec(createStatement)
    }

    companion object {
        val nameRegex = "CREATE .*TABLE (?:IF NOT EXISTS\\s+)?(\\w+)".toRegex()
    }
}
