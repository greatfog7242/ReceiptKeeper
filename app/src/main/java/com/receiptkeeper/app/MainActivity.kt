package com.receiptkeeper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.receiptkeeper.core.preferences.IconThemeManager
import com.receiptkeeper.core.preferences.LocalIconTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.receiptkeeper.app.navigation.BottomNavigationBar
import com.receiptkeeper.app.navigation.NavGraph
import com.receiptkeeper.ui.theme.ReceiptKeeperTheme
import kotlinx.coroutines.delay

/**
 * Main Activity for ReceiptKeeper
 * Serves as the entry point and hosts the Compose navigation graph
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReceiptKeeperTheme {
                MainApp()
            }
        }
    }
}

/**
 * Splash screen composable that shows the app icon briefly
 */
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500) // Show splash for 1.5 seconds
        showSplash = false
        onSplashComplete()
    }

    if (showSplash) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = "file:///android_res/drawable/ic_launcher_foreground.png"),
                contentDescription = "App Icon",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Main app composable with navigation
 */
@Composable
fun MainApp() {
    var showSplash by remember { mutableStateOf(true) }
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val iconThemeManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            IconThemeManagerEntryPoint::class.java
        ).iconThemeManager()
    }
    val iconTheme by iconThemeManager.iconTheme.collectAsState(initial = com.receiptkeeper.core.preferences.IconTheme.COLORFUL)

    // Debug: log theme changes
    androidx.compose.runtime.LaunchedEffect(iconTheme) {
        android.util.Log.d("ReceiptKeeper", "Icon theme changed to: $iconTheme")
    }

    if (showSplash) {
        SplashScreen(onSplashComplete = { showSplash = false })
    } else {
        CompositionLocalProvider(
            LocalIconTheme provides iconTheme
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    BottomNavigationBar(navController = navController)
                }
            ) { innerPadding ->
                NavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

/**
 * Hilt entry point for accessing IconThemeManager
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface IconThemeManagerEntryPoint {
    fun iconThemeManager(): IconThemeManager
}
