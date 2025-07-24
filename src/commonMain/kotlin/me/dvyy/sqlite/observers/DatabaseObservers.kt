package me.dvyy.sqlite.observers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import me.dvyy.sqlite.tables.Table

class DatabaseObservers() {
    val tableObservers = mutableMapOf<String, MutableSharedFlow<Unit>>()

    fun notify(tables: Set<Table>) {
        tables.forEach { table -> tableObservers[table.name]?.tryEmit(Unit) }
    }

    fun forTable(table: String): Flow<Unit> {
        return tableObservers.getOrPut(table) {
            MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
        }
    }

    fun forTables(tables: Collection<Table>): Flow<Unit> {
        return merge(*tables.map { forTable(it.name) }.toTypedArray())
    }
}
