package com.foodenhancer.core_ml.di

import android.content.Context
import com.foodenhancer.core_ml.FoodSegmenter
import com.foodenhancer.core_ml.ImageSegmenter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    @Provides
    @Singleton
    fun provideImageSegmenter(@ApplicationContext context: Context): ImageSegmenter {
        val segmenter = ImageSegmenter(context)
        try {
            segmenter.initialize()
        } catch (_: Exception) {
            // Model not available, will use fallback mask
        }
        return segmenter
    }

    @Provides
    @Singleton
    fun provideFoodSegmenter(@ApplicationContext context: Context): FoodSegmenter {
        val segmenter = FoodSegmenter(context)
        try {
            segmenter.initialize()
        } catch (_: Exception) {
            // Model not available, will use fallback mask
        }
        return segmenter
    }
}
