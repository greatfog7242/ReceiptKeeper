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
📱 RECEIPTKEEPER - COMPLETE USER MANUAL

Version: 2.0.0 | Last Updated: March 2026

ReceiptKeeper is a comprehensive mobile application for scanning, organizing, analyzing, and backing up receipts. It uses advanced OCR (Optical Character Recognition) with ML Kit to automatically extract information from receipt images, helping you track spending across different categories, vendors, books, and time periods.

🔧 **KEY FEATURES**
• 📸 Smart OCR scanning with camera
• 📚 Book-based organization
• 📊 Advanced analytics with charts
• 💾 Automatic daily backups
• 🔄 CSV export functionality
• 🎯 Spending goal tracking
• 🏪 Vendor management with brand icons
• 🏷️ Custom category system

---

🚀 **GETTING STARTED**

**First Launch:**
✅ Database created automatically
✅ 8 default categories pre-loaded
✅ Daily backup scheduled for 5:00 AM
✅ Camera permissions requested (for scanning)

**Quick Start Guide:**
1. **Create your first book** (Books tab → + button)
2. **Add vendors** (Settings → Vendors → + button)
3. **Scan your first receipt** (Scan tab → camera)
4. **Set up spending goals** (Settings → Spending Goals)

---

📱 **NAVIGATION OVERVIEW**

**Bottom Navigation (5 Tabs):**

1. **📚 BOOKS**
   • Organize receipts into collections
   • Create books for different purposes
   • View receipts grouped by date within each book

2. **🧾 RECEIPTS**
   • View all receipts across all books
   • Filter by book using dropdown
   • Date-based grouping with expandable headers

3. **📸 SCAN**
   • Camera-based receipt scanning
   • OCR auto-extraction (vendor, amount, date, card)
   • Manual override for all fields

4. **📊 ANALYTICS**
   • Spending trends with charts
   • Category/vendor breakdowns
   • Book-specific analytics filtering
   • CSV export functionality

5. **⚙️ SETTINGS**
   • App configuration
   • Data management
   • Backup & restore
   • User manual (this document)

---

📚 **BOOKS - ORGANIZING RECEIPTS**

**What are Books?**
Books are collections that help organize receipts (e.g., "Personal Expenses", "Business Trips", "Home Renovation").

**Creating a Book:**
1. Navigate to **Books** tab
2. Tap **+** (Floating Action Button)
3. Enter:
   • Name (required)
   • Description (optional)
4. Tap **Save**

**Book Management:**
• **View book details**: Tap any book card
• **Edit book**: Tap pencil icon on book card
• **Delete book**: Tap trash icon (⚠️ deletes all receipts in book)
• **Sort books**: By name or receipt count

**Book Detail View:**
• Shows all receipts in the book
• **Date-based grouping** (like Receipts tab)
• **Total spending** for the book
• **No edit/delete buttons** on receipts (read-only view)
• Tap receipt to view full details

---

🧾 **RECEIPTS MANAGEMENT**

**Main Receipts Screen:**
• **Date-based grouping** with expandable headers
• **Book filter dropdown** in top app bar
• **Total spending card** at top
• **+ button** for manual entry

**Adding a Receipt (Manual):**
1. Tap **+** button on Receipts screen
2. Fill form:
   • **Vendor**: Select from dropdown or type new
    • **Amount**: $12.34 format (use decimal format)
   • **Date**: YYYY-MM-DD format
   • **Book**: Required (dropdown)
   • **Category**: Required (dropdown)
   • **Payment Method**: Optional (dropdown)
   • **Notes**: Optional multi-line
   • **Image**: Optional (from gallery)
3. Tap **Save**

**Receipt List Item:**
• Vendor name with icon/brand logo
• Amount in bold primary color
• Category with colored icon
• Book name
• Date (MMM dd, yyyy format)
• Image thumbnail (if available)

**Receipt Actions:**
• **View details**: Tap receipt card
• **Edit**: Tap pencil icon on receipt card
• **Delete**: Tap trash icon on receipt card
• **View image**: Tap image thumbnail

**Receipt Detail Screen:**
• Full-size image (tap to enlarge)
• All receipt metadata
• Category color indicator
• OCR extracted text (if scanned)
• Edit/Delete buttons

---

📸 **SCANNING RECEIPTS WITH OCR**

**Three-Step Scanning Workflow:**

**Step 1: Camera Mode**
• Point camera at receipt
• Ensure good lighting
• Keep receipt flat and in focus
• Tap **capture button** (large circle)

