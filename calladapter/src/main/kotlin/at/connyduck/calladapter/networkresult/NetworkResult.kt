/**
 * Non-inline version of [kotlin.Result]
 * inline classes don't work well in combination with Retrofit
 */

@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
@file:OptIn(ExperimentalContracts::class)

package at.connyduck.calladapter.networkresult

import java.io.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [T]
 * or a failure with an arbitrary [Throwable] exception.
 */
public class NetworkResult<out T>
    @PublishedApi
    internal constructor(
        @PublishedApi
        internal val value: Any?,
    ) : Serializable {
        // discovery

        /**
         * Returns `true` if this instance represents a successful outcome.
         * In this case [isFailure] returns `false`.
         */
        public val isSuccess: Boolean get() = value !is Failure

        /**
         * Returns `true` if this instance represents a failed outcome.
         * In this case [isSuccess] returns `false`.
         */
        public val isFailure: Boolean get() = value is Failure

        // value & exception retrieval

        /**
         * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or `null`
         * if it is [failure][Result.isFailure].
         *
         * This function is a shorthand for `getOrElse { null }` (see [getOrElse]) or
         * `fold(onSuccess = { it }, onFailure = { null })` (see [fold]).
         */
        public inline fun getOrNull(): T? =
            when {
                isFailure -> null
                else -> value as T
            }

        /**
         * Returns the encapsulated [Throwable] exception if this instance represents [failure][isFailure] or `null`
         * if it is [success][isSuccess].
         *
         * This function is a shorthand for `fold(onSuccess = { null }, onFailure = { it })` (see [fold]).
         */
        public fun exceptionOrNull(): Throwable? =
            when (value) {
                is Failure -> value.exception
                else -> null
            }

        override fun equals(other: Any?): Boolean = other is NetworkResult<*> && value == other.value

        override fun hashCode(): Int = value.hashCode()

        /**
         * Returns a string `Success(v)` if this instance represents [success][Result.isSuccess]
         * where `v` is a string representation of the value or a string `Failure(x)` if
         * it is [failure][isFailure] where `x` is a string representation of the exception.
         */
        public override fun toString(): String =
            when (value) {
                is Failure -> value.toString() // "Failure($exception)"
                else -> "Success($value)"
            }

        // companion with constructors

        /**
         * Companion object for [Result] class that contains its constructor functions
         * [success] and [failure].
         */
        public companion object {
            /**
             * Returns an instance that encapsulates the given [value] as successful value.
             */
            @Suppress("INAPPLICABLE_JVM_NAME")
            @JvmName("success")
            public inline fun <T> success(value: T): NetworkResult<T> = NetworkResult(value)

            /**
             * Returns an instance that encapsulates the given [Throwable] [exception] as failure.
             */
            @Suppress("INAPPLICABLE_JVM_NAME")
            @JvmName("failure")
            public inline fun <T> failure(exception: Throwable): NetworkResult<T> = NetworkResult(createFailure(exception))

            /**
             * Converts this [NetworkResult] to a [Result] instance.
             */
            @JvmName("toResult")
            public inline fun <T> NetworkResult<T>.toResult(): Result<T> {
                return if (isSuccess) Result.success(value as T) else Result.failure(value as Throwable)
            }
        }

        internal class Failure(
            @JvmField
            val exception: Throwable,
        ) : Serializable {
            override fun equals(other: Any?): Boolean = other is Failure && exception == other.exception

            override fun hashCode(): Int = exception.hashCode()

            override fun toString(): String = "Failure($exception)"
        }
    }

/**
 * Creates an instance of internal marker [Result.Failure] class to
 * make sure that this class is not exposed in ABI.
 */
@PublishedApi
internal fun createFailure(exception: Throwable): Any = NetworkResult.Failure(exception)

/**
 * Throws exception if the result is failure. This internal function minimizes
 * inlined bytecode for [getOrThrow] and makes sure that in the future we can
 * add some exception-augmenting logic here (if needed).
 */
@PublishedApi
internal fun NetworkResult<*>.throwOnFailure() {
    if (value is NetworkResult.Failure) throw value.exception
}

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
public inline fun <R> runCatching(block: () -> R): NetworkResult<R> {
    return try {
        NetworkResult.success(block())
    } catch (e: Throwable) {
        NetworkResult.failure(e)
    }
}

/**
 * Calls the specified function [block] with `this` value as its receiver and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 */
