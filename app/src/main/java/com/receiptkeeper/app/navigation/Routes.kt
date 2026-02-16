package com.receiptkeeper.app.navigation

/**
 * Navigation routes for the app
 * Sealed class ensures type-safety for navigation
 */
sealed class Routes(val route: String) {

    // Main bottom navigation tabs
    data object Books : Routes("books")
    data object Receipts : Routes("receipts")
    data object Scan : Routes("scan")
    data object Analytics : Routes("analytics")
    data object Settings : Routes("settings")

    // Detail screens
    data object BookDetail : Routes("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }

    data object ReceiptDetail : Routes("receipt_detail/{receiptId}") {
        fun createRoute(receiptId: Long) = "receipt_detail/$receiptId"
    }

    // Settings sub-screens
    data object Vendors : Routes("vendors")
    data object Categories : Routes("categories")
    data object PaymentMethods : Routes("payment_methods")
    data object SpendingGoals : Routes("spending_goals")

    companion object {
        // List of bottom navigation destinations
        val bottomNavRoutes = listOf(
            Books,
            Receipts,
            Scan,
            Analytics,
            Settings
        )
    }
}
