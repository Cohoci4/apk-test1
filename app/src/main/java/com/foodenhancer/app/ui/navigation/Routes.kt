package com.foodenhancer.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val STYLE_PICKER = "style_picker/{imageUri}"
    const val CROP_ROTATE = "crop/{imageUri}/{styleId}"
    const val RESULT = "result/{originalUri}/{processedUri}/{styleName}"
    const val HISTORY = "history"
    const val SUBSCRIPTION = "subscription"
    const val BATCH_STYLE = "batch_style"
    const val BATCH_RESULT = "batch_result"

    fun stylePicker(imageUri: String) = "style_picker/$imageUri"
    fun cropRotate(imageUri: String, styleId: String) = "crop/$imageUri/$styleId"
    fun result(originalUri: String, processedUri: String, styleName: String) =
        "result/$originalUri/$processedUri/$styleName"
}
