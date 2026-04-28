package com.foodenhancer.data.repository

import com.foodenhancer.data.local.datastore.SubscriptionDataStore
import com.foodenhancer.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val dataStore: SubscriptionDataStore
) : SubscriptionRepository {

    override fun isPro(): Flow<Boolean> = dataStore.isPro

    override fun getRemainingDemo(): Flow<Int> = dataStore.demoCount

    override suspend fun decrementDemo() {
        dataStore.decrementDemo()
    }
}
