package com.monetra

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.viewModels
import com.monetra.presentation.navigation.MonetraNavGraph
import com.monetra.ui.theme.MonetraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // Tracks whether the app was backgrounded since last successful auth  
    private var wasInBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.startDestination.value == null
        }

        enableEdgeToEdge()
        setContent {
            val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

            MonetraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    startDestination?.let { dest ->
                        MonetraNavGraph(startDestination = dest)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // App is going to background — mark for re-lock on next resume
        wasInBackground = true
    }

    override fun onResume() {
        super.onResume()
        // If we returned from background, re-lock by resetting the start destination.
        // The ViewModel will re-emit the Lock screen as the destination.
        if (wasInBackground) {
            wasInBackground = false
            viewModel.requestRelock()
        }
    }
}