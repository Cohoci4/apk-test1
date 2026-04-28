package com.foodenhancer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.foodenhancer.domain.model.EnhancementStyle
import com.foodenhancer.domain.model.StyleCategory
import com.foodenhancer.domain.repository.StyleRepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StyleRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : StyleRepository {

    private val gson = Gson()

    private data class CatalogJson(
        val categories: List<CategoryJson>,
        val styles: List<StyleJson>
    )

    private data class CategoryJson(
        val id: String,
        val name: String,
        val displayOrder: Int
    )

    private data class StyleJson(
        val id: String,
        val name: String,
        val categoryId: String,
        val previewImage: String,
        val backgroundImage: String
    )

    private val catalogFlow: Flow<CatalogJson> = flow {
        val json = context.assets.open("styles_catalog.json").bufferedReader().use { it.readText() }
        emit(gson.fromJson(json, CatalogJson::class.java))
    }

    private val favoritesFlow: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[FAVORITES_KEY] ?: emptySet()
    }

    override fun getCategories(): Flow<List<StyleCategory>> = catalogFlow.map { catalog ->
        catalog.categories.map { cat ->
            StyleCategory(id = cat.id, name = cat.name, displayOrder = cat.displayOrder)
        }.sortedBy { it.displayOrder }
    }

    override fun getStylesByCategory(categoryId: String): Flow<List<EnhancementStyle>> =
        catalogFlow.map { catalog ->
            catalog.styles.filter { it.categoryId == categoryId }.map { it.toDomain() }
        }

    override fun getAllStyles(): Flow<List<EnhancementStyle>> = catalogFlow.map { catalog ->
        catalog.styles.map { it.toDomain() }
    }

    override fun getFavoriteStyles(): Flow<List<EnhancementStyle>> =
        combine(catalogFlow, favoritesFlow) { catalog, favIds ->
            catalog.styles.filter { it.id in favIds }.map { it.toDomain() }
        }

    override suspend fun toggleFavorite(styleId: String) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY] ?: emptySet()
            prefs[FAVORITES_KEY] = if (styleId in current) {
                current - styleId
            } else {
                current + styleId
            }
        }
    }

    override fun isFavorite(styleId: String): Flow<Boolean> = favoritesFlow.map { favIds ->
        styleId in favIds
    }

    private fun StyleJson.toDomain(): EnhancementStyle = EnhancementStyle(
        id = id,
        name = name,
        previewUrl = previewImage,
        categoryId = categoryId,
        backgroundImage = backgroundImage
    )

    companion object {
        private val FAVORITES_KEY = stringSetPreferencesKey("favorite_styles")
    }
}
