package com.foodenhancer.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FoodImageEntity)

    @Query("DELETE FROM food_images WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM food_images ORDER BY createdAt DESC")
    fun getAll(): Flow<List<FoodImageEntity>>
}
