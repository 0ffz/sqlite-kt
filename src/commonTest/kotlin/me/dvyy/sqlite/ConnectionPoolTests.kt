package me.dvyy.sqlite

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.Test

class ConnectionPoolTests {
    @Test
    fun `each thread should reuse the same connection`() = runTest {
        val db = Database.temporary()
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
        val db = Database.temporary()
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

    @Test
    fun `in memory database should correctly store data with read-write`() = runTest {
        val db = Database.inMemorySingleConnection()
        db.write {
            exec("CREATE TABLE test (id INTEGER PRIMARY KEY AUTOINCREMENT)")
            exec("INSERT INTO test VALUES (1)")
        }
        db.read {
            getSingle("SELECT * FROM test") {
                getInt(0) shouldBe 1
            }
        }
    }

    @Test
    fun `temporary database should correctly store data with read-write`() = runTest {
        val db = Database.temporary()
        db.write {
            exec("CREATE TABLE test (id INTEGER PRIMARY KEY AUTOINCREMENT)")
            exec("INSERT INTO test VALUES (1)")
        }
        db.read {
            getSingle("SELECT * FROM test") {
                getInt(0) shouldBe 1
            }
        }
        db.close()
    }

    @Test
    fun `should correctly cache prepared statements`() = runTest {
        val db = Database.temporary()

        db.read {
            repeat(100) {
                getSingle("SELECT $it") { getInt(0) } shouldBe it
                getSingle("SELECT $it") { getInt(0) } shouldBe it
            }
        }

        db.write {
            repeat(100) {
                getSingle("SELECT $it") { getInt(0) } shouldBe it
                getSingle("SELECT $it") { getInt(0) } shouldBe it
            }
        }
    }

    @Test
    fun `should finalize old prepared statements`() = runTest {
        val db = Database.temporary()
        db.write {
            val preparedStatements = (0..100).map {
                getSingle("SELECT $it") { this }
            }
            shouldThrow<SQLiteException> {
                preparedStatements.first().reset()
            }
            shouldNotThrow<SQLiteException> {
                preparedStatements.last().reset()
            }
        }
    }

//    @Test
//    fun `read performance`() = runTest {
//        Database(path = "test.db").use { db ->
//            db.read {
//                measureTime {
//                    repeat(1000000) {
//                        prepare("SELECT data ->> 'test' FROM test") {
//                            while (step()) {
//                                getInt(0)
//                            }
//                            reset()
//                        }
//                    }
////                        getSingle("SELECT * FROM test") { getInt(0) }
////                    }
//                }.let { println(it) }
//            }
//        }
//    }
}
