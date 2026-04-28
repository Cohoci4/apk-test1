package com.foodenhancer.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.foodenhancer.app.ui.theme.Primary

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    data object Camera : BottomNavItem(Routes.HOME, "Camera", Icons.Filled.CameraAlt)
    data object History : BottomNavItem(Routes.HISTORY, "History", Icons.Filled.History)
    data object Profile : BottomNavItem(Routes.SUBSCRIPTION, "Profile", Icons.Filled.Person)
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(BottomNavItem.Camera, BottomNavItem.History, BottomNavItem.Profile)

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary
                )
            )
        }
    }
}
