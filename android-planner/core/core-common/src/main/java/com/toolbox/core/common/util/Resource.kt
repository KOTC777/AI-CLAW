package com.toolbox.core.common.util

/**
 * Sealed class for representing resource states.
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isLoading: Boolean get() = this is Loading
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw cause ?: IllegalStateException(message)
        is Loading -> throw IllegalStateException("Resource is still loading")
    }
}
