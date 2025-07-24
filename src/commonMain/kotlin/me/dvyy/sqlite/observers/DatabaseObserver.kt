package me.dvyy.sqlite.observers

import me.dvyy.sqlite.Transaction

fun interface DatabaseObserver {
    fun collect(tx: Transaction)
}
