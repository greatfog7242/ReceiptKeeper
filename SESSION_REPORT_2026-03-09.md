# Session Report — 2026-03-09

## Summary

Two changes were made this session: one bug fix to the automatic backup system, and one UI adjustment to the bottom navigation bar icon size.

---

## Fix 1 — Automatic Backup Never Fires

**Commits:** `0d43b75`
**Files changed:**
- `app/src/main/java/com/receiptkeeper/core/work/BackupScheduler.kt`
- `app/src/main/java/com/receiptkeeper/features/settings/BackupRestoreViewModel.kt`

### Symptom

The automatic daily backup (scheduled for 5:00 AM) never executed, while the "Test WorkManager Backup" button in the same settings screen worked correctly every time.

### Root Cause Analysis

#### The timer reset problem

`ReceiptKeeperApp.onCreate()` is called every time the Android process starts. On every call it invoked `backupScheduler.scheduleDailyBackup()`, which contained two compounding mistakes:

**Mistake 1 — Explicit cancel before enqueue (`BackupScheduler.kt`, line 36):**
```kotlin
WorkManager.getInstance(context).cancelUniqueWork(BackupWorker.WORK_NAME)
```
This unconditionally cancelled any existing, correctly-scheduled periodic work before re-enqueuing.

**Mistake 2 — `ExistingPeriodicWorkPolicy.REPLACE` (`BackupScheduler.kt`, line 65):**
```kotlin
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    BackupWorker.WORK_NAME,
    ExistingPeriodicWorkPolicy.REPLACE,  // <-- resets the 24-hour timer
    backupRequest
)
```
`REPLACE` cancels and replaces any existing periodic work, resetting the entire period counter from zero. Combined with `calculateInitialDelay()` (which always computes the delay to the *next* 5:00 AM), the result was:

- Every app process start → cancel existing work → re-enqueue with fresh delay to next 5:00 AM
- Android routinely kills and restarts app processes, so this happened multiple times per day
- The 5:00 AM window was perpetually pushed forward and never reached

#### Why the test backup worked

`triggerImmediateBackup()` enqueues a `OneTimeWorkRequest` with no initial delay. It runs immediately on enqueue and is unaffected by subsequent app restarts.

#### Secondary issue — toggle disable did not cancel work

`BackupRestoreViewModel.toggleAutoBackup(enabled = false)` only wrote the preference flag:
```kotlin
backupPreferences.isAutoBackupEnabled = false
// WorkManager task was NOT cancelled
```
The BackupWorker would still fire at 5:00 AM, check the flag, and silently skip. While not the cause of the primary bug, the work should be cancelled when the user disables the feature.

### Fix Applied

**`BackupScheduler.kt`** — removed the explicit `cancelUniqueWork` call and changed the policy to `KEEP`:
```kotlin
// KEEP: if work is already scheduled, leave it alone.
// Only enqueues fresh when nothing is scheduled (first launch, or after explicit cancel).
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    BackupWorker.WORK_NAME,
    ExistingPeriodicWorkPolicy.KEEP,
    backupRequest
)
```

**`BackupRestoreViewModel.kt`** — added explicit cancellation when the user disables auto-backup:
```kotlin
if (enabled) {
    backupScheduler.scheduleDailyBackup()
} else {
    backupScheduler.cancelDailyBackup()
}
```

### Outcome

- First app launch: schedules periodic work with initial delay to next 5:00 AM ✓
- Subsequent app launches: existing schedule is preserved, timer is not reset ✓
- User disables auto-backup: WorkManager task is cancelled, not just preference-flagged ✓
- User re-enables auto-backup: fresh schedule is created (no existing work to keep) ✓

---

## Fix 2 — Bottom Navigation Icon Size

**Commit:** `c4513af`
**File changed:**
- `app/src/main/java/com/receiptkeeper/app/navigation/BottomNavigation.kt`

### Change

The non-Scan bottom navigation icons were too small at `18.dp`. Increased to `24.dp` to improve visual balance and tap target clarity.

```kotlin
// Before
val iconSize = if (item.route == Routes.Scan) 42.dp else 18.dp

// After
val iconSize = if (item.route == Routes.Scan) 42.dp else 24.dp
```

The Scan icon remains at `42.dp` as it is intentionally oversized to serve as the primary action button.

---

## Deployment

Both fixes were built as a clean release APK and deployed to the connected device via `adb install -r` (no uninstall, user data preserved). Both commits were pushed to `origin/master`.

| Commit | Description |
|--------|-------------|
| `0d43b75` | fix: automatic backup never fires due to WorkManager policy reset on every app launch |
| `c4513af` | feat: increase bottom nav icon size from 18dp to 24dp |
