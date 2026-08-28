package com.luojiaping.onmyenglish.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.value

sealed interface AppError {
    val message: String

    data class Validation(override val message: String) : AppError

    data class Authentication(override val message: String = "AI API key is missing or invalid") : AppError

    data class RateLimited(
        override val message: String = "AI provider rate limit reached",
        val retryAfterSeconds: Long? = null,
    ) : AppError

    data class Remote(
        val statusCode: Int,
        override val message: String,
        val retryable: Boolean,
    ) : AppError

    data class Network(override val message: String, val cause: Throwable? = null) : AppError

    data class Parsing(override val message: String, val cause: Throwable? = null) : AppError

    data class Storage(override val message: String, val cause: Throwable? = null) : AppError

    data class Unknown(override val message: String, val cause: Throwable? = null) : AppError
}
