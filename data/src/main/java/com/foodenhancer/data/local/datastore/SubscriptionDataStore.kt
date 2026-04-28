package com.foodenhancer.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val IS_PRO_KEY = booleanPreferencesKey("is_pro")
        private val DEMO_COUNT_KEY = intPreferencesKey("demo_count")
        private const val DEFAULT_DEMO_COUNT = 5
    }

    val isPro: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[IS_PRO_KEY] ?: false
    }

    val demoCount: Flow<Int> = dataStore.data.map { prefs ->
        prefs[DEMO_COUNT_KEY] ?: DEFAULT_DEMO_COUNT
    }

    suspend fun setIsPro(isPro: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_PRO_KEY] = isPro
        }
    }

    suspend fun decrementDemo() {
        dataStore.edit { prefs ->
            val current = prefs[DEMO_COUNT_KEY] ?: DEFAULT_DEMO_COUNT
            prefs[DEMO_COUNT_KEY] = (current - 1).coerceAtLeast(0)
        }
    }
}
