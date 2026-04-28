package com.foodenhancer.app.ui.navigation

object Routes {
    const val HOME = "home"
    const val STYLE_PICKER = "style_picker/{imageUri}"
    const val RESULT = "result/{originalUri}/{processedUri}/{styleName}"
    const val HISTORY = "history"
    const val SUBSCRIPTION = "subscription"

    fun stylePicker(imageUri: String) = "style_picker/$imageUri"
    fun result(originalUri: String, processedUri: String, styleName: String) =
        "result/$originalUri/$processedUri/$styleName"
}
