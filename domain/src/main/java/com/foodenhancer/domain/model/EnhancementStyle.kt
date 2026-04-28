package com.foodenhancer.domain.model

data class EnhancementStyle(
    val id: String,
    val name: String,
    val previewUrl: String,
    val categoryId: String = "",
    val backgroundImage: String = ""
) {
    companion object {
        val RUSTIC = EnhancementStyle("rustic", "Rustic Wood", "backgrounds/rustic.jpg", "mood", "backgrounds/rustic.jpg")
        val MINIMAL = EnhancementStyle("minimal", "Clean White", "backgrounds/minimal.jpg", "minimal", "backgrounds/minimal.jpg")
        val ITALIAN = EnhancementStyle("italian", "Italian Evening", "backgrounds/italian.jpg", "cuisine", "backgrounds/italian.jpg")
        val POP_ART = EnhancementStyle("pop_art", "Pop Art", "backgrounds/popart.jpg", "mood", "backgrounds/popart.jpg")
        val DARK_MOOD = EnhancementStyle("dark_mood", "Dark Mood", "backgrounds/dark.jpg", "mood", "backgrounds/dark.jpg")

        val ALL = listOf(RUSTIC, MINIMAL, ITALIAN, POP_ART, DARK_MOOD)
    }
}
