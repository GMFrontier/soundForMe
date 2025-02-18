package com.frommetoyou.soundforme.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.frommetoyou.soundforme.presentation.ui.AppBottomNavigation
import com.frommetoyou.soundforme.presentation.ui.CentralNavigation
import com.frommetoyou.soundforme.presentation.ui.SoundForMeApp
import com.frommetoyou.soundforme.presentation.ui.TopAppBar

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1111
            )
        }

        setContent {
            enableEdgeToEdge(
                navigationBarStyle =
                SystemBarStyle.dark(0x00)
            )
            AppTheme(darkTheme = true, dynamicColor = false) {
                SoundForMeApp {
                    val navController = rememberNavController()

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                navController = navController,
                            )
                        },
                        bottomBar = {
                            AppBottomNavigation(navController = navController)
                        }
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = innerPadding.calculateStartPadding(
                                        LayoutDirection.Rtl
                                    ),
                                    end = innerPadding.calculateEndPadding(
                                        LayoutDirection.Rtl
                                    ),
                                    bottom = innerPadding.calculateBottomPadding(),
                                    top = innerPadding.calculateTopPadding()
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