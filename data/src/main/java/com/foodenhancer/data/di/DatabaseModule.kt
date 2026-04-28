package com.foodenhancer.data.di

import android.content.Context
import androidx.room.Room
import com.foodenhancer.data.local.db.FoodEnhancerDatabase
import com.foodenhancer.data.local.db.FoodImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FoodEnhancerDatabase {
        return Room.databaseBuilder(
            context,
            FoodEnhancerDatabase::class.java,
            "food_enhancer.db"
        ).build()
    }

    @Provides
    fun provideFoodImageDao(db: FoodEnhancerDatabase): FoodImageDao {
        return db.foodImageDao()
    }
}
