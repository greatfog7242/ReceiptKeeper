package com.receiptkeeper.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.receiptkeeper.BuildConfig

private val userManualContent = """
ReceiptKeeper - User Manual

Version: 1.0.0

ReceiptKeeper is a mobile application for scanning, organizing, and analyzing receipts. It uses OCR (Optical Character Recognition) to automatically extract information from receipt images, helping you track spending across different categories, vendors, and time periods.

---

GETTING STARTED

First Launch:
- The app will create a local database on your device
- Default categories will be automatically set up (Food, Grocery, Hardware, Entertainment, Transportation, Utilities, Healthcare, Other)

Adding Your First Receipt:
- Manual Entry: Tap the + button on the Receipts screen
- Scan a Receipt: Use the Scan tab to photograph a receipt

---

NAVIGATION

The app uses a bottom navigation bar with 5 main sections:

- Books: Organize receipts into collections
- Receipts: View all receipts
- Scan: Scan receipts with camera
- Analytics: View spending insights
- Settings: App configuration

---

BOOKS

Books help you organize receipts into separate collections (e.g., "Personal", "Business", "Travel").

Creating a Book:
1. Navigate to the Books tab
2. Tap the + button
3. Enter a name for the book
4. Optionally add a description
5. Tap Save

Managing Books:
- View receipts in a book: Tap on a book card
- Edit a book: Tap the edit icon
- Delete a book: Swipe left, then tap Delete
Note: Deleting a book will also delete all receipts within it

---

RECEIPTS

Viewing Receipts:
- Receipts are grouped by date
- Tap a date header to expand/collapse that group
- Each receipt shows: vendor name, category icon, amount, and thumbnail

Filtering by Book:
Use the dropdown menu at the top to filter receipts by book

Adding a Receipt Manually:
1. Tap the + (FAB) button on the Receipts screen
2. Fill in receipt details:
   - Vendor: Select or create new
   - Amount: Enter total amount
   - Date: Select transaction date
   - Book: Choose which book
   - Category: Select category
   - Payment Method (optional)
   - Notes (optional)
   - Image (optional): Attach from gallery
3. Tap Save

Editing/Deleting:
- Edit: Tap receipt, then tap Edit button
- Delete: Swipe left on receipt, or tap Delete in detail view

Viewing Receipt Details:
- Tap to see full-size image (tap to enlarge)
- Shows: vendor, category, book, payment method, date, amount, notes, OCR text

---

SCANNING RECEIPTS

The Scan feature uses OCR to automatically extract information from receipt photos.

How to Scan:
1. Navigate to the Scan tab
2. Point camera at the receipt
3. Tap capture button
4. Review captured image
5. Tap Use Photo (or retake)
6. Wait for OCR processing
7. Review and edit extracted information
8. Fill in remaining fields
9. Tap Save Receipt

OCR Tips:
- Ensure good lighting
- Keep receipt flat and in focus
- Entire receipt should be visible
- You can always manually correct any values

---

ANALYTICS

Date Range:
Select a time period at the top:
- This Week, This Month, This Year
- Last 7 Days, Last 30 Days, Last 90 Days
- Custom Range

Total Spending Card:
- Shows total spending for the period
- Shows number of receipts

Category Breakdown:
- Pie Chart, Stacked Bar Chart, Treemap views
- Shows spending by category with percentages

Vendor Breakdown:
- Similar to category, shows spending by vendor

Spending Goals:
- If set in Settings, shows progress in Analytics
- Visual progress bar with color indicator

---

SETTINGS

Vendors:
Manage stores/merchants. Add custom brand logos.

Categories:
- Default: Food, Grocery, Hardware, Entertainment, Transportation, Utilities, Healthcare, Other
- Add custom categories with color and icon

Payment Methods:
Track how you pay. Types: Cash, Credit Card, Debit Card, Other.

Spending Goals:
Set budget targets:
- Periods: Daily, Weekly, Monthly, Yearly
- Optional category filter (or overall goal)
- Progress shown in Analytics

---

EXPORTING DATA

How to Export:
1. Go to Analytics tab
2. Select date range
3. Tap Export button

Output:
- Creates folder: ReceiptKeeper_YYYYMMDD_HHMMSS
- Contains: receipts.csv and images/ folder

CSV Format:
- Date, Vendor, Category, Book, Payment Method, Amount, Notes, Image Filename

---

TIPS & TRICKS

Efficient Scanning:
- Good lighting for better OCR
- Flat, unfolded receipt
- Fill the camera frame
- Hold steady

Organizing:
- Use books for different purposes
- Consistent categories for accurate analytics
- Scan immediately after purchases

---

DATA PRIVACY

- All data stored locally on device
- No data sent to external servers
- Receipt images stay on device
- You have full control

---

For more details, visit the full user manual in the project repository.
""".trimIndent()

/**
 * Settings screen - app configuration and management screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToVendors: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToSpendingGoals: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showUserManualDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showUserManualDialog) {
        UserManualDialog(onDismiss = { showUserManualDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SettingsItem(
                icon = Icons.Default.Store,
                title = "Vendors",
                subtitle = "Manage store and merchant names",
                onClick = onNavigateToVendors
            )

            HorizontalDivider()

            SettingsItem(
                icon = Icons.Default.Category,
                title = "Categories",
                subtitle = "Manage expense categories",
                onClick = onNavigateToCategories
            )

            HorizontalDivider()

            SettingsItem(
                icon = Icons.Default.CreditCard,
                title = "Payment Methods",
                subtitle = "Manage payment cards and methods",
                onClick = onNavigateToPaymentMethods
            )

            HorizontalDivider()

            SettingsItem(
                icon = Icons.Default.TrendingUp,
                title = "Spending Goals",
                subtitle = "Set and track budget goals",
                onClick = onNavigateToSpendingGoals
            )

            HorizontalDivider()

            SettingsItem(
                icon = Icons.Default.MenuBook,
                title = "User Manual",
                subtitle = "How to use ReceiptKeeper",
                onClick = { showUserManualDialog = true }
            )

            HorizontalDivider()

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "Version and build information",
                onClick = { showAboutDialog = true }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Simple version footer
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "ReceiptKeeper",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoRow(label = "Version", value = BuildConfig.VERSION_NAME)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Build", value = BuildConfig.BUILD_NUMBER)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Receipt scanning and management app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun UserManualDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Manual",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                HorizontalDivider()

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = userManualContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
