# ReceiptKeeper Android App - Complete Implementation Plan

## Context

This plan implements a **complete Receipt Organizer Android application** from scratch using **Jetpack Compose** and modern Android 2026 best practices. The app enables users to scan receipts using their camera, automatically extract information via ML Kit OCR, organize receipts into books/folders, track spending by category, set budget goals, and export data to CSV.

**Current State:** Phase 0 in progress - SOP tracking artifacts created, Android project structure being set up.

**Why This Change:** Build a production-ready receipt management app with all features specified in featurelist.md.

**Development Methodology:** This project follows the **LONG-RUNNING ANDROID AGENT HARNESS (SOP)** which requires:
- State-driven development loop with mandatory tracking artifacts
- Build → Deploy → Verify cycle for every feature
- Features are NOT "Done" until installed and verified on device/emulator
- Progress tracking via `ai/feature_list.json` and `ai/progress.md`

---

## Architecture Overview

**Pattern:** MVVM with Clean Architecture principles
- **UI Layer:** Jetpack Compose screens + ViewModels (state management)
- **Domain Layer:** Business models (pure Kotlin classes)
- **Data Layer:** Room database + Repositories

**Tech Stack:**
- Jetpack Compose (Material 3)
- Room Database (local persistence)
- Hilt (dependency injection)
- ML Kit Text Recognition (OCR)
- CameraX (camera integration)
- Coil (image loading)
- Kotlin Coroutines + Flow (async/reactive)

---

## Database Schema

### Core Entities

**ReceiptEntity** (main entity)
- `id`, `bookId` (FK), `vendorId` (FK), `categoryId` (FK), `paymentMethodId` (FK)
- `totalAmount`, `transactionDate`, `notes`, `imageUri`, `extractedText`
- `createdAt`, `updatedAt`
- Foreign keys with CASCADE on delete for books

**BookEntity** - Organize receipts into folders
- `id`, `name`, `description`, `createdAt`, `updatedAt`

**CategoryEntity** - Expense categories (8 pre-seeded)
- `id`, `name`, `colorHex`, `isDefault`, `createdAt`
- Default categories: Food, Grocery, Hardware, Entertainment, Transportation, Utilities, Healthcare, Other

**VendorEntity** - Store/merchant names
- `id`, `name`, `createdAt`

**PaymentMethodEntity** - Payment types (Cash, Credit Card, Other)
- `id`, `name`, `type`, `lastFourDigits`, `createdAt`

**SpendingGoalEntity** - Budget goals
- `id`, `amount`, `period` (DAILY/WEEKLY/MONTHLY/YEARLY), `categoryId`, `createdAt`, `updatedAt`

---

## Implementation Phases (8 Phases)

### Phase 0: SOP Infrastructure Setup 🔧
**Goal:** Create tracking artifacts and project scaffolding

**Tasks:**
1. Create Android Studio project with Empty Compose Activity template
2. Set up `ai/` directory structure
3. Create `ai/init.sh` with environment validation
4. Generate `ai/feature_list.json` from featurelist.md (all phases/features)
5. Create `ai/progress.md` with initial entry
6. Create `CLAUDE.md` with:
   - Package name: `com.receiptkeeper`
   - Reference to agent-work-sop
   - Build/deploy commands
   - Project-specific conventions
7. Initialize git repository
8. Create `.gitignore` for Android
9. Verify emulator/device is connected (`adb devices`)
10. Run initial build and deploy to confirm setup

**Critical Files:**
- `ai/init.sh` - Environment validator
- `ai/feature_list.json` - Master task list
- `ai/progress.md` - Handoff log
- `CLAUDE.md` - AI agent instructions
- `settings.gradle.kts` - Project configuration
- `build.gradle.kts` - Root build file
- `app/build.gradle.kts` - App module build

**Success Criteria:**
- Empty Compose app builds successfully
- App installs and launches on emulator/device
- `sh ai/init.sh` passes all checks
- All tracking artifacts in place

**Status:** ✅ In Progress (tracking artifacts complete, creating project structure)

---

### Phase 1: Foundation & Database ⚙️
**Goal:** Working database with basic CRUD

