package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.LockScreen
import com.example.ui.screens.MainAppContainer
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.AriaTheme
import com.example.viewmodel.AuthState
import com.example.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: FinanceViewModel = viewModel()
      val userProfile by viewModel.userProfile.collectAsState()
      val authState by viewModel.authState.collectAsState()

      val themeId = userProfile?.activeThemeId ?: "MINIMAL_WHITE"

      AriaTheme(themeId = themeId) {
        Box(modifier = Modifier.fillMaxSize()) {
          when (authState) {
            AuthState.Onboarding -> OnboardingScreen(viewModel = viewModel)
            AuthState.Locked -> LockScreen(viewModel = viewModel)
            AuthState.Authenticated -> MainAppContainer(viewModel = viewModel)
          }
        }
      }
    }
  }
}
