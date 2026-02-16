# ReceiptKeeper - AI Agent Instructions

## Project Overview
Android receipt scanning and management app with OCR capabilities using Jetpack Compose and ML Kit.

**Package Name:** `com.receiptkeeper`
**Min SDK:** 26 (Android 8.0)
**Target SDK:** 35 (Android 15)
**Compile SDK:** 35

## Development Methodology

This project follows the **LONG-RUNNING ANDROID AGENT HARNESS (SOP)**.

### Every Session MUST:
1. Execute `sh ai/init.sh` to validate environment
2. Read `ai/progress.md` for last handoff note
3. Read `ai/feature_list.json` to find next incomplete task

### Feature Implementation Loop:
1. **Implement** ONE sub-feature only
2. **Build:** `./gradlew assembleDebug`
3. **Deploy:** `adb install -r app/build/outputs/apk/debug/app-debug.apk`
4. **Verify:** `adb shell am start -n com.receiptkeeper/.MainActivity`
5. **Monitor:** `adb logcat -d *:E` (check for crashes)
6. **Update:** Mark complete in `ai/feature_list.json`
7. **Log:** Add handoff note to `ai/progress.md`
8. **Commit:** `git commit -m "feat: [Feature] verified on device"`

### Honesty Constraint
- If build fails 3 times, STOP and request human intervention
- Do NOT hallucinate fixes or skip verification steps
- Features marked "Complete" must be runnable on device
- Never mark a task complete without actual device verification

## Architecture

**Pattern:** MVVM with Clean Architecture principles
- **UI Layer:** Jetpack Compose screens + ViewModels (StateFlow for state)
- **Domain Layer:** Business models (pure Kotlin classes)
- **Data Layer:** Room database + Repositories

**Tech Stack:**
- Jetpack Compose (Material 3) - UI framework
- Room Database - Local persistence
- Hilt - Dependency injection
- ML Kit Text Recognition - OCR functionality
- CameraX - Camera integration
- Coil - Image loading
- Kotlin Coroutines + Flow - Async/reactive programming

## Package Structure

```
com.receiptkeeper/
├── app/
│   ├── MainActivity.kt (Entry point, NavHost setup)
│   ├── ReceiptKeeperApp.kt (Hilt Application class)
│   └── navigation/
│       ├── NavGraph.kt (Navigation configuration)
│       ├── Routes.kt (Route definitions)
│       └── BottomNavigation.kt (Bottom tabs component)
├── core/
│   ├── database/
│   │   ├── ReceiptDatabase.kt (Room database definition)
│   │   └── Converters.kt (Type converters for Room)
│   ├── util/
│   │   ├── DateFormatter.kt
│   │   ├── CurrencyFormatter.kt
│   │   └── Constants.kt
│   └── di/
│       ├── AppModule.kt (App-level DI)
│       ├── DatabaseModule.kt (Database DI)
│       └── OcrModule.kt (ML Kit DI)
├── data/
│   ├── local/
│   │   ├── entity/ (Room entities)
│   │   │   ├── ReceiptEntity.kt
│   │   │   ├── BookEntity.kt
│   │   │   ├── CategoryEntity.kt
│   │   │   ├── VendorEntity.kt
│   │   │   ├── PaymentMethodEntity.kt
│   │   │   └── SpendingGoalEntity.kt
│   │   └── dao/ (Data Access Objects)
│   │       ├── ReceiptDao.kt
│   │       ├── BookDao.kt
│   │       ├── CategoryDao.kt
│   │       ├── VendorDao.kt
│   │       ├── PaymentMethodDao.kt
│   │       └── SpendingGoalDao.kt
│   ├── repository/ (Repository implementations)
│   │   ├── ReceiptRepository.kt
│   │   ├── BookRepository.kt
│   │   ├── CategoryRepository.kt
│   │   ├── VendorRepository.kt
│   │   ├── PaymentMethodRepository.kt
│   │   ├── SpendingGoalRepository.kt
│   │   └── AnalyticsRepository.kt
│   └── mapper/ (Entity ↔ Domain mappers)
│       └── DataMappers.kt
├── domain/
│   └── model/ (Domain models)
│       ├── Receipt.kt
│       ├── Book.kt
│       ├── Category.kt
│       ├── Vendor.kt
│       ├── PaymentMethod.kt
│       └── SpendingGoal.kt
├── features/
│   ├── books/
│   │   ├── BooksScreen.kt
│   │   ├── BooksViewModel.kt
│   │   ├── BookDetailScreen.kt
│   │   ├── BookDetailViewModel.kt
│   │   └── components/ (Book-specific components)
│   ├── receipts/
│   │   ├── ReceiptsScreen.kt
│   │   ├── ReceiptsViewModel.kt
│   │   ├── ReceiptDetailScreen.kt
│   │   ├── ReceiptDetailViewModel.kt
│   │   └── components/
│   │       └── ReceiptListItem.kt
│   ├── scan/
│   │   ├── ScanReceiptScreen.kt
│   │   ├── ScanReceiptViewModel.kt
│   │   ├── camera/
│   │   │   └── CameraPreview.kt
│   │   └── ocr/
│   │       ├── OcrProcessor.kt (ML Kit integration)
│   │       └── ReceiptParser.kt (Text parsing logic)
│   ├── analytics/
│   │   ├── AnalyticsScreen.kt
│   │   ├── AnalyticsViewModel.kt
│   │   └── components/
│   │       ├── SpendingGoalCard.kt
│   │       ├── CategoryBreakdownChart.kt
│   │       └── DateRangePicker.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       ├── SettingsViewModel.kt
│       ├── VendorsScreen.kt
│       ├── CategoriesScreen.kt
│       └── PaymentMethodsScreen.kt
└── ui/
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    └── components/ (Shared reusable components)
        └── SwipeToDeleteItem.kt
```

