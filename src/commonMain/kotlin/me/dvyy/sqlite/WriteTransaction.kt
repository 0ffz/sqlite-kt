package me.dvyy.sqlite

import androidx.sqlite.SQLiteConnection
import me.dvyy.sqlite.tables.Table
import me.dvyy.sqlite.tables.TableReading
import kotlin.coroutines.RestrictsSuspension

@RestrictsSuspension
class WriteTransaction(
    connection: SQLiteConnection,
    identity: Identity,
) : Transaction(connection, identity) {
    val modifiedTables = mutableSetOf<Table>()

    //TODO complete modification api
    fun modified(vararg tables: TableReading) {
        modifiedTables.addAll(TableReading.reduce(tables.toSet()))
    }
//
//    fun modified(vararg tables: String) {
//        TODO("Implement getting tables by name")
//    }

//    fun insert(@Language("SQLite", prefix = "INSERT ") statement: String) {
//
//    }
}