public inline fun <T, R> T.runCatching(block: T.() -> R): NetworkResult<R> {
    return try {
        NetworkResult.success(block())
    } catch (e: Throwable) {
        NetworkResult.failure(e)
    }
}

// -- extensions ---

/**
 * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or throws the encapsulated [Throwable] exception
 * if it is [failure][Result.isFailure].
 *
 * This function is a shorthand for `getOrElse { throw it }` (see [getOrElse]).
 */
public inline fun <T> NetworkResult<T>.getOrThrow(): T {
    throwOnFailure()
    return value as T
}

/**
 * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or the
 * result of [onFailure] function for the encapsulated [Throwable] exception if it is [failure][Result.isFailure].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [onFailure] function.
 *
 * This function is a shorthand for `fold(onSuccess = { it }, onFailure = onFailure)` (see [fold]).
 */
public inline fun <R, T : R> NetworkResult<T>.getOrElse(onFailure: (exception: Throwable) -> R): R {
    contract {
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    return when (val exception = exceptionOrNull()) {
        null -> value as T
        else -> onFailure(exception)
    }
}

/**
 * Returns the encapsulated value if this instance represents [success][Result.isSuccess] or the
 * [defaultValue] if it is [failure][Result.isFailure].
 *
 * This function is a shorthand for `getOrElse { defaultValue }` (see [getOrElse]).
 */
public inline fun <R, T : R> NetworkResult<T>.getOrDefault(defaultValue: R): R {
    if (isFailure) return defaultValue
    return value as T
}

/**
 * Returns the result of [onSuccess] for the encapsulated value if this instance represents [success][Result.isSuccess]
 * or the result of [onFailure] function for the encapsulated [Throwable] exception if it is [failure][Result.isFailure].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [onSuccess] or by [onFailure] function.
 */
public inline fun <R, T> NetworkResult<T>.fold(
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable) -> R,
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    return when (val exception = exceptionOrNull()) {
        null -> onSuccess(value as T)
        else -> onFailure(exception)
    }
}

// transformation

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][Result.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][Result.isFailure].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] function.
 * See [mapCatching] for an alternative that encapsulates exceptions.
 */
public inline fun <R, T> NetworkResult<T>.map(transform: (value: T) -> R): NetworkResult<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when {
        isSuccess -> NetworkResult.success(transform(value as T))
        else -> NetworkResult(value)
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated value
 * if this instance represents [success][Result.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][Result.isFailure].
 *
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 * See [map] for an alternative that rethrows exceptions from `transform` function.
 */
public inline fun <R, T> NetworkResult<T>.mapCatching(transform: (value: T) -> R): NetworkResult<R> {
    return when {
        isSuccess -> runCatching { transform(value as T) }
        else -> NetworkResult(value)
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][Result.isFailure] or the
 * original encapsulated value if it is [success][Result.isSuccess].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] function.
 * See [recoverCatching] for an alternative that encapsulates exceptions.
 */
public inline fun <R, T : R> NetworkResult<T>.recover(transform: (exception: Throwable) -> R): NetworkResult<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (val exception = exceptionOrNull()) {
        null -> this
        else -> NetworkResult.success(transform(exception))
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][Result.isFailure] or the
 * original encapsulated value if it is [success][Result.isSuccess].
 *
 * This function catches any [Throwable] exception thrown by [transform] function and encapsulates it as a failure.
 * See [recover] for an alternative that rethrows exceptions.
 */
public inline fun <R, T : R> NetworkResult<T>.recoverCatching(transform: (exception: Throwable) -> R): NetworkResult<R> {
    return when (val exception = exceptionOrNull()) {
        null -> this
        else -> runCatching { transform(exception) }
    }
}

// "peek" onto value/exception and pipe

/**
 * Performs the given [action] on the encapsulated [Throwable] exception if this instance represents [failure][Result.isFailure].
 * Returns the original `Result` unchanged.
 */
public inline fun <T> NetworkResult<T>.onFailure(action: (exception: Throwable) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    exceptionOrNull()?.let { action(it) }
    return this
}

/**
 * Performs the given [action] on the encapsulated value if this instance represents [success][Result.isSuccess].
 * Returns the original `Result` unchanged.
 */
public inline fun <T> NetworkResult<T>.onSuccess(action: (value: T) -> Unit): NetworkResult<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (isSuccess) action(value as T)
    return this
}

// -------------------