**Step 2: Preview Mode**
• Review captured image
• Options:
   ✅ **Process with OCR** (recommended)
   ⏭️ **Skip OCR - Enter Manually**
   🔄 **Retake Photo**

**Step 3: Edit Mode**
• **Auto-extracted fields** (editable):
   - Vendor name (with dropdown selection)
   - Total amount
   - Transaction date
   - Card last 4 digits (if found)
• **Required fields**:
   - Book selection (dropdown)
   - Category selection (dropdown)
• **Optional fields**:
   - Payment method (dropdown)
   - Notes (multi-line)
• **OCR debug view**: Expand to see raw extracted text
• **Image preview**: Tap to view full-size

**OCR Extraction Patterns:**
• **Vendor**: First non-date line of text
• **Date**: MM/DD/YYYY, DD-MM-YYYY, YYYY-MM-DD
• **Amount**: "total" followed by $12.34 (e.g., $12.34)
• **Card**: "xxxx", "****", "card" patterns

**Scanning Tips:**
✅ Good, even lighting
✅ Receipt flat on surface
✅ Fill camera frame with receipt
✅ Hold device steady
✅ Clean lens for clear images

---

📊 **ANALYTICS & REPORTING**

**Date Range Selection:**
• **Quick ranges**: This Month, Last 30 Days, Last 90 Days, This Year
• **Custom range**: Tap calendar icon

**Book Filtering:**
• **Dropdown at top** of analytics screen
• "All Books" or select specific book
• All charts update based on selection

**Analytics Components:**

1. **📈 Spending Trend Chart**
   • Line chart showing accumulated spending over time
   • **Red line**: Actual spending
   • **Green dashed line**: Goal projection (if set)
   • **Legends on right side**
   • Y-axis: Dollar amounts
   • X-axis: Dates (MM/DD format)

2. **💰 Total Spending Card**
   • Total amount for selected period
   • Receipt count
   • Compared to spending goals

3. **🎯 Spending Goals Progress**
   • Visual progress bars
   • **Green**: Under budget
   • **Red**: Over budget
   • Percentage completion
   • Goal amount vs. actual spending

4. **📊 Category Breakdown**
   • Visual chart by category
   • Color-coded by category colors
   • Percentage distribution
   • Amount per category

**CSV Export:**
1. Select date range and book filter
2. Tap **Export** button (share icon)
3. System share sheet opens
4. Choose destination (Email, Drive, etc.)
5. **Files included**:
   • `receipts_export_YYYYMMDD.csv`
   • All receipt data in CSV format
   • Properly formatted fields with quotes

---

⚙️ **SETTINGS & CONFIGURATION**

**Settings Menu Items:**

1. **🏪 VENDORS**
   • Add/edit/delete vendors
   • **Brand icons**: Auto-detected for major chains
   • **Custom icons**: Upload your own
   • Vendor names used in dropdowns throughout app

2. **🏷️ CATEGORIES**
   • **8 default categories** (cannot be deleted):
     - Food 🍕, Grocery 🛒, Hardware 🔨
     - Entertainment 🎬, Transportation 🚗
     - Utilities 💡, Healthcare 🏥, Other 📦
   • **Add custom categories**:
     - Name, color selection, icon
   • Color-coded throughout app

3. **💳 PAYMENT METHODS**
   • Types: Cash, Credit Card, Debit Card, Other
   • Add last 4 digits for cards
   • Icons based on payment type

4. **🎯 SPENDING GOALS**
   • **Periods**: Daily, Weekly, Monthly, Yearly
   • **Scope**: Overall or category-specific
   • **Amount**: Target spending limit
   • **Progress tracking**: Real-time in Analytics

5. **💾 BACKUP & RESTORE** ⭐ **NEW**
   • **Automatic Daily Backup**:
     - Runs at 5:00 AM daily
     - Saves to: `Downloads/雪松堡账本/DailyBackup/`
     - Includes: Database + all images
     - Creates: `daily_backup.zip`
   • **Manual Backup**:
     - Tap "Create Backup Now"
     - Timestamped folder in `Downloads/雪松堡账本/`
   • **Restore**:
     - Select from backup list
     - Confirmation dialog
     - ⚠️ Requires app restart
   • **Test WorkManager Backup**:
     - Immediate test of backup system
     - Verifies WorkManager scheduling

6. **📖 USER MANUAL**
   • This document
   • Always accessible
   • Updated with new features

---

💾 **BACKUP SYSTEM - DETAILED GUIDE**