## Database Schema

### Entities and Relationships

**ReceiptEntity** (main entity)
- Primary key: `id`
- Foreign keys: `bookId`, `vendorId`, `categoryId`, `paymentMethodId`
- Fields: `totalAmount`, `transactionDate`, `notes`, `imageUri`, `extractedText`, `createdAt`, `updatedAt`
- Cascade delete when book is deleted

**BookEntity**
- Primary key: `id`
- Fields: `name`, `description`, `createdAt`, `updatedAt`

**CategoryEntity** (8 pre-seeded defaults)
- Primary key: `id`
- Fields: `name`, `colorHex`, `isDefault`, `createdAt`
- Defaults: Food, Grocery, Hardware, Entertainment, Transportation, Utilities, Healthcare, Other

**VendorEntity**
- Primary key: `id`
- Fields: `name`, `createdAt`

**PaymentMethodEntity**
- Primary key: `id`
- Fields: `name`, `type` (Cash, Credit Card, Other), `lastFourDigits`, `createdAt`

**SpendingGoalEntity**
- Primary key: `id`
- Fields: `amount`, `period` (DAILY/WEEKLY/MONTHLY/YEARLY), `categoryId`, `createdAt`, `updatedAt`

## Build Commands

### Standard Build & Deploy
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.receiptkeeper/.MainActivity

# View error logs only
adb logcat -d *:E

# View all logs with filtering
adb logcat -s ReceiptKeeper:*
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

### Clean Build
```bash
# Clean build directory
./gradlew clean

# Clean + rebuild
./gradlew clean assembleDebug
```

## Critical Implementation Notes

### OCR Processing (Phase 5)
- Process on `Dispatchers.IO` background thread
- Show loading indicator during extraction
- Always allow manual override of extracted fields
- Store raw OCR text for debugging
- Regex patterns for parsing:
  - Vendor: Top 3 non-date lines
  - Date: `MM/DD/YYYY`, `DD-MM-YYYY`, `YYYY-MM-DD`
  - Amount: `total.*?$?\s*(\d+\.\d{2})`
  - Card: `card|xxxx|****\s*(\d{4})`

### Image Storage
- Save images to `context.filesDir/receipts/`
- Store file URIs in database (not actual images)
- Implement cleanup when receipt deleted
- Use Coil for efficient loading with caching

### State Management Pattern
```kotlin
// Standard ViewModel state pattern
data class ReceiptsUiState(
    val receipts: List<Receipt> = emptyList(),
    val totalSpending: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

// In ViewModel
private val _uiState = MutableStateFlow(ReceiptsUiState())
val uiState: StateFlow<ReceiptsUiState> = _uiState.asStateFlow()
```

### Data Seeding
- Use Room database callback to seed 8 default categories on first launch
- Each category has predefined color (hex codes)
- Mark default categories with `isDefault = true`

## Testing Requirements

### Every Feature MUST:
1. Build without errors
2. Deploy successfully to device/emulator
3. Launch without crashes
4. Function as specified in feature_list.json
5. Be verified on actual device before marking complete

### Check for Silent Crashes:
```bash
# After deploying and testing feature
adb logcat -d *:E | grep -E "FATAL|AndroidRuntime|Error"
```

### End-to-End Test Scenarios:
1. **Book Management:** Create → Edit → Delete book with receipts
2. **Receipt Scanning:** Scan → OCR → Save → Verify in list
3. **Analytics:** Add receipts → Set goal → View analytics → Export CSV
4. **Edit/Delete:** View detail → Edit → Delete → Verify removed

## Git Commit Conventions

```bash
# Feature completion
git commit -m "feat: implement books screen with CRUD operations"

# Bug fix
git commit -m "fix: resolve crash when deleting receipt without image"

# Build/config changes
git commit -m "build: add CameraX dependencies"

# Documentation
git commit -m "docs: update CLAUDE.md with OCR patterns"
```

## Troubleshooting

### Build Fails
1. Check `ai/init.sh` passes
2. Verify Gradle sync in Android Studio
3. Clean build: `./gradlew clean`
4. Check dependency versions in `app/build.gradle.kts`

### Deploy Fails
1. Verify device connected: `adb devices`
2. Check USB debugging enabled on device
3. Uninstall old version: `adb uninstall com.receiptkeeper`
4. Retry install

### App Crashes
1. Check logcat: `adb logcat -d *:E`
2. Look for `FATAL EXCEPTION` or `AndroidRuntime` errors
3. Verify all Hilt modules are properly configured
4. Check database migrations if schema changed

## Phase Implementation Order

1. **Phase 0:** SOP Infrastructure (this setup)
2. **Phase 1:** Database & DI foundation
3. **Phase 2:** Navigation & UI scaffolds
4. **Phase 3:** Books & Settings CRUD
5. **Phase 4:** Receipt management (manual entry)
6. **Phase 5:** Camera & OCR (core value feature)
7. **Phase 6:** Analytics & CSV export
8. **Phase 7:** Polish & testing

## Critical Success Factors

- **Never skip device verification** - features aren't done until tested on device
- **Update tracking artifacts** - keep feature_list.json and progress.md current
- **One feature at a time** - resist temptation to do multiple features at once
- **Build frequently** - catch errors early
- **Test on real device** - emulator behavior can differ from physical devices

---

**Last Updated:** 2026-02-16
**Current Phase:** Phase 0 - SOP Infrastructure Setup
