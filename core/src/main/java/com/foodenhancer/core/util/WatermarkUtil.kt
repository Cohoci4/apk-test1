package com.foodenhancer.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object WatermarkUtil {

    fun applyWatermark(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 80
            textSize = result.width * 0.12f
            typeface = Typeface.DEFAULT_BOLD
        }

        val text = "Demo"
        val textWidth = paint.measureText(text)

        canvas.save()
        canvas.rotate(-30f, result.width / 2f, result.height / 2f)

        val spacing = textWidth * 1.5f
        val rows = (result.height / spacing * 2).toInt() + 2
        val cols = (result.width / spacing * 2).toInt() + 2

        for (row in -rows..rows) {
            for (col in -cols..cols) {
                val x = result.width / 2f + col * spacing
                val y = result.height / 2f + row * spacing
                canvas.drawText(text, x, y, paint)
            }
        }

        canvas.restore()
        return result
    }
}