**Automatic Daily Backup:**
• **Schedule**: 5:00 AM daily
• **Location**: `Downloads/雪松堡账本/DailyBackup/daily_backup.zip`
• **Contents**:
   - `receipt_keeper_backup.db` (database export)
   - `images/` folder (all receipt images)
   - `backup_info.txt` (metadata)
• **Overwrites**: Previous daily backup
• **Requirements**: No network needed

**Manual Backup:**
• **Location**: `Downloads/雪松堡账本/YYYYMMDD_HHMMSS/`
• **File**: `receipt_keeper_backup_YYYYMMDD_HHMMSS.zip`
• **Same contents** as automatic backup

**Backup Contents:**
1. **Database Export**: Uses SQLite `VACUUM INTO` for clean copy
2. **Images**: All `.jpg`, `.jpeg`, `.png`, `.webp` files from receipts
3. **Metadata**: Backup timestamp, file counts, sizes

**Restore Process:**
1. Select backup from list
2. Confirm restoration (⚠️ replaces current data)
3. App closes automatically
4. **Restart app** to complete restoration
5. Verify data integrity

**Testing Backups:**
• **Test WorkManager Backup** button
• Triggers immediate backup via WorkManager
• Check logs for status
• Verify `daily_backup.zip` creation

**Backup Locations:**
• **Primary**: External storage (Downloads folder)
• **Fallback**: App-specific storage (if external unavailable)
• **Android 11+**: May require permission grant

---

📈 **ADVANCED FEATURES**

**Book-Specific Analytics:**
• Filter analytics by book
• Compare spending across books
• Track project/program budgets separately

**Vendor Dropdowns:**
• Consistent vendor selection across app
• Prevents duplicates (Walmart vs WALMART vs walmart)
• Brand icon display in lists

**Date-Based Grouping:**
• Consistent across Books and Receipts tabs
• Expandable/collapsible date headers
• Date totals shown in headers
• Sorted newest-first

**Image Management:**
• Thumbnail generation
• Full-screen viewer with tap-to-close
• Efficient caching with Coil
• Storage in app-specific directory

**Form Validation:**
• Required fields highlighted
• Amount format validation
• Date format validation
• Vendor auto-creation

---

🔧 **TROUBLESHOOTING**

**Common Issues & Solutions:**

1. **OCR Not Extracting Data**
   • Ensure good lighting
   • Receipt should be flat
   • Text should be clear and legible
   • Use manual entry as fallback

2. **Backup Not Running**
   • Check "Automatic Daily Backup" is enabled
   • Verify storage permissions
   • Test with "Test WorkManager Backup"
   • Check battery optimization settings

3. **App Crashes**
   • Restart app
   • Check available storage
   • Update to latest version
   • Restore from backup if needed

4. **Images Not Loading**
   • Check storage permissions
   • Verify image files exist
   • Restart app
   • Check Coil cache settings

5. **Database Issues**
   • Use backup/restore
   • Export CSV as backup
   • Clear app data (last resort)

**Performance Tips:**
• Keep receipt count under 10,000 for optimal performance
• Regularly export CSV backups
• Use books to organize large collections
• Clear image cache if app slows down

---

🔒 **PRIVACY & SECURITY**

**Data Storage:**
• All data stored locally on device
• No cloud synchronization
• No external servers
• You control all data

**Permissions:**
• **Camera**: For scanning receipts
• **Storage**: For saving backups and images
• **No network permissions required**

**Backup Security:**
• Backups stored on device
• Optional manual export to cloud services
• No automatic cloud uploads
• You choose backup destinations

**Data Ownership:**
• You own 100% of your data
• Export anytime via CSV
• Delete anytime via app
• No data mining or sharing

---

🔄 **DATA EXPORT & MIGRATION**

**CSV Export:**
• Full data export from Analytics tab
• Compatible with Excel, Google Sheets, etc.
• Includes all receipt fields
• Image filenames included

**Backup Files:**
• Standard ZIP format
• Can be extracted on any device
• Database in SQLite format
• Images in original format

**Migration to New Device:**
1. Create manual backup on old device
2. Transfer backup file to new device
3. Install ReceiptKeeper on new device
4. Restore from backup
5. Verify data integrity

---

📞 **SUPPORT & FEEDBACK**

**Getting Help:**
• **User Manual**: This document (Settings → User Manual)
• **GitHub Repository**: Issue tracking and feature requests
• **In-app feedback**: Coming soon

**Feature Requests:**
• Submit via GitHub issues
• Include use case description
• Priority given to most requested features

**Bug Reports:**
• Describe steps to reproduce
• Include device information
• Screenshots if possible
• Error messages if any

---

📋 **QUICK REFERENCE**

