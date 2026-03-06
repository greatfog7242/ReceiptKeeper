# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep WorkManager and Hilt Worker classes
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.hilt.work.HiltWorker { *; }
-keep class com.receiptkeeper.core.work.BackupWorker { *; }
-keep class androidx.hilt.work.HiltWorkerFactory { *; }

# Keep Hilt WorkerFactory
-keep class * implements androidx.work.Configuration$Provider { *; }

# Keep WorkManager internal classes
-keepclassmembers class androidx.work.impl.WorkerWrapper {
    *;
}
-keepclassmembers class androidx.work.impl.utils.futures.SettableFuture {
    *;
}
