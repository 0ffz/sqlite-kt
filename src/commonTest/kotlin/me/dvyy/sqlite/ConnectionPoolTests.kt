package me.dvyy.sqlite

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteException
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.Test

class ConnectionPoolTests {
    @Test
    fun `each thread should reuse the same connection`() = runTest {
        val db = Database(path = "test.db")
        val entries = ConcurrentLinkedQueue<Pair<Thread, SQLiteConnection>>()
        repeat(1000) {
            launch(Dispatchers.IO) {
                db.read {
                    entries.add(Thread.currentThread() to connection)
                }
            }
        }
        val threadToConnectionsUsed =
            entries.groupBy { it.first }.mapValues { it.value.map { it.second }.distinct().size }
        threadToConnectionsUsed.values.forEach {
            assert(it == 1) { "Thread used multiple connections, see connections per thread:\n$threadToConnectionsUsed" }
        }
        db.close()
    }

    @Test
    fun `readOnly connection should not allow writes`() = runTest {
        val db = Database(
            path = "test.db",
        )
        shouldNotThrow<SQLiteException> {
            db.read {
                getSingle("SELECT 'test'") { getText(0) }
            }
        }

        shouldThrow<SQLiteException> {
            db.read {
                exec("CREATE TABLE test (id INTEGER PRIMARY KEY AUTOINCREMENT)")
            }
        }
        db.close()
    }
}
