package com.foodenhancer.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_images")
data class FoodImageEntity(
    @PrimaryKey val id: String,
    val originalUri: String,
    val processedUri: String?,
    val styleId: String?,
    val styleName: String?,
    val createdAt: Long
)
