# ReceiptKeeper Static Analysis & Architectural Audit Report

---

## CRITICAL RISKS

### 1. GlobalScope Anti-Pattern (HIGH)
**Files:**
- `ReceiptDetailScreen.kt:414`
- `ReceiptsScreen.kt:593, 914`

**Issue:** Using `GlobalScope` instead of `viewModelScope` for coroutine operations. This bypasses structured concurrency and can lead to:
- Memory leaks (coroutines outliving the UI)
- Unpredictable cancellation behavior
- Resource leaks

**Recommendation:** Replace all `GlobalScope.launch` with `viewModelScope.launch` or use proper Compose state management with `rememberCoroutineScope()`.

---

### 2. ImageHandler Context Leak Risk (MEDIUM)
**File:** `ReceiptDetailScreen.kt:384`

```kotlin
val imageHandler = remember { ImageHandler(context) }
```

**Issue:** Creating `ImageHandler` with `LocalContext.current` in a Composable can lead to stale Context references if the Composable survives longer than the Activity. The `ImageHandler` is also a `@Singleton` with `@ApplicationContext`, so this instantiation is redundant.

**Recommendation:** Use Hilt injection instead: `@Inject lateinit var imageHandler: ImageHandler` via ViewModel.

---

### 3. Deprecated Storage APIs (MEDIUM)
**File:** `CsvExporter.kt:118, 124, 145`

```kotlin
Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
```

**Issue:** Deprecated since Android 10 (API 29). Will fail on Android 11+ for direct file access.

**Recommendation:** Use MediaStore Downloads API consistently, or use `context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)` as fallback.

---

### 4. Hardcoded Splash Delay (LOW)
**File:** `MainActivity.kt:53`

```kotlin
delay(1500) // Show splash for 1.5 seconds
```

**Issue:** Blocks a coroutine thread for 1.5 seconds unnecessarily. This is a minor UX anti-pattern.

**Recommendation:** Use Splash Screen API (Android 12+) or show splash only during initial loading.

---

## REDUNDANT CODE

### 5. Unused Composable Function
**File:** `CategoryBreakdownChart.kt:461-529`

`CategorySpendingItem` is defined but never invoked anywhere in the codebase.

**Action:** Remove or integrate into chart views.

---

### 6. Unused Imports in ReceiptsScreen
**File:** `ReceiptsScreen.kt:32-34`

```kotlin
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
```

**Issue:** These ML Kit imports are never used in this file - OCR is handled in `ScanReceiptViewModel`.

**Action:** Remove unused imports.

---

### 7. Nullability Risk in ReceiptsScreen
**File:** `ReceiptsScreen.kt:593`

```kotlin
GlobalScope.launch {
    // image download operation
}
```

If the screen is destroyed during download, this operation continues blindly and may crash when trying to show Toast.

---

### 8. Legacy CSV Export Method
**File:** `CsvExporter.kt:197-199`

```kotlin
fun shareCSV(context: Context, file: File) {
    // Now just shows that file is saved
}
```

Empty implementation kept for "compatibility" - should be removed or properly implemented.

---

## REFACTORING RECOMMENDATIONS

### 9. Missing Dedicated ViewModel
**File:** `ReceiptDetailScreen.kt:41`

```kotlin
viewModel: ReceiptsViewModel = hiltViewModel()
```

**Issue:** Reusing `ReceiptsViewModel` for detail screen breaks separation of concerns. The detail screen should have its own `ReceiptDetailViewModel` to:
- Load only the single receipt (not all receipts)
- Handle detail-specific operations (delete, export single receipt)
- Properly scope lifecycle

**Action:** Create `ReceiptDetailViewModel` with single receipt loading.

---

### 10. Dispatcher Qualifier Issues
**File:** `AppModule.kt:19-32`

```kotlin
@Provides @Singleton @IoDispatcher
fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
```

**Issue:** `Dispatchers.IO` is already a singleton object. Wrapping it in `@Singleton` provides no benefit and adds unnecessary indirection.

**Recommendation:** Remove custom dispatcher providers and use `Dispatchers.IO` directly, or use Hilt's built-in `@IoDispatcher` from `dagger.hilt.android.lifecycle.HiltViewModel`.

---

### 11. Database Migration Strategy
**File:** `DatabaseModule.kt:35`

```kotlin
.fallbackToDestructiveMigration() // For development only - remove in production
```

**Issue:** Dangerous for production - will wipe all user data on schema changes.

**Action:** Implement proper migrations before production release.

---

### 12. Category Entity Reference Inconsistency
**Issue:** `CategoryBreakdownChart` accepts `List<Category>` but also `List<CategorySpending>`. This creates redundant lookups in every composable:

```kotlin
val category = categories.find { it.id == spending.categoryId }
```

**Recommendation:** Pre-join data in ViewModel or create a combined data class.

---

## DEPENDENCY ANALYSIS

### 13. build.gradle.kts Review

| Dependency | Status | Notes |
|-------------|--------|-------|
| ML Kit Text Recognition | ✅ Used | In `OcrProcessor.kt` |
| CameraX | ✅ Used | In `CameraPreview.kt` |
| Room | ✅ Used | Throughout |
| Hilt | ✅ Used | Throughout |
| Coil | ✅ Used | Image loading |
| Accompanist Permissions | ✅ Used | Camera permissions |
| kotlinx-datetime | ✅ Used | Date handling |
| Coroutines Play Services | ✅ Used | `Tasks.await()` |

**No leaky dependencies found** - all are properly scoped with `implementation`.

---

## ARCHITECTURE OBSERVATIONS

### Positive Patterns
- ✅ Room operations all use `Flow` - no blocking main thread
- ✅ OCR processing properly uses `Dispatchers.IO` via `@IoDispatcher`
- ✅ Bitmap correctly recycled in `OcrProcessor.kt:40`
- ✅ Clean separation: Repository pattern with domain models
- ✅ Proper use of Hilt for DI
- ✅ Custom charts implemented with Compose Canvas (no MPAndroidChart bloat)

### Areas for Improvement
- ⚠️ Over-fetching in detail screen (loads all receipts just to find one)
- ⚠️ Some composables have too many responsibilities
- ⚠️ No error boundaries for crash recovery

---

## SUMMARY

| Category | Count |
|----------|-------|
| Critical Risks | 1 (GlobalScope) |
| High Priority | 3 (Context leak, deprecated APIs) |
| Medium Priority | 2 (Splash delay, redundant code) |
| Refactoring Candidates | 4 |

**Top 3 Actions:**
1. Replace `GlobalScope` with `viewModelScope` or `rememberCoroutineScope()`
2. Add dedicated `ReceiptDetailViewModel`
3. Remove unused imports and implement proper DB migrations before production

---

*Report generated: 2026-02-27*
