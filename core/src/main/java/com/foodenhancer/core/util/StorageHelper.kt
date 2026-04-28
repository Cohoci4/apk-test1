package com.foodenhancer.core.util

import android.os.Environment
import android.os.StatFs

object StorageHelper {
    private const val MIN_FREE_SPACE_MB = 50L

    fun hasEnoughStorage(): Boolean {
        return try {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            val availableMB = availableBytes / (1024 * 1024)
            availableMB >= MIN_FREE_SPACE_MB
        } catch (_: Exception) {
            true
        }
    }
}
