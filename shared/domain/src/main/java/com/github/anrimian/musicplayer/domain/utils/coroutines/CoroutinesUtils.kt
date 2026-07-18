package com.github.anrimian.musicplayer.domain.utils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

fun CoroutineScope.launchCatching(
    onError: ((Throwable) -> Unit)? = null,
    onProgress: ((Boolean) -> Unit)? = null,
    action: (suspend CoroutineScope.() -> Unit)
): Job {
    return launch {
        try {
            onProgress?.invoke(true)
            action()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            if (onError != null) {
                onError(e)
            } else {
                throw e
            }
        } finally {
            onProgress?.invoke(false)
        }
    }
}

fun <T> Flow<T>.subscribeCatching(
    onNext: ((T) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    onProgress: ((Boolean) -> Unit)? = null,
    scope: CoroutineScope
): Job {
    return scope.launch {
        try {
            onProgress?.invoke(true)
            this@subscribeCatching.collectIndexed { index, item ->
                if (index == 0) {
                    onProgress?.invoke(false)
                }
                onNext?.invoke(item)
            }
            onComplete?.invoke()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            if (onError != null) {
                onError(e)
            } else {
                throw e
            }
        } finally {
            onProgress?.invoke(false)
        }
    }
}

fun <T> (suspend () -> T).onErrorReturn(defaultValue: T): suspend () -> T {
    return {
        try {
            this.invoke()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            defaultValue
        }
    }
}

fun <T> (suspend () -> T).doOnError(action: (Throwable) -> Unit): suspend () -> T {
    return {
        try {
            this.invoke()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            action(e)
            throw e
        }
    }
}

suspend inline fun <T> runOrNull(crossinline block: suspend () -> T): T? {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }
}

suspend inline fun <T> mapError(
    crossinline mapper: (Throwable) -> Throwable,
    crossinline block: suspend () -> T
): T {
    try {
        return block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        throw mapper(e)
    }
}

suspend fun <T> retryWithBackoff(times: Int, retryMillis: Long, block: suspend () -> T) {
    retryWithExponentialBackoff(times, retryMillis, 1f, block)
}

suspend fun <T> retryWithExponentialBackoff(
    times: Int,
    initialDelay: Long,
    factor: Float = 2.0f,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {}

        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong()
    }
    return block()
}

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (currentCoroutineContext().isActive) {
        emit(Unit)
        delay(period)
    }
}

fun tickerLongValueFlow(
    initialValue: Long,
    step: Long,
    period: Duration,
    initialDelay: Duration = Duration.ZERO
) = flow {
    delay(initialDelay)
    var value = initialValue
    while (currentCoroutineContext().isActive) {
        emit(value)
        value += step
        delay(period)
    }
}

fun <T> Flow<T>.onSuccess(action: suspend () -> Unit): Flow<T> {
    return this.onCompletion { cause ->
        if (cause == null) {
            action()
        }
    }
}

fun <T> Flow<T>.onErrorReturnItem(fallbackValue: T): Flow<T> {
    return this.catch { emit(fallbackValue) }
}

fun <T> Flow<T>.onErrorReturn(provider: (Throwable) -> T): Flow<T> {
    return this.catch { e -> emit(provider(e)) }
}

fun <T> Flow<T>.doOnError(action: (Throwable) -> Unit): Flow<T> {
    return this.catch { e ->
        action(e)
        throw e
    }
}

fun <T> Flow<T>.retryWithDelay(
    maxRetryCount: Int,
    delayMillis: Long
): Flow<T> {
    return this.retryWhen { cause, attempt ->
        if (cause is CancellationException) {
            throw cause
        }
        if (attempt < maxRetryCount) {
            delay(delayMillis)
            true
        } else {
            false
        }
    }
}