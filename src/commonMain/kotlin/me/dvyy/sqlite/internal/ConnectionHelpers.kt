package me.dvyy.sqlite.internal

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL


@PublishedApi
internal inline fun <R> SQLiteConnection.transaction(
    isDeferred: Boolean = true,
    crossinline block: () -> R,
): R {
    execSQL(if (isDeferred) "BEGIN IMMEDIATE" else "BEGIN EXCLUSIVE")
    val result = try {
        block()
    } catch (ex: Throwable) {
        execSQL("ROLLBACK")
        throw ex
    }
    execSQL("COMMIT")
    return result
}
