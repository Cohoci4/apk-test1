package com.foodenhancer.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FoodImageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FoodEnhancerDatabase : RoomDatabase() {
    abstract fun foodImageDao(): FoodImageDao
}
