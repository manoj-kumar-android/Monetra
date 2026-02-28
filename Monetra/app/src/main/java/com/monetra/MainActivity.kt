package com.monetra

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !viewModel.isReady.value
        }

        enableEdgeToEdge()

        // Detect Deep Link early
        val intentData = intent?.data
        if (intentData?.scheme == "monetra" && intentData.host == "refundable") {
            intentData.getQueryParameter("id")?.toLongOrNull()?.let { id ->
                viewModel.setPendingRefundableId(id)
            }
        }

        setContent {
            val isReady by viewModel.isReady.collectAsStateWithLifecycle()
            val isDashboardUser by viewModel.isDashboardUser.collectAsStateWithLifecycle()
            
            MonetraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isReady) {
                        val initialRoutes = remember(isDashboardUser) {
                            val routes = mutableListOf<NavKey>()
                            if (isDashboardUser) {
                                val id = viewModel.consumePendingRefundableId()
                                if (id != null) {
                                    routes.add(Route.TransactionList())
                                    routes.add(Route.TransactionList(initialTab = "Refundable"))
                                    routes.add(Route.RefundableDetails(id))
                                } else {
                                    routes.add(Route.TransactionList())
                                }
                            } else {
                                routes.add(Route.Welcome)
                            }
                            routes.toTypedArray()
                        }
                        val backStack = rememberNavBackStack(*initialRoutes)

                        MonetraNavGraph(backStack = backStack)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        // Handle deep link if activity is already running
        val intentData = intent.data
        if (intentData?.scheme == "monetra" && intentData.host == "refundable") {
            intentData.getQueryParameter("id")?.toLongOrNull()?.let { id ->
                viewModel.setPendingRefundableId(id)
            }
        }
    }


}