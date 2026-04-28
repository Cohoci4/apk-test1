package com.foodenhancer.domain.model

data class EnhancementStyle(
    val id: String,
    val name: String,
    val previewUrl: String
) {
    companion object {
        val VIBRANT = EnhancementStyle("vibrant", "Vibrant", "https://styles.foodenhancer.com/vibrant.jpg")
        val WARM = EnhancementStyle("warm", "Warm Tones", "https://styles.foodenhancer.com/warm.jpg")
        val COOL = EnhancementStyle("cool", "Cool Tones", "https://styles.foodenhancer.com/cool.jpg")
        val DRAMATIC = EnhancementStyle("dramatic", "Dramatic", "https://styles.foodenhancer.com/dramatic.jpg")
        val NATURAL = EnhancementStyle("natural", "Natural", "https://styles.foodenhancer.com/natural.jpg")

        val ALL = listOf(VIBRANT, WARM, COOL, DRAMATIC, NATURAL)
    }
}
