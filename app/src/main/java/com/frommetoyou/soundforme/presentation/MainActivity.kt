package com.frommetoyou.soundforme.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.frommetoyou.soundforme.presentation.ui.AppBottomNavigation
import com.frommetoyou.soundforme.presentation.ui.CentralNavigation
import com.frommetoyou.soundforme.presentation.ui.SoundForMeApp
import com.frommetoyou.soundforme.presentation.ui.TopAppBar
import com.frommetoyou.soundforme.presentation.ui.util.getAdvertisingId
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.concurrent.thread


class MainActivity : ComponentActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var appUpdateManager: AppUpdateManager
    private var updateType = AppUpdateType.IMMEDIATE
    private lateinit var updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var snackbarHostState: SnackbarHostState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)

        val context = this
        MobileAds.initialize(context)

        updateResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            if (result.resultCode != RESULT_OK) {
                Log.v(
                    "InAppUpdates",
                    "Update flow failed! Result code: " + result.resultCode
                );
            }
        }

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
                    snackbarHostState = remember { SnackbarHostState() }

                    checkForUpdates()

                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState)
                        },
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

    private val installStateUpdatedListener =
        InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                launchRestartSnackBar()
            }
        }

    private fun checkForUpdates() {

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            updateType =
                if (info.updatePriority() >= 4) AppUpdateType.IMMEDIATE else AppUpdateType.IMMEDIATE
            val isUpdateAvailable =
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            if (isUpdateAvailable) {
                when (updateType) {
                    AppUpdateType.FLEXIBLE -> {
                        appUpdateManager.registerListener(
                            installStateUpdatedListener
                        )

                    }

                    AppUpdateType.IMMEDIATE -> {
                        appUpdateManager.startUpdateFlowForResult(
                            info,
                            updateResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                                .build()
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (updateType == AppUpdateType.IMMEDIATE) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                            .build()
                    )
                }
            }
        }
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    launchRestartSnackBar()
                }
            }
        }
    }

    private fun launchRestartSnackBar() {
        lifecycleScope.launch {
            val result = snackbarHostState
                .showSnackbar(
                    message = "Download successful, please restart.",
                    actionLabel = "Restart",
                    duration = SnackbarDuration.Indefinite
                )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    appUpdateManager.completeUpdate()
                }

                SnackbarResult.Dismissed -> {
                    /* Handle snackbar dismissed */
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
    }
}