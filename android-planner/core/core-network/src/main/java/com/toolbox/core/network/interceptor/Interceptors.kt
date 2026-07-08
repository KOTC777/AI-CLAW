package com.toolbox.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Auth interceptor that adds API key to requests.
 */
class AuthInterceptor(
    private val apiKeyProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val apiKey = apiKeyProvider() ?: return chain.proceed(original)

        val request = original.newBuilder()
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .build()

        return chain.proceed(request)
    }
}

/**
 * Retry interceptor with exponential backoff.
 */
class RetryInterceptor(
    private val maxRetries: Int = 3
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                return chain.proceed(chain.request())
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    Thread.sleep((1000L * (attempt + 1)))
                }
            }
        }
        throw lastException ?: IllegalStateException("Request failed after $maxRetries retries")
    }
}