**Tasks:**
1. Set up project structure and Gradle configuration
2. Add dependencies: Compose BOM, Room, Hilt, ML Kit, CameraX, Coil
3. Create Hilt application class and modules (AppModule, DatabaseModule)
4. Define all entity classes with relationships and indices
5. Create DAOs with Flow-based queries
6. Implement ReceiptDatabase with callback to seed default categories
7. Create repository layer for all entities
8. Write data mappers (Entity ↔ Domain models)
9. Unit test repositories and DAOs

**Critical Files:**
- `app/build.gradle.kts` - Dependencies
- `core/database/ReceiptDatabase.kt` - Database definition
- `data/local/entity/*Entity.kt` - All entities
- `data/local/dao/*Dao.kt` - All DAOs
- `data/repository/*Repository.kt` - Repository implementations
- `core/di/AppModule.kt` & `DatabaseModule.kt` - DI setup

**Success Criteria:** Can programmatically insert/query all entity types

---

### Phase 2: Basic UI Framework 🎨
**Goal:** Navigation and screen scaffolds

**Tasks:**
1. Create Navigation Routes (sealed class)
2. Set up NavHost in MainActivity with Compose Navigation
3. Create bottom navigation bar (5 tabs: Books, Receipts, **Scan**, Analytics, Settings)
4. Implement Material 3 theme (colors, typography)
5. Create empty scaffold screens for all main pages
6. Wire up navigation between screens
7. Add top app bars

**Critical Files:**
- `app/MainActivity.kt` - Main entry point
- `app/navigation/NavGraph.kt` - Navigation setup
- `app/navigation/Routes.kt` - Route definitions
- `app/navigation/BottomNavigation.kt` - Bottom tabs
- `ui/theme/Theme.kt`, `Color.kt`, `Type.kt` - Material 3 theme

**Success Criteria:** Can navigate through all screens via bottom tabs

---

### Phase 3: Books & Settings Management 📚
**Goal:** Complete CRUD for Books, Categories, Vendors, Payment Methods

**Tasks:**
1. Implement BooksScreen with LazyVerticalGrid
2. Create BookCard composable component
3. Add book creation/edit/delete functionality
4. Implement BookDetailScreen showing receipts in book
5. Create SettingsScreen hub with navigation to sub-screens
6. Implement VendorsScreen with list + CRUD operations
7. Create CategoriesScreen with color picker
8. Implement PaymentMethodsScreen with type selector
9. Add SwipeToDeleteItem shared component
10. Implement pull-to-refresh for all lists

**Critical Files:**
- `features/books/BooksScreen.kt` & `BooksViewModel.kt`
- `features/books/BookDetailScreen.kt`
- `features/settings/SettingsScreen.kt`
- `features/settings/VendorsScreen.kt`, `CategoriesScreen.kt`, `PaymentMethodsScreen.kt`
- `ui/components/SwipeToDeleteItem.kt`

**Success Criteria:** Full CRUD for books, vendors, categories, payment methods

---

### Phase 4: Basic Receipt Management 🧾
**Goal:** Manual receipt entry and viewing (without OCR)

**Tasks:**
1. Create ReceiptsViewModel with StateFlow for UI state
2. Implement ReceiptsScreen with LazyColumn list
3. Design ReceiptListItem composable with thumbnail
4. Add manual receipt creation form
5. Implement image selection from gallery (photo picker)
6. Create ReceiptDetailScreen with full-size image viewer
7. Add edit mode for receipt details (toggle view/edit)
8. Implement delete confirmation dialog
9. Add book filtering dropdown
10. Calculate and display total spending
11. Save images to app-specific directory (`context.filesDir/receipts/`)
12. Load images efficiently with Coil

**Critical Files:**
- `features/receipts/ReceiptsScreen.kt` & `ReceiptsViewModel.kt`
- `features/receipts/ReceiptDetailScreen.kt` & `ReceiptDetailViewModel.kt`
- `features/receipts/components/ReceiptListItem.kt`
- `data/repository/ReceiptRepository.kt` - Core receipt operations

**Success Criteria:** Can manually add, view, edit, delete receipts with images

---

### Phase 5: Camera & OCR Integration 📷
**Goal:** Automatic receipt scanning with text extraction

**Tasks:**
1. Add camera permissions handling (Accompanist Permissions)
2. Implement CameraPreview composable using CameraX
3. Create image capture logic
4. Integrate ML Kit Text Recognition
5. Create OcrProcessor injectable class
6. Implement ReceiptParser with regex patterns:
   - Vendor name extraction (from top 3 lines)
   - Date parsing (MM/DD/YYYY, DD-MM-YYYY, YYYY-MM-DD formats)
   - Total amount extraction (keywords: "Total", "Amount Due", etc.)
   - Payment info extraction (last 4 digits of card)
