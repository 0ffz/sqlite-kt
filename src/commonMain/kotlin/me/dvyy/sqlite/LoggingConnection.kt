package me.dvyy.sqlite

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement

class LoggingConnection(val delegate: SQLiteConnection) : SQLiteConnection by delegate {
    override fun prepare(sql: String): SQLiteStatement {
//        println("Prepared SQL: $sql")
        return delegate.prepare(sql)
    }
}
