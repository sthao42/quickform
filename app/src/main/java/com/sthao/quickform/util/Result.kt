package com.sthao.quickform.util

/**
 * A generic wrapper for handling operations that can succeed or fail.
 * This helps with consistent error handling across the app.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

/**
 * Returns true if the result is successful.
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * Returns true if the result is an error.
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/**
 * Returns true if the result is loading.
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

/**
 * Returns the data if successful, null otherwise.
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Returns the data if successful, or the default value if not.
 */
fun <T> Result<T>.getOrDefault(defaultValue: T): T = when (this) {
    is Result.Success -> data
    else -> defaultValue
}

/**
 * Transforms the data if the result is successful.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> this
}

/**
 * Executes the given block if the result is successful.
 */
inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        block(data)
    }
    return this
}

/**
 * Executes the given block if the result is an error.
 */
inline fun <T> Result<T>.onError(block: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) {
        block(exception)
    }
    return this
}

/**
 * Executes the given block if the result is loading.
 */
inline fun <T> Result<T>.onLoading(block: () -> Unit): Result<T> {
    if (this is Result.Loading) {
        block()
    }
    return this
}
