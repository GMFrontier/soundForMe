package com.frommetoyou.soundforme.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.frommetoyou.soundforme.presentation.ui.AppBottomNavigation
import com.frommetoyou.soundforme.presentation.ui.CentralNavigation
import com.frommetoyou.soundforme.presentation.ui.SoundForMeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme(darkTheme = true, dynamicColor = false) {
                SoundForMeApp() {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = {
                            AppBottomNavigation(navController = navController)
                        }
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = innerPadding.calculateStartPadding(LayoutDirection.Rtl),
                                    end = innerPadding.calculateEndPadding(LayoutDirection.Rtl),
                                    bottom = innerPadding.calculateBottomPadding()
                                )
                        ) {
                            CentralNavigation(navController = navController)
                        }
                    }
                }
            }
        }
    }
}