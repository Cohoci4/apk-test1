package com.foodenhancer.core.util

import android.graphics.Bitmap
import android.graphics.Color

object ImageCompositor {

    /**
     * Composites the original image onto a background using the segmentation mask.
     * Where the mask is white (foreground), pixels come from the original.
     * Where the mask is black (background), pixels come from the background image.
     * A Gaussian blur is applied to the mask edges for smooth blending.
     *
     * @param original The original food photo.
     * @param mask The segmentation mask (white = foreground food, black = background).
     * @param background The style-specific background image.
     * @param blurRadius Radius for feathering mask edges (default 5).
     * @return The composited Bitmap.
     */
    fun composite(
        original: Bitmap,
        mask: Bitmap,
        background: Bitmap,
        blurRadius: Int = 5
    ): Bitmap {
        val width = original.width
        val height = original.height

        val scaledMask = Bitmap.createScaledBitmap(mask, width, height, true)
        val scaledBg = Bitmap.createScaledBitmap(background, width, height, true)

        val featheredMask = applyBoxBlur(scaledMask, blurRadius)

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val originalPixels = IntArray(width * height)
        val bgPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)

        original.getPixels(originalPixels, 0, width, 0, 0, width, height)
        scaledBg.getPixels(bgPixels, 0, width, 0, 0, width, height)
        featheredMask.getPixels(maskPixels, 0, width, 0, 0, width, height)

        for (i in originalPixels.indices) {
            val maskValue = Color.red(maskPixels[i]) / 255f

            val origR = Color.red(originalPixels[i])
            val origG = Color.green(originalPixels[i])
            val origB = Color.blue(originalPixels[i])

            val bgR = Color.red(bgPixels[i])
            val bgG = Color.green(bgPixels[i])
            val bgB = Color.blue(bgPixels[i])

            val r = (origR * maskValue + bgR * (1f - maskValue)).toInt().coerceIn(0, 255)
            val g = (origG * maskValue + bgG * (1f - maskValue)).toInt().coerceIn(0, 255)
            val b = (origB * maskValue + bgB * (1f - maskValue)).toInt().coerceIn(0, 255)

            resultPixels[i] = Color.argb(255, r, g, b)
        }

        result.setPixels(resultPixels, 0, width, 0, 0, width, height)

        if (scaledMask !== mask) scaledMask.recycle()
        if (scaledBg !== background) scaledBg.recycle()
        featheredMask.recycle()

        return result
    }

    /**
     * Applies a box blur approximation of Gaussian blur to the mask bitmap.
     * Three passes of box blur approximate a Gaussian blur.
     */
    private fun applyBoxBlur(bitmap: Bitmap, radius: Int): Bitmap {
        if (radius <= 0) return bitmap.copy(Bitmap.Config.ARGB_8888, false)

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val values = IntArray(width * height) { Color.red(pixels[it]) }
        val temp = IntArray(width * height)

        // Three-pass box blur for Gaussian approximation
        for (pass in 0 until 3) {
            // Horizontal pass
            for (y in 0 until height) {
                var sum = 0
                var count = 0
                for (x in 0 until minOf(radius, width)) {
                    sum += values[y * width + x]
                    count++
                }
                for (x in 0 until width) {
                    val addX = x + radius
                    val removeX = x - radius - 1
                    if (addX < width) { sum += values[y * width + addX]; count++ }
                    if (removeX >= 0) { sum -= values[y * width + removeX]; count-- }
                    temp[y * width + x] = sum / maxOf(count, 1)
                }
            }

            // Vertical pass
            for (x in 0 until width) {
                var sum = 0
                var count = 0
                for (y in 0 until minOf(radius, height)) {
                    sum += temp[y * width + x]
                    count++
                }
                for (y in 0 until height) {
                    val addY = y + radius
                    val removeY = y - radius - 1
                    if (addY < height) { sum += temp[addY * width + x]; count++ }
                    if (removeY >= 0) { sum -= temp[removeY * width + x]; count-- }
                    values[y * width + x] = sum / maxOf(count, 1)
                }
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val resultPixels = IntArray(width * height) { Color.argb(255, values[it], values[it], values[it]) }
        result.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return result
    }
}
