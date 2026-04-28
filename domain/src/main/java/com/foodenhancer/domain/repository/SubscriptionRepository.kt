package com.foodenhancer.domain.repository

import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun isPro(): Flow<Boolean>
    fun getRemainingDemo(): Flow<Int>
    suspend fun decrementDemo()
}
