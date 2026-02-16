package com.receiptkeeper.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.receiptkeeper.features.analytics.AnalyticsScreen
import com.receiptkeeper.features.books.BooksScreen
import com.receiptkeeper.features.receipts.ReceiptsScreen
import com.receiptkeeper.features.scan.ScanScreen
import com.receiptkeeper.features.settings.SettingsScreen

/**
 * Main navigation graph for the app
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Books.route,
        modifier = modifier
    ) {
        // Bottom navigation screens
        composable(route = Routes.Books.route) {
            BooksScreen(
                onNavigateToBookDetail = { bookId ->
                    navController.navigate(Routes.BookDetail.createRoute(bookId))
                }
            )
        }

        composable(route = Routes.Receipts.route) {
            ReceiptsScreen(
                onNavigateToReceiptDetail = { receiptId ->
                    navController.navigate(Routes.ReceiptDetail.createRoute(receiptId))
                }
            )
        }

        composable(route = Routes.Scan.route) {
            ScanScreen(
                onReceiptScanned = { receiptId ->
                    // Navigate to receipt detail after successful scan
                    navController.navigate(Routes.ReceiptDetail.createRoute(receiptId)) {
                        // Pop back to receipts, removing scan from backstack
                        popUpTo(Routes.Receipts.route)
                    }
                }
            )
        }

        composable(route = Routes.Analytics.route) {
            AnalyticsScreen()
        }

        composable(route = Routes.Settings.route) {
            SettingsScreen(
                onNavigateToVendors = {
                    navController.navigate(Routes.Vendors.route)
                },
                onNavigateToCategories = {
                    navController.navigate(Routes.Categories.route)
                },
                onNavigateToPaymentMethods = {
                    navController.navigate(Routes.PaymentMethods.route)
                },
                onNavigateToSpendingGoals = {
                    navController.navigate(Routes.SpendingGoals.route)
                }
            )
        }

        // Detail screens (to be implemented in later phases)
        composable(
            route = Routes.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) {
            // TODO: Phase 3 - BookDetailScreen
        }

        composable(
            route = Routes.ReceiptDetail.route,
            arguments = listOf(navArgument("receiptId") { type = NavType.LongType })
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getLong("receiptId") ?: return@composable
            com.receiptkeeper.features.receipts.ReceiptDetailScreen(
                receiptId = receiptId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings sub-screens
        composable(route = Routes.Vendors.route) {
            com.receiptkeeper.features.settings.VendorsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Routes.Categories.route) {
            com.receiptkeeper.features.settings.CategoriesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Routes.PaymentMethods.route) {
            com.receiptkeeper.features.settings.PaymentMethodsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Routes.SpendingGoals.route) {
            // TODO: Phase 6
            androidx.compose.material3.Text("Spending Goals Screen - Phase 6")
        }
    }
}