7. Update ScanReceiptScreen with auto-filled form
8. Display extracted raw text (debug view)
9. Allow manual override of all fields
10. Handle OCR errors gracefully with fallback
11. Add loading indicator during OCR processing

**Critical Files:**
- `features/scan/ScanReceiptScreen.kt` & `ScanReceiptViewModel.kt`
- `features/scan/camera/CameraPreview.kt`
- `features/scan/ocr/OcrProcessor.kt` - **Critical: ML Kit integration**
- `features/scan/ocr/ReceiptParser.kt` - Text parsing logic
- `core/di/OcrModule.kt` - Provide TextRecognizer

**Key OCR Patterns:**
```kotlin
// Vendor: Top 3 non-date lines
// Date: Regex for MM/DD/YYYY, DD-MM-YYYY, YYYY-MM-DD
// Amount: Regex for "total.*?$?\s*(\d+\.\d{2})"
// Card: Regex for "card|xxxx|****\s*(\d{4})"
```

**Success Criteria:** Can scan receipt photo and auto-populate vendor, date, amount, payment method

---

### Phase 6: Analytics & Reporting 📊
**Goal:** Spending insights and CSV export

**Tasks:**
1. Create AnalyticsViewModel
2. Implement date range picker component
3. Add DAO queries for spending in date range
4. Calculate total spending for selected period
5. Create SpendingGoal CRUD in settings
6. Implement goal progress calculation
7. Design SpendingGoalCard with visual progress bar
8. Add DAO query for category breakdown (GROUP BY categoryId)
9. Create CategoryBreakdownChart (pie or bar chart)
10. Implement CSV export logic (format: date,vendor,category,amount,notes)
11. Add file sharing intent for CSV
12. Create AnalyticsRepository for complex queries

**Critical Files:**
- `features/analytics/AnalyticsScreen.kt` & `AnalyticsViewModel.kt`
- `features/analytics/components/SpendingGoalCard.kt`
- `features/analytics/components/CategoryBreakdownChart.kt`
- `features/analytics/components/DateRangePicker.kt`
- `data/repository/AnalyticsRepository.kt`

**Key Queries:**
```kotlin
// Total spending in range
SELECT SUM(totalAmount) FROM receipts
WHERE transactionDate BETWEEN :start AND :end

// Category breakdown
SELECT categoryId, SUM(totalAmount) FROM receipts
WHERE transactionDate BETWEEN :start AND :end
GROUP BY categoryId
```

**Success Criteria:** Working analytics dashboard with date filtering, goal tracking, and CSV export

---

### Phase 7: Polish & Testing ✨
**Goal:** Production-ready app

**Tasks:**
1. Add loading states to all ViewModels
2. Implement comprehensive error handling
3. Create empty state composables for all lists
4. Add retry logic for failed operations
5. Implement smooth animations and transitions
6. Add form validation with error messages
7. Optimize image loading and caching with Coil
8. Write UI tests for critical flows (Compose UI testing)
9. Add content descriptions for accessibility
10. Performance profiling and optimization
11. Bug fixes from testing
12. Final UX polish (spacing, colors, transitions)

**Testing Coverage:**
- Unit tests: Repositories, ViewModels, ReceiptParser
- UI tests: Receipt creation flow, delete operations, scanning flow
- Integration tests: Database migrations, end-to-end flows

**Success Criteria:** Production-ready, tested, accessible app

---

## Package Structure

```
com.receiptkeeper/
├── app/
│   ├── MainActivity.kt
│   ├── ReceiptKeeperApp.kt (Hilt Application)
│   └── navigation/ (NavGraph, Routes, BottomNavigation)
├── core/
│   ├── database/ (ReceiptDatabase, Converters)
│   ├── util/ (DateFormatter, CurrencyFormatter, Constants)
│   └── di/ (AppModule, DatabaseModule, OcrModule)
├── data/
│   ├── local/entity/ (All entities)
│   ├── local/dao/ (All DAOs)
│   ├── repository/ (Repository implementations)
│   └── mapper/ (Entity ↔ Domain mappers)
├── domain/
│   └── model/ (Domain models: Receipt, Book, Category, etc.)
├── features/
│   ├── books/ (BooksScreen, BooksViewModel, BookDetailScreen)
│   ├── receipts/ (ReceiptsScreen, ReceiptDetailScreen + ViewModels)
│   ├── scan/ (ScanReceiptScreen + camera/ + ocr/)
│   ├── analytics/ (AnalyticsScreen + components)
│   └── settings/ (All settings screens)
└── ui/
    ├── theme/ (Color, Theme, Type)
    └── components/ (Shared reusable components)
```

