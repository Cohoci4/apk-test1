package com.foodenhancer.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foodenhancer.app.ui.screen.AboutScreen
import com.foodenhancer.app.ui.screen.BatchResultScreen
import com.foodenhancer.app.ui.screen.BatchStyleScreen
import com.foodenhancer.app.ui.screen.CropRotateScreen
import com.foodenhancer.app.ui.screen.HistoryScreen
import com.foodenhancer.app.ui.screen.HomeScreen
import com.foodenhancer.app.ui.screen.PrivacyScreen
import com.foodenhancer.app.ui.screen.ResultScreen
import com.foodenhancer.app.ui.screen.StylePickerScreen
import com.foodenhancer.app.ui.screen.SubscriptionScreen
import com.foodenhancer.app.ui.screen.TermsScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.HISTORY, Routes.SUBSCRIPTION)

    var batchPhotoUris by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var batchResultUris by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onImageSelected = { encodedUri ->
                        navController.navigate(Routes.stylePicker(encodedUri))
                    },
                    onHistoryItemClick = { originalUri, processedUri, styleName ->
                        navController.navigate(Routes.result(originalUri, processedUri, styleName))
                    },
                    onBatchDone = { uris ->
                        batchPhotoUris = uris
                        navController.navigate(Routes.BATCH_STYLE)
                    }
                )
            }

            composable(
                route = Routes.STYLE_PICKER,
                arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
            ) {
                StylePickerScreen(
                    onNavigateToResult = { originalUri, processedUri, styleName ->
                        navController.navigate(Routes.result(originalUri, processedUri, styleName)) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onNavigateToCrop = { imageUri, styleId ->
                        navController.navigate(Routes.cropRotate(imageUri, styleId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.CROP_ROTATE,
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType },
                    navArgument("styleId") { type = NavType.StringType }
                )
            ) {
                CropRotateScreen(
                    onNavigateToResult = { originalUri, processedUri, styleName ->
                        navController.navigate(Routes.result(originalUri, processedUri, styleName)) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.RESULT,
                arguments = listOf(
                    navArgument("originalUri") { type = NavType.StringType },
                    navArgument("processedUri") { type = NavType.StringType },
                    navArgument("styleName") { type = NavType.StringType }
                )
            ) {
                ResultScreen(
                    onBack = { navController.popBackStack() },
                    onEnhanceAgain = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(
                    onItemClick = { originalUri, processedUri, styleName ->
                        navController.navigate(Routes.result(originalUri, processedUri, styleName))
                    }
                )
            }

            composable(Routes.SUBSCRIPTION) {
                SubscriptionScreen(
                    onNavigateToAbout = { navController.navigate(Routes.ABOUT) },
                    onNavigateToPrivacy = { navController.navigate(Routes.PRIVACY) },
                    onNavigateToTerms = { navController.navigate(Routes.TERMS) }
                )
            }

            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.PRIVACY) {
                PrivacyScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.TERMS) {
                TermsScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.BATCH_STYLE) {
                BatchStyleScreen(
                    photoUris = batchPhotoUris,
                    onComplete = { resultUris ->
                        batchResultUris = resultUris
                        navController.navigate(Routes.BATCH_RESULT) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.BATCH_RESULT) {
                BatchResultScreen(
                    processedUris = batchResultUris,
                    onImageClick = { originalUri, processedUri ->
                        navController.navigate(
                            Routes.result(
                                Uri.encode(originalUri),
                                Uri.encode(processedUri),
                                Uri.encode("Batch")
                            )
                        )
                    },
                    onHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
