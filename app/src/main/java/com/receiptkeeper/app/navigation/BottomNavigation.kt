package com.receiptkeeper.app.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.receiptkeeper.core.preferences.IconTheme
import com.receiptkeeper.core.preferences.LocalIconTheme

/**
 * Bottom navigation bar component
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val iconTheme = LocalIconTheme.current
        
        val navItems = if (iconTheme == IconTheme.COLORFUL) {
            bottomNavItemsColorful
        } else {
            bottomNavItemsMonochrome
        }

        navItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    val iconSize = if (item.route == Routes.Scan) 48.dp else 24.dp
                    if (item.iconResId != null) {
                        // Colorful theme: WebP images with tint
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route.route } == true
                        val tintColor = if (isSelected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                        Icon(
                            painter = painterResource(id = item.iconResId),
                            contentDescription = item.label,
                            modifier = Modifier.size(iconSize),
                            tint = tintColor // Apply gray/green tint
                        )
                    } else if (item.iconVector != null) {
                        // Monochrome theme: Material Icons with theme tint
                        Icon(
                            imageVector = item.iconVector,
                            contentDescription = item.label,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route.route } == true,
                onClick = {
                    navController.navigate(item.route.route) {
                        // Pop up to the start destination to avoid building up a large back stack
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4CAF50), // Green for selected
                    selectedTextColor = Color(0xFF4CAF50), // Green text for selected
                    indicatorColor = Color.Transparent, // Remove indicator background
                    unselectedIconColor = Color(0xFF9E9E9E), // Gray for unselected
                    unselectedTextColor = Color(0xFF9E9E9E) // Gray text for unselected
                )
            )
        }
    }
}
