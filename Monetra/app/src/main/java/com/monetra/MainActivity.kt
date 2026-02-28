package com.monetra

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.monetra.presentation.navigation.MonetraNavGraph
import com.monetra.presentation.navigation.Route
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
            !viewModel.isReady.value
        }

        enableEdgeToEdge()
        setContent {
            val isReady by viewModel.isReady.collectAsStateWithLifecycle()
            val isDashboardUser by viewModel.isDashboardUser.collectAsStateWithLifecycle()
            
            // Moving rememberNavBackStack into the `if (isReady)` block
            // to ensure it takes the correct start destination.
            // The Lock screen logic is also moved inside the if (isReady) block
            MonetraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isReady) {
                        // In Navigation 3, the backstack is a SnapshotStateList<Any>.
                        // We initialize it with the first route when it's empty, and
                        // push the Lock screen immediately to prevent a launch blink.
                        val baseRoute = if (isDashboardUser) Route.TransactionList else Route.Welcome
                        val backStack = rememberNavBackStack(baseRoute as NavKey, Route.Lock as NavKey)

                        MonetraNavGraph(backStack = backStack)
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