package com.foodenhancer.domain.usecase

import com.foodenhancer.domain.model.BatchEnhanceResult
import com.foodenhancer.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject
import javax.inject.Singleton

data class BatchItem(
    val imageBytes: ByteArray,
    val imageUri: String,
    val styleId: String
)

@Singleton
class BatchEnhanceUseCase @Inject constructor(
    private val enhanceFoodImageUseCase: EnhanceFoodImageUseCase,
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(items: List<BatchItem>): Flow<BatchEnhanceResult> = flow {
        val isPro = subscriptionRepository.isPro().first()
        if (!isPro) {
            val remaining = subscriptionRepository.getRemainingDemo().first()
            if (remaining < items.size) {
                emit(
                    BatchEnhanceResult(
                        currentIndex = 0,
                        total = items.size,
                        processedUris = emptyList(),
                        errors = listOf("Not enough demo uses left (you need ${items.size}, have $remaining)"),
                        isComplete = true
                    )
                )
                return@flow
            }
        }

        val processedUris = mutableListOf<String>()
        val errors = mutableListOf<String>()

        items.forEachIndexed { index, item ->
            emit(
                BatchEnhanceResult(
                    currentIndex = index,
                    total = items.size,
                    processedUris = processedUris.toList(),
                    errors = errors.toList(),
                    isComplete = false
                )
            )

            val result = enhanceFoodImageUseCase(
                imageBytes = item.imageBytes,
                imageUri = item.imageUri,
                styleId = item.styleId
            )

            result.fold(
                onSuccess = { processingResult ->
                    processedUris.add(processingResult.resultUri)
                },
                onFailure = { error ->
                    errors.add("Photo ${index + 1}: ${error.message}")
                }
            )
        }

        emit(
            BatchEnhanceResult(
                currentIndex = items.size,
                total = items.size,
                processedUris = processedUris.toList(),
                errors = errors.toList(),
                isComplete = true
            )
        )
    }
}
