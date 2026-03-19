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
import androidx.navigation.NavController

private val userManualContent = """
RECEIPTKEEPER USER MANUAL
═══════════════════════════════════════
Version 2.1  |  Updated March 2026

ReceiptKeeper is a fully offline receipt management app. Capture receipts with your camera, let OCR extract the key details automatically, organize them into books, track spending across categories, and keep everything backed up — all without any cloud account or network connection.

───────────────────────────────────────
QUICK START
───────────────────────────────────────

1. Open the Books tab and tap + to create your first book
   (e.g., "Groceries", "Business", "Home")
2. Go to the Scan tab to photograph a receipt
3. Review the auto-extracted fields and tap Save
4. Open the Receipts tab to see your receipt, or Analytics to see spending charts

That's the core loop. Everything else builds on it.

───────────────────────────────────────
BOTTOM NAVIGATION — 5 TABS
───────────────────────────────────────

📚 BOOKS
   Organize receipts into named collections.

🧾 RECEIPTS
   See every receipt across all books in one list.

📸 SCAN
   Photograph a receipt and let OCR fill in the fields.

📊 ANALYTICS
   Charts, category breakdowns, and spending goal tracking.

⚙️ SETTINGS
   Vendors, categories, payment methods, backup, and this manual.

───────────────────────────────────────
📚  BOOKS
───────────────────────────────────────

Books let you group receipts by purpose — a work trip, a home project, monthly household expenses, etc.

CREATING A BOOK
  1. Tap + (floating button, bottom-right)
  2. Enter a name (required) and optional description
  3. Tap Save

MANAGING BOOKS
  • Edit — tap the pencil icon on a book card
  • Delete — tap the trash icon
    ⚠ Deleting a book permanently deletes all its receipts
  • Sort — tap the sort icon in the top bar to toggle between
    sort by name and sort by receipt count

BOOK DETAIL VIEW
  • All receipts in the book, grouped by date
  • Tap a date header to expand / collapse that day
  • Date headers show the daily total on the right
  • Tap any receipt row to open its full detail screen

───────────────────────────────────────
🧾  RECEIPTS
───────────────────────────────────────

The Receipts tab shows every receipt in the database, newest first.

DATE GROUPING
  Receipts are grouped under date headers. Tap a header to
  expand or collapse that day's receipts. The header shows
  the day's total spending on the right.

TOTAL SPENDING CARD
  A card at the top shows the running total for the currently
  visible (filtered / searched) receipts.

SEARCHING  (🔍 icon in top bar)
  Tap the magnifying glass to open the search field.
  The search runs instantly across ALL receipt fields:
    • Vendor name
    • Category name
    • Book name
    • Payment method name
    • Notes
    • OCR extracted text
    • Amount (e.g. type "12.5" to find $12.50)
    • Date (e.g. type "2026-02")
  Tap ✕ inside the field to clear the query.
  Tap the icon again (now shows ✕) to close search entirely.
  Search and the book filter work together simultaneously.

BOOK FILTER  (funnel icon in top bar)
  Tap the funnel to filter by a specific book.
  A filled / colored funnel means a filter is active.

ADDING A RECEIPT MANUALLY  (+ icon in top bar)
  Fill in:
    • Vendor — choose from dropdown or type a new name
    • Amount — decimal format, e.g. 12.34
    • Date — tap to open date picker
    • Book — required, choose from dropdown
    • Category — required, choose from dropdown
    • Payment Method — optional
    • Notes — optional free-form text
    • Image — optional, pick from gallery
  Tap Save.

RECEIPT ACTIONS
  • Tap card → open detail screen
  • Pencil icon → edit
  • Trash icon → delete (with confirmation)
  • Image thumbnail → full-screen image viewer

RECEIPT DETAIL SCREEN
  • Full-size image at the top — tap it to open the full-screen viewer
  • All metadata: vendor, category, book, payment method, date
  • Notes and raw OCR text (if scanned)
  • Download button saves the image to your Downloads folder
  • Back arrow returns to the list

FULL-SCREEN IMAGE VIEWER
  • Pinch with two fingers to zoom in (up to 8×)
  • Drag to pan when zoomed in
  • Pinch back out to return to fit-screen view
  • Tap the download icon (top-right) to save the image to gallery
  • Tap ✕ to close

───────────────────────────────────────
📸  SCAN (OCR)
───────────────────────────────────────

STEP 1 — CAMERA
  Point the camera at the receipt, ensure it is flat and
  well-lit, then tap the large circular shutter button.

STEP 2 — PREVIEW
  Review the captured image. Choose one of:
    ✅ Process with OCR — recommended
    ✏️  Skip OCR, enter manually
    🔄 Retake photo

STEP 3 — EDIT & SAVE
  OCR pre-fills these fields (all editable):
    • Vendor name
    • Total amount
    • Transaction date
    • Card last 4 digits (if visible on receipt)
  You must also select:
    • Book (required)
    • Category (required)
  Optionally add:
    • Payment method
    • Notes
  Expand "Raw OCR Text" to see exactly what was extracted.
  Tap Save. You are taken directly to the new receipt's detail screen.

WHAT OCR LOOKS FOR
  • Vendor  — prominent text in the top section of the receipt
  • Date    — patterns: MM/DD/YYYY · DD-MM-YYYY · YYYY-MM-DD
              also 2-digit years (MM/DD/YY)
  • Amount  — lines containing "total" followed by a dollar amount
              also handles trailing-dash negatives (e.g. 12.34-)
  • Card    — text containing "xxxx", "****", or "card" near 4 digits

TIPS FOR BEST OCR RESULTS
  ✅ Even, bright lighting — avoid shadows across the text
  ✅ Lay the receipt flat; creases cause blur
  ✅ Fill the camera frame with the receipt
  ✅ Keep the phone steady while shooting
  ✅ Always review and correct the extracted fields before saving

───────────────────────────────────────
📊  ANALYTICS
───────────────────────────────────────

DATE RANGE
  Choose a quick range: This Month · Last 30 Days ·
  Last 90 Days · This Year
  Or tap the calendar icon for a custom start and end date.

BOOK FILTER
  The dropdown at the top of the screen limits all charts and
  totals to a single book, or shows all books combined.

SPENDING TREND CHART
  A line chart of cumulative spending over the selected period.
  Red solid line = actual spending.
  Green dashed line = goal projection (when a goal is set).

TOTAL SPENDING SUMMARY
  Total amount and receipt count for the selected period and book.

SPENDING GOALS
  Progress bars for each goal you have set.
  Green = under budget. Red = over budget.
  Shows amount spent vs. goal amount and percentage used.

CATEGORY BREAKDOWN
  Switch between Pie, Bar, and Treemap views using the icons.
  Each slice / bar / tile is color-coded to match the category color
  you assigned in Settings → Categories.

VENDOR BREAKDOWN
  Same chart options (Pie / Bar / Treemap) for top vendors.

CSV EXPORT
  1. Set your desired date range and book filter
  2. Tap the share icon (top-right of Analytics screen)
  3. The system share sheet opens — choose email, Drive, etc.
  The exported file is named receipts_export_YYYYMMDD.csv and
  contains all fields: date, vendor, category, amount, book,
  payment method, notes, image filename.

───────────────────────────────────────
⚙️  SETTINGS
───────────────────────────────────────

VENDORS
  Add, rename, or delete vendor names.
  For well-known chains, ReceiptKeeper automatically shows a
  brand logo instead of a generic icon.
  You can also upload a custom icon for any vendor.
  Vendor names appear in the dropdown when adding or editing receipts.

CATEGORIES
  8 built-in categories cannot be deleted:
    Food · Grocery · Hardware · Entertainment
    Transportation · Utilities · Healthcare · Other
  You can add custom categories with a custom name, color, and icon.
  Colors appear throughout the app on charts and list items.

ICON THEME
  Toggle between colorful icons and monochrome icons.
  The choice applies globally across all screens.

PAYMENT METHODS
  Create payment methods of type: Cash, Credit Card, Debit Card, Other.
  For cards, you can store the last 4 digits.
  The appropriate icon is shown automatically.

SPENDING GOALS
  Create one or more goals:
    • Period — Daily, Weekly, Monthly, or Yearly
    • Scope  — overall spending or a specific category
    • Amount — your target limit
  Progress appears on the Analytics screen in real time.

BACKUP & RESTORE
  (See full details in the Backup section below.)

USER MANUAL
  This document. Always up to date.

ABOUT
  Shows the app version and build number.

───────────────────────────────────────
💾  BACKUP & RESTORE
───────────────────────────────────────

All backup files are stored locally. No network is needed.
Location: Downloads/雪松堡账本/

AUTOMATIC DAILY BACKUP
  Runs at 5:00 AM every day via WorkManager.
  File: Downloads/雪松堡账本/DailyBackup/daily_backup.zip
  Each run overwrites the previous daily backup file.
  Contents of the zip:
    • receipt_keeper_backup.db  (SQLite database export)
    • images/                   (all receipt image files)
    • backup_info.txt           (timestamp and metadata)

  If the auto-backup is not running, check:
    1. "Automatic Daily Backup" toggle is ON
    2. Storage permission is granted
    3. Battery optimization is not blocking the app
       (tap "Battery Optimization" in the backup screen)
  Use "Test WorkManager Backup" to trigger an immediate test run.

MANUAL BACKUP
  Tap "Create Backup Now" at any time.
  File: Downloads/雪松堡账本/receipt_keeper_backup_YYYYMMDD_HHmmss.zip
  Same contents as the daily backup.

EMERGENCY BACKUP BEFORE RESTORE
  Every time you restore a backup, the app automatically creates
  a full backup of your current data first.
  File: Downloads/雪松堡账本/receipt_keeper_backup_emergency_YYYYMMDD_HHmmss.zip
  If the emergency backup fails, the restore is aborted — your
  data is never overwritten unless the safety copy succeeded.

RESTORING FROM A BACKUP
  1. Tap any backup entry in the list to select it
  2. Tap Restore in the confirmation dialog
  3. The emergency backup is created automatically
  4. The database and images are restored
  5. Force-close the app and reopen it to complete the restore
  ⚠ Restoring replaces ALL current data with the backup's data.

DELETING A BACKUP
  Swipe or tap the trash icon on any backup entry.
  The daily_backup.zip entry is re-created by the next scheduled run.

RECOMPRESS IMAGES TO WEBP
  Found in the backup screen. Converts any legacy JPEG/PNG receipt
  images in the database to WebP format to save storage space.
  Rotation (EXIF orientation) is preserved during conversion.

MIGRATING TO A NEW DEVICE
  1. Create a manual backup on the old device
  2. Copy the ZIP file to the new device (USB, email, cloud, etc.)
  3. Install ReceiptKeeper on the new device
  4. Open Settings → Backup & Restore and place the ZIP file in
     Downloads/雪松堡账本/
  5. Select it from the backup list and tap Restore
  6. Restart the app

───────────────────────────────────────
🔒  PRIVACY
───────────────────────────────────────

  • All data is stored only on your device
  • No accounts, no cloud sync, no external servers
  • No network permission is required for any core feature
  • Camera permission is needed for the Scan tab
  • Storage permission is needed to write backup files
  • You own your data — export or delete it at any time

───────────────────────────────────────
🔧  TROUBLESHOOTING
───────────────────────────────────────

OCR extracts wrong or missing data
  → Check lighting and receipt flatness
  → Zoom in — fill the frame with the receipt
  → All OCR fields are editable before saving
  → Use "Skip OCR" and enter manually if needed

Backup not running automatically
  → Verify the toggle is enabled in Backup & Restore
  → Grant storage permission if prompted
  → Tap "Test WorkManager Backup" to verify the scheduler
  → Disable battery optimization for ReceiptKeeper

Images appear rotated
  → This was a known issue; it is fixed in v2.1
  → Use "Recompress Images to WebP" to re-process old images

Analytics shows no data
  → Confirm the date range includes your receipts
  → Check whether a book filter is hiding receipts
  → Verify receipts have been saved successfully

App is slow with many receipts
  → Use books to split large collections
  → Export older periods to CSV and delete them from the app
  → Use "Recompress Images to WebP" to reduce image storage

Database appears empty after restore
  → Force-close the app completely and reopen it
  → If still empty, the restore may have failed — check that the
    emergency backup file exists in Downloads/雪松堡账本/

───────────────────────────────────────
❓  FREQUENTLY ASKED QUESTIONS
───────────────────────────────────────

Q: Is my data backed up automatically?
A: Yes, once daily at 5 AM if the automatic backup toggle is on.

Q: Can I use the app with no internet?
A: Yes. All features are fully offline.

Q: Can I accidentally lose data when restoring?
A: No. An emergency backup is always created before any restore.
   If the emergency backup fails, the restore is cancelled.

Q: How do I move my data to a new phone?
A: Create a manual backup → copy the ZIP to the new phone →
   install the app → restore from the backup file.

Q: Can I open the CSV export in Excel?
A: Yes. The CSV uses standard comma-separated format with
   quoted fields, compatible with Excel, Google Sheets, etc.

Q: What happens if I delete the app?
A: App data is deleted. Backup ZIP files in Downloads/雪松堡账本/
   are NOT deleted (they are in external storage).
   Reinstall the app and restore from a backup.

Q: Can I zoom in on receipt images?
A: Yes. Tap the image to open the full-screen viewer, then
   pinch with two fingers to zoom up to 8× and drag to pan.

Q: Can the OCR handle non-English receipts?
A: The current OCR engine (ML Kit Latin) works best with
   English and other Latin-alphabet languages.

───────────────────────────────────────
📋  VERSION HISTORY
───────────────────────────────────────

v2.1 — March 2026
  ✅ Full-text search on Receipts screen (vendor, category,
     book, payment, notes, OCR text, amount, date)
  ✅ Pinch-to-zoom and pan in full-screen image viewer (up to 8×)
  ✅ Emergency backup auto-created before every restore
  ✅ EXIF orientation preserved when converting images to WebP
  ✅ Emergency backup files labelled with "emergency" in filename

v2.0 — February 2026
  ✅ Automatic daily backup via WorkManager
  ✅ Book-specific analytics filtering
  ✅ Spending trend chart with goal projection line
  ✅ Treemap chart view for categories and vendors
  ✅ Icon theme toggle (colorful vs monochrome)
  ✅ Recompress images to WebP

───────────────────────────────────────
📱  APP INFORMATION
───────────────────────────────────────

Minimum Android : 8.0 (API 26)
Target Android  : 15 (API 35)
Package         : com.receiptkeeper
Support         : GitHub Issues

Thank you for using ReceiptKeeper.
""".trimIndent()

/**
 * Settings screen - app configuration and management screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToVendors: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToIconTheme: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToSpendingGoals: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {},
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
                icon = Icons.Default.Palette,
                title = "Icon Theme",
                subtitle = "Switch between colorful and monochrome icons",
                onClick = onNavigateToIconTheme
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
                icon = Icons.Default.Backup,
                title = "Backup & Restore",
                subtitle = "Backup database and restore from backup",
                onClick = onNavigateToBackupRestore
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

            // Debug menu item (only in debug builds)
            if (BuildConfig.DEBUG) {
                HorizontalDivider()
                
                SettingsItem(
                    icon = Icons.Default.BugReport,
                    title = "Debug Helper",
                    subtitle = "Add test data for debugging",
                    onClick = onNavigateToDebug
                )
            }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserManualDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("User Manual") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = userManualContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
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

