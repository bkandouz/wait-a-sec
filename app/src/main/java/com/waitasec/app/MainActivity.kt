package com.waitasec.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.waitasec.app.ui.onboarding.OnboardingScreen
import com.waitasec.app.ui.settings.SettingsScreen
import com.waitasec.app.ui.settings.SettingsViewModel
import com.waitasec.app.ui.theme.Mist
import com.waitasec.app.ui.theme.SageMid
import com.waitasec.app.ui.theme.WaitASecTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WaitASecTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Mist) {
                    val app = application as WaitASecApp
                    val viewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.Factory(app),
                    )
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        val settings = withTimeoutOrNull(2_000) {
                            app.repository.settings.first { true }
                        }
                        startDestination = if (settings?.onboardingComplete == true) {
                            "settings"
                        } else {
                            "onboarding"
                        }
                    }

                    val destination = startDestination
                    if (destination == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SageMid)
                        }
                    } else {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = destination) {
                            composable("onboarding") {
                                OnboardingScreen(
                                    accessibilityEnabled = viewModel.accessibilityEnabled,
                                    onOpenAccessibility = viewModel::openAccessibilitySettings,
                                    onRefreshAccessibility = viewModel::refreshAccessibilityStatus,
                                    onContinue = {
                                        viewModel.completeOnboarding()
                                        navController.navigate("settings") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    },
                                )
                            }
                            composable("settings") {
                                SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
