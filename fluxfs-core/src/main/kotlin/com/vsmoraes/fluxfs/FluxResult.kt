package com.vsmoraes.fluxfs

/**
 * Result type for filesystem operations that can either succeed with a value
 * or fail with a specific error type.
 *
 * FluxResult is used throughout FluxFS instead of throwing exceptions, making
 * error handling explicit and type-safe.
 *
 * We've opted to not use Kotlin's Result type for this purpose because it forces
 * the errors to be exceptions, which defeats the purpose of errors being explicit.
 */
sealed interface FluxResult<out T> {
    data class Success<T>(
        val value: T,
    ) : FluxResult<T>

    sealed interface Error : FluxResult<Nothing> {
        data class FileNotFound(
            val path: String,
        ) : Error

        data class FileAlreadyExists(
            val path: String,
        ) : Error

        data class DirectoryNotFound(
            val path: String,
        ) : Error

        data class DirectoryAlreadyExists(
            val path: String,
        ) : Error

        data class IOError(
            val message: String,
            val cause: Throwable? = null,
        ) : Error
    }
}

inline fun <T> FluxResult<T>.onSuccess(action: (T) -> Unit): FluxResult<T> =
    apply {
        if (this is FluxResult.Success) action(value)
    }

inline fun <T> FluxResult<T>.onError(action: (FluxResult.Error) -> Unit): FluxResult<T> =
    apply {
        if (this is FluxResult.Error) action(this)
    }

fun <T> FluxResult<T>.getOrNull(): T? =
    when (this) {
        is FluxResult.Success -> value
        is FluxResult.Error -> null
    }

fun <T> FluxResult<T>.getOrThrow(): T =
    when (this) {
        is FluxResult.Success -> value
        is FluxResult.Error -> throw IllegalStateException("FluxResult failed: $this")
    }

inline fun <T> FluxResult<T>.getOrElse(defaultValue: (FluxResult.Error) -> T): T =
    when (this) {
        is FluxResult.Success -> value
        is FluxResult.Error -> defaultValue(this)
    }

fun FluxResult<*>.isSuccess(): Boolean = this is FluxResult.Success

fun FluxResult<*>.isError(): Boolean = this is FluxResult.Error

fun FluxResult<Boolean>.isTrue(): Boolean = this is FluxResult.Success && value

fun FluxResult<Boolean>.isFalse(): Boolean = this is FluxResult.Success && !value
