# ReceiptKeeper 📱

A powerful Android receipt scanning and management app with OCR capabilities, built with Jetpack Compose and ML Kit.

**Track expenses • Scan receipts • Analyze spending • Export data**

---

## ✨ Features

### 📸 Receipt Scanning with OCR
- **Camera Integration**: Capture receipt photos directly in-app
- **ML Kit OCR**: Automatic text extraction from receipt images
- **Smart Parsing**: Auto-detects vendor, date, amount, and card last 4 digits
- **Manual Override**: Edit all extracted fields before saving
- **Image Storage**: Securely stores receipt images in app directory

### 📚 Book Management
- **Organize by Books**: Group receipts into folders (e.g., "Personal", "Business", "Travel")
- **Book Details**: View all receipts and total spending per book
- **Quick Access**: Tap any book to see its receipts
- **CRUD Operations**: Create, edit, delete books with descriptions

### 🧾 Receipt Management
- **Multiple Entry Methods**:
  - Scan with camera + OCR
  - Manual entry with form
  - Photo picker from gallery
- **Rich Metadata**:
  - Vendor name (with smart dropdown)
  - Amount and date
  - Category with color coding
  - Book assignment
  - Payment method
  - Notes
- **Receipt Details**: Tap-to-enlarge images, full metadata view
- **Edit & Delete**: Full CRUD operations on all receipts
- **Filtering**: Filter receipts by book

### 📊 Analytics & Insights
- **Date Range Selection**: View spending for any period
  - This Month
  - Last Month
  - Last 30/90 Days
  - This Year
  - All Time
  - Custom ranges
- **Total Spending**: See total and receipt count for selected period
- **Category Breakdown**: Visual charts showing spending by category
  - Progress bars with percentages
  - Color-coded categories
  - Amount per category

### 🎯 Spending Goals
- **Budget Tracking**: Set spending goals and track progress
- **Flexible Periods**: Daily, Weekly, Monthly, or Yearly goals
- **Category-Specific**: Set goals for specific categories or globally
- **Visual Progress**:
  - Progress bars showing spent vs. goal
  - Percentage completion
  - Amount remaining
  - **Over-budget warnings** with red styling
- **Goal Management**: Create, edit, delete goals in Settings

### 📤 Data Export
- **CSV Export**: Export receipts to CSV format
- **Date Range Filter**: Export specific periods
- **Complete Data**: Includes all fields (Date, Vendor, Category, Book, Payment Method, Amount, Notes)
- **Easy Sharing**: System share sheet integration
  - Email as attachment
  - Save to Google Drive
  - Share to any app

### ⚙️ Settings & Customization
- **Vendors Management**: Add, edit, delete vendor names
- **Categories**:
  - 8 default categories (Food, Grocery, Hardware, etc.)
  - Custom categories with color picker
  - 10 predefined colors
- **Payment Methods**:
  - Cash, Credit Card, Debit Card, Other
  - Optional last 4 digits for cards
