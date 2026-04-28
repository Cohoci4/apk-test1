package com.foodenhancer.data.di

import com.foodenhancer.data.repository.BillingRepositoryImpl
import com.foodenhancer.data.repository.HistoryRepositoryImpl
import com.foodenhancer.data.repository.ImageEnhancerRepositoryImpl
import com.foodenhancer.data.repository.ImageSegmentationRepositoryImpl
import com.foodenhancer.data.repository.SubscriptionRepositoryImpl
import com.foodenhancer.data.repository.WatermarkApplierImpl
import com.foodenhancer.domain.repository.BillingRepository
import com.foodenhancer.domain.repository.HistoryRepository
import com.foodenhancer.domain.repository.ImageEnhancerRepository
import com.foodenhancer.domain.repository.ImageSegmentationRepository
import com.foodenhancer.domain.repository.SubscriptionRepository
import com.foodenhancer.domain.repository.WatermarkApplier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindImageEnhancerRepository(
        impl: ImageEnhancerRepositoryImpl
    ): ImageEnhancerRepository

    @Binds
    @Singleton
    abstract fun bindImageSegmentationRepository(
        impl: ImageSegmentationRepositoryImpl
    ): ImageSegmentationRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        impl: BillingRepositoryImpl
    ): BillingRepository

    @Binds
    @Singleton
    abstract fun bindWatermarkApplier(
        impl: WatermarkApplierImpl
    ): WatermarkApplier
}
