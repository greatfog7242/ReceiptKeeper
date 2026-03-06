package com.receiptkeeper.app.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

/**
 * Data class for bottom navigation items
 */
data class BottomNavItem(
    val route: Routes,
    val iconVector: ImageVector? = null,
    val iconResId: Int? = null,
    val label: String
) {
    init {
        require(iconVector != null || iconResId != null) {
            "Either iconVector or iconResId must be provided"
        }
    }
}

/**
 * Bottom navigation items configuration
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = Routes.Books,
        iconResId = com.receiptkeeper.R.drawable.ic_bottom_books,
        label = "Books"
    ),
    BottomNavItem(
        route = Routes.Receipts,
        iconResId = com.receiptkeeper.R.drawable.ic_bottom_receipts,
        label = "Receipts"
    ),
    BottomNavItem(
        route = Routes.Scan,
        iconResId = com.receiptkeeper.R.drawable.ic_bottom_scan,
        label = "Scan"
    ),
    BottomNavItem(
        route = Routes.Analytics,
        iconResId = com.receiptkeeper.R.drawable.ic_bottom_analytics,
        label = "Analytics"
    ),
    BottomNavItem(
        route = Routes.Settings,
        iconResId = com.receiptkeeper.R.drawable.ic_bottom_settings,
        label = "Settings"
    )
)