- **Spending Goals**: Manage budget goals

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 26+ (Android 8.0+)
- Physical device or emulator for testing
- JDK 17 or later

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/greatfog7242/ReceiptKeeper.git
   cd ReceiptKeeper
   ```

2. **Open in Android Studio:**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Build and Run:**
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

   Or simply click the "Run" button in Android Studio.

---

## 📖 User Guide

### First Time Setup

1. **Launch the App**: ReceiptKeeper opens to the Books screen
2. **Create Your First Book**:
   - Tap the "+" FAB button
   - Enter name (e.g., "Personal Expenses")
   - Optional: Add description
   - Tap "Save"

3. **Set Up Categories** (Optional):
   - Go to Settings → Categories
   - Default categories are pre-loaded
   - Add custom categories if needed

### Scanning a Receipt

1. **Navigate to Scan Tab**: Tap the camera icon in bottom navigation
2. **Grant Camera Permission**: Allow when prompted (first time only)
3. **Capture Receipt**:
   - Point camera at receipt
   - Ensure text is clear and well-lit
   - Tap the large camera button
4. **Process with OCR**:
   - Tap "Process with OCR" (or "Skip OCR" for manual entry)
   - Wait 2-3 seconds for text extraction
5. **Review & Edit**:
   - **Vendor**: Select from dropdown or type new name
   - **Amount**: Verify extracted amount (editable)
   - **Date**: Check/edit date (YYYY-MM-DD format)
   - **Book**: Select which book to file in ⚠️ Required
   - **Category**: Choose expense category ⚠️ Required
   - **Payment Method**: Optional
   - **Notes**: Add any additional details
6. **Save**: Tap "Save Receipt"
7. **Navigate to Details**: Automatically goes to receipt detail screen

### Adding a Receipt Manually

1. **Go to Receipts Tab**: Tap the receipt icon
2. **Tap FAB (+)**: Opens manual entry form
3. **Select Image** (Optional):
   - Tap "Select Image from Gallery"
   - Choose photo from your device
4. **Fill in Details**:
   - Vendor, Amount, Date ⚠️ Required
   - Book, Category ⚠️ Required
   - Payment Method, Notes (Optional)
5. **Save**: Tap "Save"

### Viewing Receipts

**By Book:**
1. Go to Books tab
2. Tap any book card
3. See all receipts in that book with total spending

**All Receipts:**
1. Go to Receipts tab
2. View all receipts across all books
3. Use book filter dropdown to filter by specific book

**Receipt Details:**
- Tap any receipt to see full details
- View full-size image (tap image to enlarge)
- See all metadata

### Editing & Deleting

**Edit Receipt:**
1. In Receipts tab or Book detail
2. Tap edit icon (pencil) on receipt
3. Modify any field
4. Tap "Save"

**Delete Receipt:**
1. Tap delete icon (trash) on receipt
2. Confirm deletion
3. Receipt and image are permanently removed

**Manage Books:**
1. In Books tab, tap edit/delete icons on book card
2. ⚠️ Deleting a book deletes all its receipts (cascade delete)

### Using Analytics

1. **Navigate to Analytics Tab**: Tap the chart icon
2. **Select Date Range**:
   - Tap "Quick Select" button
   - Choose preset (This Month, Last 30 Days, etc.)
   - Or use custom date range
3. **View Insights**:
   - Total spending for period (large card at top)
   - Receipt count
   - Spending by category (visual breakdown)
   - Spending goals progress (if goals are set)

### Setting Spending Goals

1. **Go to Settings → Spending Goals**
2. **Add New Goal**:
   - Tap FAB (+)
   - Set budget amount (e.g., $500)
   - Choose period (Daily/Weekly/Monthly/Yearly)
   - Select category (or "All Categories" for global goal)
   - Tap "Save"
3. **View Progress**:
   - Return to Analytics tab
   - Scroll to "Spending Goals" section
   - See progress bars showing:
     - Spent amount
     - Remaining/Over by amount
     - Percentage
     - ⚠️ Red warning if over budget

### Exporting Data

1. **Go to Analytics Tab**
2. **Select Date Range**: Choose which receipts to export
3. **Tap Share Icon** (📤 top-right corner)
4. **Choose Export Destination**:
   - Gmail: Email as CSV attachment
   - Google Drive: Save to cloud
   - Files: Save locally
   - Any other app
5. **File Format**: Standard CSV with headers
   - Opens in Excel, Google Sheets, Numbers, etc.
   - Contains: Date, Vendor, Category, Book, Payment Method, Amount, Notes

### Managing Settings

**Vendors:**
- Settings → Vendors
- Add commonly used vendors
- Edit names for consistency
- Delete unused vendors

**Categories:**
- Settings → Categories
- Default categories included
- Add custom categories
- Choose from 10 colors
- Cannot delete default categories

**Payment Methods:**
- Settings → Payment Methods
- Add cards, cash, etc.
- Specify type (Cash/Credit/Debit/Other)
- Optional: Add last 4 digits
- Edit or delete anytime

---

## 🏗️ Architecture

**Pattern:** MVVM with Clean Architecture

```
app/
├── core/                      # Core utilities
│   ├── database/             # Room database
│   ├── di/                   # Hilt dependency injection
│   └── util/                 # Utilities (ImageHandler, CsvExporter)
├── data/                      # Data layer
│   ├── local/
│   │   ├── entity/          # Room entities
│   │   └── dao/             # Data Access Objects
│   ├── repository/          # Repository implementations
│   └── mapper/              # Entity ↔ Domain mappers
├── domain/                    # Domain layer
│   └── model/               # Domain models (pure Kotlin)
├── features/                  # Feature modules
│   ├── books/               # Book management
│   ├── receipts/            # Receipt CRUD
│   ├── scan/                # Camera & OCR
│   ├── analytics/           # Analytics & reporting
│   └── settings/            # Settings screens
└── ui/                        # Shared UI components
    ├── theme/               # Material 3 theme
    └── components/          # Reusable components
