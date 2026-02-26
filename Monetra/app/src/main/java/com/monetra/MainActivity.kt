package com.monetra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.monetra.presentation.navigation.MonetraNavGraph
import com.monetra.ui.theme.MonetraTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val viewModel: MainViewModel by viewModels()
        
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
}