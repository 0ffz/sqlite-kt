package me.dvyy.sqlite.internal

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

/**
 * Throttles a flow with emissions on the leading and trailing edge.
 * Events, from the incoming flow, during the throttle window are discarded.
 * Events are discarded by using a conflated buffer.
 * This throttle method acts as a slow consumer, but backpressure is not a concern
 * due to the conflated buffer dropping events during the throttle window.
 */
@PublishedApi
internal fun <T> Flow<T>.throttle(window: Duration): Flow<T> = flow {
    val bufferedFlow = this@throttle.buffer(Channel.Factory.CONFLATED)
    bufferedFlow.collect { value ->
        emit(value)
        delay(window)
    }
}
