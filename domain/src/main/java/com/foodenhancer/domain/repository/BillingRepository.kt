package com.foodenhancer.domain.repository

import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    suspend fun purchasePro()
    fun isProPurchased(): Flow<Boolean>
}