**Keyboard Shortcuts (Web Version):**
• `Ctrl + N`: New receipt
• `Ctrl + F`: Filter receipts
• `Ctrl + E`: Export data
• `Ctrl + S`: Save receipt

**Icon Meanings:**
• 📚 Books: Collection of receipts
• 🧾 Receipt: Individual transaction
• 📸 Camera: Scan receipt
• 📊 Chart: Analytics
• ⚙️ Gear: Settings
• 💾 Floppy disk: Backup
• 🔄 Arrows: Restore
• 📤 Box with arrow: Export

**Color Coding:**
• **Primary**: Main actions, totals
• **Success**: Under budget, completed
• **Error**: Over budget, warnings
• **Surface**: Background elements
• **Variant**: Secondary elements

---

🎯 **BEST PRACTICES**

**Receipt Management:**
1. **Scan immediately** after purchase
2. **Categorize consistently** for accurate analytics
3. **Use books** for different expense types
4. **Review monthly** using Analytics tab
5. **Export quarterly** for external records

**Backup Strategy:**
1. **Enable automatic daily backups**
2. **Monthly manual backups** for archives
3. **Export CSV quarterly** for spreadsheet analysis
4. **Store backups** in multiple locations

**Analytics Review:**
1. **Weekly**: Check spending vs goals
2. **Monthly**: Category breakdown review
3. **Quarterly**: Vendor spending analysis
4. **Yearly**: Annual spending trends

---

🆕 **VERSION 2.0 HIGHLIGHTS**

**New in This Version:**
✅ **Automatic daily backups** with WorkManager
✅ **Book-specific analytics** filtering
✅ **Spending trend charts** with right-side legends
✅ **Enhanced BookDetailScreen** with date grouping
✅ **Test WorkManager Backup** button
✅ **Updated User Manual** (this document)
✅ **Improved backup/restore UI**
✅ **Better error handling** throughout app

**Coming Soon:**
• Multi-device sync
• Receipt search functionality
• Custom report generation
• Receipt sharing options
• Advanced OCR models

---

📄 **LICENSE & ATTRIBUTIONS**

**Open Source Components:**
• Jetpack Compose (UI framework)
• Room Database (local storage)
• ML Kit (OCR functionality)
• CameraX (camera integration)
• Coil (image loading)
• WorkManager (background tasks)

**License:**
• MIT License
• Free for personal and commercial use
• Source code available on GitHub

**Attributions:**
• Icons: Material Design Icons
• OCR: Google ML Kit
• Database: Android Room
• UI: Jetpack Compose

---

❓ **FREQUENTLY ASKED QUESTIONS**

**Q: Is my data backed up automatically?**
A: Yes, if "Automatic Daily Backup" is enabled (default). Backups run at 5:00 AM daily.

**Q: Can I use the app offline?**
A: Yes! All features work offline except for initial brand icon downloads.

**Q: How do I transfer data to a new phone?**
A: Create a manual backup, transfer the ZIP file, install app on new phone, then restore.

**Q: Are receipt images stored securely?**
A: Yes, images are stored in app-specific directory and included in encrypted backups.

**Q: Can I export data to Excel?**
A: Yes, use CSV export from Analytics tab. Files are compatible with Excel/Sheets.

**Q: What happens if I delete the app?**
A: All local data is deleted. Restore from backup after reinstalling.

**Q: Is there a web version?**
A: Not currently. Mobile-only for now.

---

🎉 **GETTING THE MOST FROM RECEIPTKEEPER**

**Pro Tips:**
1. **Consistency is key** - Scan receipts daily
2. **Use books wisely** - Separate personal/business
3. **Set realistic goals** - Start with monthly targets
4. **Regular reviews** - Weekly analytics check
5. **Multiple backups** - Local + cloud storage

**Success Stories:**
• Users save 15% annually by tracking spending
• Small businesses track deductible expenses
• Travelers organize trip receipts
• Homeowners track renovation costs

**Community:**
• Share tips on GitHub
• Request features
• Report bugs
• Help improve the app

---

📱 **APP INFORMATION**

**Version:** 2.0.0
**Minimum Android:** 8.0 (API 26)
**Target Android:** 15 (API 35)
**Package:** com.receiptkeeper
**Release Date:** March 2026
**Developer:** ReceiptKeeper Team
**Support:** GitHub Issues

**Thank you for using ReceiptKeeper!** 🎉

Your financial organization journey starts here. Scan, track, analyze, and save with confidence.

---
*This manual is always available in Settings → User Manual*
*Last updated: March 6, 2026*
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

