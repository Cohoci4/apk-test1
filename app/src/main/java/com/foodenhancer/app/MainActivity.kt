package com.foodenhancer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.foodenhancer.app.ui.navigation.AppNavGraph
import com.foodenhancer.app.ui.theme.FoodEnhancerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodEnhancerTheme {
                AppNavGraph()
            }
        }
    }
}
