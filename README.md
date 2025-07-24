# sqlite-kt

A simple wrapper around [androidx.sqlite](https://developer.android.com/kotlin/multiplatform/sqlite) for Kotlin Multiplatform. This library provides some good-to-have features around sqlite, like a simple connection pool built for kotlinx.coroutines, while still letting users do low-level things with sqlite and leverage its extremely low latency.

**NOTE: This library is a WIP for my own projects, expect breaking changes!**

## Features

- Automatic setup of WAL for performance, with a connection pool that has one write connection and many read connections.
- Uses Kotlin's experimental context-parameters introduced in `2.2.0` to jump between suspending and database world nicely.
  - Suspending calls are only required once to get access to a read/write connection, the rest of db logic just uses the connection as a context parameter
- TODO: Observe changes as flows (by listening to table updates)
- TODO: Potential to support new targets as androidx.sqlite multiplatform drivers mature
