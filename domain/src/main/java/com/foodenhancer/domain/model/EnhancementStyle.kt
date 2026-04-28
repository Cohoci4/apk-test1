package com.foodenhancer.domain.model

data class EnhancementStyle(
    val id: String,
    val name: String,
    val previewUrl: String
) {
    companion object {
        val RUSTIC = EnhancementStyle("rustic", "Rustic", "https://styles.foodenhancer.com/rustic.jpg")
        val MINIMAL = EnhancementStyle("minimal", "Minimal", "https://styles.foodenhancer.com/minimal.jpg")
        val ITALIAN = EnhancementStyle("italian", "Italian Evening", "https://styles.foodenhancer.com/italian.jpg")
        val POP_ART = EnhancementStyle("pop_art", "Pop Art", "https://styles.foodenhancer.com/pop_art.jpg")
        val DARK_MOOD = EnhancementStyle("dark_mood", "Dark Mood", "https://styles.foodenhancer.com/dark_mood.jpg")

        val ALL = listOf(RUSTIC, MINIMAL, ITALIAN, POP_ART, DARK_MOOD)
    }
}
