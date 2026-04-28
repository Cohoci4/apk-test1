package com.foodenhancer.data.repository

import com.foodenhancer.data.local.datastore.SubscriptionDataStore
import com.foodenhancer.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    private val dataStore: SubscriptionDataStore
) : BillingRepository {

    override suspend fun purchasePro() {
        dataStore.setIsPro(true)
    }

    override fun isProPurchased(): Flow<Boolean> = dataStore.isPro
}