---

## Critical Dependencies (build.gradle.kts)

```kotlin
dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // ML Kit OCR
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Utilities
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
}
```

---

## Verification & Testing

### End-to-End Test Scenarios

1. **Book Management Flow**
   - Create new book → Verify appears in grid
   - Edit book name → Verify changes persist
   - Delete book → Verify receipts are deleted (cascade)

2. **Receipt Scanning Flow**
   - Navigate to Scan tab
   - Grant camera permission
   - Capture receipt photo
   - Verify OCR extracts vendor, date, amount
   - Select book and category
   - Save → Verify appears in Receipts list

3. **Analytics Flow**
   - Set spending goal ($500/month)
   - Add receipts totaling $300
   - Navigate to Analytics
   - Select date range
   - Verify total shows $300
   - Verify progress bar shows 60%
   - Export CSV → Verify file contains receipts

4. **Edit/Delete Flow**
   - View receipt detail
   - Edit amount from $50 to $75
   - Save → Verify total spending updates
   - Swipe to delete → Verify removed from list

### Unit Test Coverage

- `ReceiptParser.extractAmount()` - Test various receipt formats
- `ReceiptParser.extractDate()` - Test MM/DD/YYYY, YYYY-MM-DD formats
- `ReceiptRepository.insertReceipt()` - Test auto-vendor creation
- `AnalyticsRepository.getCategoryBreakdown()` - Test grouping logic
- `BooksViewModel.deleteBook()` - Test cascade delete of receipts

---

## Key Implementation Notes

### OCR Processing
- Process on background thread (Dispatchers.IO)
- Show loading indicator during extraction
- Always allow manual override of extracted fields
- Store raw OCR text for debugging

### Image Storage
- Save images to `context.filesDir/receipts/`
- Store file URIs in database (not actual images)
- Implement cleanup when receipt deleted
- Use Coil for efficient loading with caching

### State Management Pattern
```kotlin
data class ReceiptsUiState(
    val receipts: List<Receipt> = emptyList(),
    val totalSpending: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel
private val _uiState = MutableStateFlow(ReceiptsUiState())
val uiState: StateFlow<ReceiptsUiState> = _uiState.asStateFlow()
```

### Data Seeding
- Use Room database callback to seed 8 default categories on first launch
- Each category has predefined color (hex codes)
- Mark default categories with `isDefault = true`

---

## Critical Files to Implement (Priority Order)

1. **app/build.gradle.kts** - Dependencies and configuration
2. **core/database/ReceiptDatabase.kt** - Database foundation
3. **data/local/entity/ReceiptEntity.kt** - Core schema with relationships
4. **app/navigation/NavGraph.kt** - Navigation structure
5. **features/scan/ocr/OcrProcessor.kt** - ML Kit integration (unique value)
6. **data/repository/ReceiptRepository.kt** - Core business logic
7. **features/scan/ScanReceiptScreen.kt** - Main feature UI
8. **features/receipts/ReceiptsScreen.kt** - Receipt browsing

---

## Estimated Timeline

- **Phase 0:** 1-2 days (SOP Infrastructure & Project Setup) ✅ In Progress
- **Phase 1:** 3-5 days (Database & DI setup)
- **Phase 2:** 2-3 days (Navigation & UI framework)
- **Phase 3:** 4-5 days (Books & Settings CRUD)
- **Phase 4:** 4-5 days (Receipt Management)
- **Phase 5:** 5-7 days (Camera & OCR - most complex)
- **Phase 6:** 4-5 days (Analytics & Export)
- **Phase 7:** 3-5 days (Polish & Testing)

**Total:** ~4-6 weeks for complete implementation

---

This plan provides a complete roadmap from empty project to production-ready Receipt Organizer app with all features from featurelist.md implemented using modern Android 2026 best practices.
