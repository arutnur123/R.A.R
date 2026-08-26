package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ui.components.FrostedBackgroundWrapper
import com.example.ui.components.FrostedNavigationBar
import com.example.ui.components.RARNavigationTab
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RARViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RARViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: RARViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(RARNavigationTab.CORE) }

    // Audio Permission Request Handler
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    FrostedBackgroundWrapper {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                FrostedNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    modifier = Modifier.navigationBarsPadding()
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        RARNavigationTab.CORE -> CoreAssistantScreen(viewModel = viewModel)
                        RARNavigationTab.HARDWARE -> HardwareScreen(viewModel = viewModel)
                        RARNavigationTab.POMODORO -> PomodoroScreen(viewModel = viewModel)
                        RARNavigationTab.TASKS -> TaskAndNotesScreen(viewModel = viewModel)
                        RARNavigationTab.AI_STUDIO -> AIStudioScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
