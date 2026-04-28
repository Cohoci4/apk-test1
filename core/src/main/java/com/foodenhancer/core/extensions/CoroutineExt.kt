package com.foodenhancer.core.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> safeCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: suspend () -> T
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
