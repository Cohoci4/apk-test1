package com.foodenhancer.domain.usecase

import com.foodenhancer.domain.model.EnhancementStyle
import com.foodenhancer.domain.model.FoodImage
import com.foodenhancer.domain.model.ProcessingResult
import com.foodenhancer.domain.repository.HistoryRepository
import com.foodenhancer.domain.repository.ImageEnhancerRepository
import com.foodenhancer.domain.repository.ImageSegmentationRepository
import com.foodenhancer.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnhanceFoodImageUseCase @Inject constructor(
    private val segmentationRepository: ImageSegmentationRepository,
    private val enhancerRepository: ImageEnhancerRepository,
    private val historyRepository: HistoryRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    private val demoMutex = Mutex()

    suspend operator fun invoke(
        imageBytes: ByteArray,
        imageUri: String,
        styleId: String
    ): Result<ProcessingResult> {
        val isPro = subscriptionRepository.isPro().first()
        if (!isPro) {
            demoMutex.withLock {
                val remaining = subscriptionRepository.getRemainingDemo().first()
                if (remaining <= 0) {
                    return Result.failure(IllegalStateException("Demo limit exceeded. Please upgrade to Pro."))
                }
                subscriptionRepository.decrementDemo()
            }
        }

        val maskResult = segmentationRepository.segment(imageBytes)
        if (maskResult.isFailure) {
            return Result.failure(maskResult.exceptionOrNull() ?: Exception("Segmentation failed"))
        }
        val mask = maskResult.getOrThrow()

        val enhanceResult = enhancerRepository.enhance(imageBytes, mask, styleId)
        if (enhanceResult.isFailure) {
            return Result.failure(enhanceResult.exceptionOrNull() ?: Exception("Enhancement failed"))
        }
        val result = enhanceResult.getOrThrow()

        val foodImage = FoodImage(
            id = UUID.randomUUID().toString(),
            originalUri = imageUri,
            processedUri = result.resultUri,
            style = EnhancementStyle.ALL.find { it.id == styleId },
            createdAt = System.currentTimeMillis()
        )
        historyRepository.save(foodImage)

        return Result.success(result)
    }
}