```

---

## 🛠️ Technology Stack

### Core
- **Kotlin** - Primary language
- **Jetpack Compose** - Modern declarative UI
- **Material 3** - Material Design components
- **Hilt** - Dependency injection
- **Kotlin Coroutines + Flow** - Asynchronous programming

### Database
- **Room** - Local SQLite database
- **Type Converters** - Instant, LocalDate support
- **Foreign Keys** - Cascade deletes

### Camera & OCR
- **CameraX** - Camera integration
- **ML Kit Text Recognition** - OCR processing
- **Accompanist Permissions** - Permission handling

### Image Handling
- **Coil** - Efficient image loading & caching
- **FileProvider** - Secure file sharing

### Navigation
- **Jetpack Navigation Compose** - Single-activity architecture
- **Type-safe routes** - Compile-time navigation safety

---

## 📱 Minimum Requirements

- **Min SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 35 (Android 15)
- **Compile SDK:** 35
- **Camera:** Required for scanning (optional feature)
- **Storage:** ~50MB for app + images

---

## 🧪 Building & Testing

### Debug Build
```bash
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Install on Device
```bash
# Debug variant (package: com.receiptkeeper.debug)
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.receiptkeeper.debug/com.receiptkeeper.app.MainActivity

# Check logs
adb logcat -d *:E
```

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

---

## 📂 Database Schema

### Tables
- **receipts** - Main receipt records with foreign keys
- **books** - Organizational folders for receipts
- **categories** - Expense categories (8 defaults + custom)
- **vendors** - Store/merchant names
- **payment_methods** - Payment cards and methods
- **spending_goals** - Budget goals with periods

### Key Relationships
- Receipt → Book (many-to-one, CASCADE delete)
- Receipt → Vendor (many-to-one, optional)
- Receipt → Category (many-to-one, required)
- Receipt → PaymentMethod (many-to-one, optional)
- SpendingGoal → Category (many-to-one, optional)

---

## 🎨 Features Overview

| Feature | Screen | Description |
|---------|--------|-------------|
| 📚 Books | Books Tab | Organize receipts into folders |
| 🧾 Receipts | Receipts Tab | View, add, edit, delete receipts |
| 📸 Scan | Scan Tab | Camera + OCR for receipt capture |
| 📊 Analytics | Analytics Tab | Spending insights and charts |
| ⚙️ Settings | Settings Tab | Manage vendors, categories, payment methods, goals |
| 🎯 Goals | Settings → Goals | Budget tracking with progress |
| 📤 Export | Analytics → Share | CSV export with filtering |

---

## 🔒 Privacy & Security

- **Local Storage**: All data stored locally on device
- **No Cloud Sync**: No data leaves your device (unless you export)
- **Secure Images**: Images stored in app-private directory
- **No Analytics**: No tracking or data collection
- **FileProvider**: Secure file sharing with temporary access

---

## 🐛 Troubleshooting

### OCR Not Working
- Ensure good lighting when scanning
- Hold camera steady
- Try "Skip OCR" and enter manually
- Check camera permissions in Settings

### Images Not Showing
- Check storage permissions
- Verify images exist in app directory
- Try clearing app cache

### Export Button Disabled
- Add at least one receipt first
- Export button only works when receipts exist

### App Crashes on Launch
- Check device logs: `adb logcat -d *:E`
- Ensure Min SDK 26+ (Android 8.0+)
- Try uninstall and reinstall

---

## 📈 Roadmap

### Completed ✅
- Phase 0-6: All core features implemented
- Books, Receipts, Scanning, Analytics, Goals, Export

### Future Enhancements 🚀
- Cloud backup (Google Drive, Dropbox)
- Multi-currency support
- Recurring receipts
- Tax calculation
- Dark mode enhancements
- Widgets
- Wear OS support

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Commit Convention
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `refactor:` - Code refactoring
- `test:` - Adding tests
- `chore:` - Maintenance tasks

---

## 📄 License

This project is available under the MIT License. See LICENSE file for details.

---

## 👨‍💻 Development

### Project Setup for Contributors
1. Clone repository
2. Open in Android Studio
3. Sync Gradle
4. Read `CLAUDE.md` for architecture guidelines
5. Check `ai/progress.md` for development notes

### Development Tools
- **Android Studio** - IDE
- **Git** - Version control
- **Gradle** - Build system
- **Hilt** - Dependency injection

---

## 📞 Support

For issues, questions, or feature requests:
- Open an issue on GitHub
- Check existing issues first
- Provide device info and logs for bugs

---

## 🙏 Acknowledgments

- **ML Kit** by Google - OCR capabilities
- **CameraX** - Camera integration
- **Jetpack Compose** - Modern UI framework
- **Material Design 3** - UI components
- **Coil** - Image loading library

---

## 📊 Project Stats

- **Lines of Code:** ~10,000+
- **Kotlin Files:** 60+
- **Compose Screens:** 15+
- **Database Tables:** 6
- **Features Implemented:** 40+
- **Development Time:** Phases 0-6 complete
- **Build Time:** ~5s (incremental)

---

**Built with ❤️ using Jetpack Compose and Kotlin**

*ReceiptKeeper - Your Personal Expense Tracking Companion*
