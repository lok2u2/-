package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.MedicinesScreen
import com.example.ui.screens.SystemScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val viewModel: MainViewModel = viewModel()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    // If no user is logged in, show login form
    if (currentUser == null) {
        LoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = { viewModel.setScreen(Screen.HOME) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "首页") },
                        label = { Text("首页", fontSize = 12.sp) },
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.MEDICINES,
                        onClick = { viewModel.setScreen(Screen.MEDICINES) },
                        icon = { Icon(Icons.Filled.Healing, contentDescription = "药物管理") },
                        label = { Text("药物管理", fontSize = 12.sp) },
                        modifier = Modifier.testTag("nav_medicines")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.LOGS,
                        onClick = { viewModel.setScreen(Screen.LOGS) },
                        icon = { Icon(Icons.Filled.History, contentDescription = "履历日志") },
                        label = { Text("药箱日志", fontSize = 12.sp) },
                        modifier = Modifier.testTag("nav_logs")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.SYSTEM,
                        onClick = { viewModel.setScreen(Screen.SYSTEM) },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "系统设置") },
                        label = { Text("系统管理", fontSize = 12.sp) },
                        modifier = Modifier.testTag("nav_system")
                    )
                }
            }
        ) { innerPadding ->
            // Display screen based on current screen routing state
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen(viewModel = viewModel)
                    Screen.MEDICINES -> MedicinesScreen(viewModel = viewModel)
                    Screen.LOGS -> LogsScreen(viewModel = viewModel)
                    Screen.SYSTEM -> SystemScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
