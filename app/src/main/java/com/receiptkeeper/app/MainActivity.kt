package com.receiptkeeper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.receiptkeeper.app.navigation.BottomNavigationBar
import com.receiptkeeper.app.navigation.NavGraph
import com.receiptkeeper.ui.theme.ReceiptKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

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
 * Main app composable with navigation
 */
@Composable
fun MainApp() {
    val navController = rememberNavController()

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
