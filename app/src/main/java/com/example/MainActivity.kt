package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNav
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.ExerciseScreen
import com.example.ui.screens.ExercisesOverviewScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LessonDetailScreen
import com.example.ui.screens.LessonsListScreen
import com.example.ui.theme.ArabicAppTheme
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.ArabicAppViewModel
import com.example.viewmodel.NavTab

class MainActivity : ComponentActivity() {
    private val appViewModel: ArabicAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // If the previous run crashed, show the stack trace so it can be copied.
        val crashFile = java.io.File(filesDir, "crash.txt")
        if (crashFile.exists()) {
            val crashText = try { crashFile.readText() } catch (_: Exception) { "" }
            crashFile.delete()
            setContent {
                androidx.compose.foundation.text.selection.SelectionContainer {
                    androidx.compose.material3.Text(
                        text = crashText,
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    )
                }
            }
            return
        }

        setContent {
            val settings by appViewModel.settings.collectAsState()

            ArabicAppTheme(darkTheme = settings.isDarkMode) {
                // Enforce RTL Arabic direction across all screens
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ArabicStudyApp(viewModel = appViewModel)
                }
            }
        }
    }
}

@Composable
fun ArabicStudyApp(
    viewModel: ArabicAppViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeScreen by viewModel.activeScreen.collectAsState()
    val isDataLoaded by viewModel.isDataLoaded.collectAsState()

    if (!isDataLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                androidx.compose.material3.CircularProgressIndicator()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = "جارٍ تحميل الدروس…",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                )
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (activeScreen == ActiveScreen.TABS) {
                AppBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { tab -> viewModel.selectTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeScreen) {
                ActiveScreen.LESSON_DETAIL -> {
                    LessonDetailScreen(viewModel = viewModel)
                }

                ActiveScreen.EXERCISE_SESSION -> {
                    ExerciseScreen(viewModel = viewModel)
                }

                ActiveScreen.EXAM_VIEW -> {
                    ExamScreen(viewModel = viewModel)
                }

                ActiveScreen.TABS -> {
                    when (currentTab) {
                        NavTab.HOME -> HomeScreen(viewModel = viewModel)
                        NavTab.LESSONS -> LessonsListScreen(viewModel = viewModel)
                        NavTab.EXERCISES -> ExercisesOverviewScreen(viewModel = viewModel)
                        NavTab.ACCOUNT -> AccountScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
