package me.dvyy.sqlite

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.*
import androidx.sqlite.execSQL
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import me.dvyy.sqlite.observers.DatabaseObservers
import me.dvyy.sqlite.internal.throttle
import me.dvyy.sqlite.internal.transaction
import me.dvyy.sqlite.tables.TableReading
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

open class Database(
    private val driver: BundledSQLiteDriver,
    private val readConnections: Int = 4,
    private val path: String,
    val defaultIdentity: Identity? = 0,
    val watchQueryThrottle: Duration = 100.milliseconds
) {
    val writeConnection = createConnection(readOnly = false)
    val readerConnectionPool = Channel<Lazy<SQLiteConnection>>(readConnections)
    val dbWriteDispatcher = newSingleThreadContext("db-writes")
    val dbReadDispatcher = newFixedThreadPoolContext(readConnections, "db-reads")
    val observers = DatabaseObservers()

    fun createConnection(readOnly: Boolean): SQLiteConnection {
        //FIXME readonly errors
        val readFlag = if (readOnly) SQLITE_OPEN_READONLY else SQLITE_OPEN_READWRITE
        return driver.open(
                path,
                SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE or SQLITE_OPEN_NOMUTEX
            )
        .also {
            it.execSQL(
                """
                PRAGMA journal_mode=WAL;
                PRAGMA synchronous=normal;
                PRAGMA journal_size_limit=6144000;
                """.trimIndent()
            )
        }
    }

    init {
        repeat(readConnections) {
            readerConnectionPool.trySend(
                lazy { createConnection(readOnly = true) }
            )
        }
    }

    // TODO need SupervisorJob? Check this is safe with parallel writes
    suspend inline fun <T> write(
        identity: Identity = defaultIdentity ?: error("Identity must be specified when writing"),
        crossinline block: WriteTransaction.() -> T,
    ): T = withContext(dbWriteDispatcher) {
        val tx = WriteTransaction(writeConnection, identity)
        writeConnection.transaction {
            tx.block()
        }.also { observers.notify(tx.modifiedTables) }
    }

    suspend inline fun <T> read(
        crossinline block: Transaction.() -> T,
    ): T {
        val conn = readerConnectionPool.receive().value
        try {
            return withContext(dbReadDispatcher) {
                Transaction(conn).block()
            }
        } finally {
            readerConnectionPool.send(lazy { conn })
        }
    }

    inline fun <T> watch(
        vararg tables: TableReading,
        crossinline read: Transaction.() -> T,
    ) = flow {
        emit(read { read() })
        observers.forTables(TableReading.reduce(tables.toSet())).throttle(watchQueryThrottle).collect {
            emit(read { read() })
        }
    }
}
