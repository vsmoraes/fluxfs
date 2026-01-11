package com.vsmoraes.fluxfs

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
            val cause: Throwable?,
        ) : Error
    }
}

fun <T> FluxResult<T>.onSuccess(action: (T) -> Unit): FluxResult<T> {
    if (this is FluxResult.Success) action(value)
    return this
}

fun <T> FluxResult<T>.onError(action: (FluxResult.Error) -> Unit): FluxResult<T> {
    if (this is FluxResult.Error) action(this)
    return this
}

fun FluxResult<*>.isSuccess() = this is FluxResult.Success

fun FluxResult<*>.isError() = this is FluxResult.Error

fun FluxResult<Boolean>.isTrue() = this is FluxResult.Success && this.value

fun FluxResult<Boolean>.isFalse() = this is FluxResult.Success && !this.value
