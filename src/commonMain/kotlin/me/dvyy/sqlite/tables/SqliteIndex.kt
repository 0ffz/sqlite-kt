package me.dvyy.sqlite.tables

import me.dvyy.sqlite.WriteTransaction
import org.intellij.lang.annotations.Language

//TODO implement automatic index (re)-creation
private data class SqliteIndex(
    @param:Language("SQLite")
    val statement: String,
) {
    context(tx: WriteTransaction)
    fun create() {
        tx.exec(statement)
    }
}
