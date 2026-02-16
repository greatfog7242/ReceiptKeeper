package com.receiptkeeper.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class for bottom navigation items
 */
data class BottomNavItem(
    val route: Routes,
    val icon: ImageVector,
    val label: String
)

/**
 * Bottom navigation items configuration
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = Routes.Books,
        icon = Icons.Default.Book,
        label = "Books"
    ),
    BottomNavItem(
        route = Routes.Receipts,
        icon = Icons.Default.Receipt,
        label = "Receipts"
    ),
    BottomNavItem(
        route = Routes.Scan,
        icon = Icons.Default.CameraAlt,
        label = "Scan"
    ),
    BottomNavItem(
        route = Routes.Analytics,
        icon = Icons.Default.Analytics,
        label = "Analytics"
    ),
    BottomNavItem(
        route = Routes.Settings,
        icon = Icons.Default.Settings,
        label = "Settings"
    )
)
