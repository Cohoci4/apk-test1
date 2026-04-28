package com.foodenhancer.domain.usecase

import com.foodenhancer.domain.model.EnhancementStyle
import com.foodenhancer.domain.model.FoodImage
import com.foodenhancer.domain.model.ProcessingResult
import com.foodenhancer.domain.repository.HistoryRepository
import com.foodenhancer.domain.repository.ImageEnhancerRepository
import com.foodenhancer.domain.repository.ImageSegmentationRepository
import com.foodenhancer.domain.repository.StyleRepository
import com.foodenhancer.domain.repository.SubscriptionRepository
import com.foodenhancer.domain.repository.WatermarkApplier
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
    private val subscriptionRepository: SubscriptionRepository,
    private val watermarkApplier: WatermarkApplier,
    private val styleRepository: StyleRepository
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
        val isManualMode = maskResult.isFailure

        val result = if (isManualMode) {
            val fallbackResult = enhancerRepository.enhanceFallback(imageBytes, styleId)
            if (fallbackResult.isFailure) {
                return Result.failure(fallbackResult.exceptionOrNull() ?: Exception("Enhancement failed"))
            }
            fallbackResult.getOrThrow()
        } else {
            val mask = maskResult.getOrThrow()
            val enhanceResult = enhancerRepository.enhance(imageBytes, mask, styleId)
            if (enhanceResult.isFailure) {
                return Result.failure(enhanceResult.exceptionOrNull() ?: Exception("Enhancement failed"))
            }
            enhanceResult.getOrThrow()
        }

        val finalUri = if (!isPro) {
            try {
                watermarkApplier.applyWatermark(result.resultUri)
            } catch (_: Exception) {
                result.resultUri
            }
        } else {
            result.resultUri
        }

        val foodImage = FoodImage(
            id = UUID.randomUUID().toString(),
            originalUri = imageUri,
            processedUri = finalUri,
            style = styleRepository.getAllStyles().first().find { it.id == styleId }
                ?: EnhancementStyle.ALL.find { it.id == styleId },
            createdAt = System.currentTimeMillis()
        )
        historyRepository.save(foodImage)

        return Result.success(
            ProcessingResult(
                resultUri = finalUri,
                thumbnailUri = result.thumbnailUri,
                isManualMode = isManualMode
            )
        )
    }
}
