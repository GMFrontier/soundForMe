package com.frommetoyou.soundforme.domain.use_case

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.model.ActiveButton
import com.frommetoyou.soundforme.domain.model.Modes
import com.frommetoyou.soundforme.domain.model.SettingConfig
import com.frommetoyou.soundforme.presentation.MainActivity
import com.frommetoyou.soundforme.presentation.ui.util.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

const val DETECTOR_CHANNEL_ID = "Detector"

class DetectorService : Service() {
    val screenUnlockReceiver = ScreenUnlockReceiver()
    val intentFilter = IntentFilter(Intent.ACTION_USER_PRESENT)

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var settingsManager: SettingsManager

    private var settings: SettingConfig? = null
    private var audioClassifierUseCase: AudioClassifierUseCase? = null
    private var musicPlayer: MusicPlayer? = null
    private var flashJob: Job? = null
    private var vibrationJob: Job? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(screenUnlockReceiver, intentFilter)

        settingsManager = KoinJavaComponent.getKoin().get()
        audioClassifierUseCase = KoinJavaComponent.getKoin().get()
        musicPlayer = KoinJavaComponent.getKoin().get()
        serviceScope.launch {
            settingsManager.getSettings().collectLatest {
                settings?.musicItem?.uri != it.musicItem.uri
                musicPlayer?.setAlarmUri(it.musicItem.uri.toUri())
                settings = it
                updateNotification()
            }
        }
    }

    override fun onStartCommand(
        intent: Intent?, flags: Int, startId: Int
    ): Int {

        when (intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            audioClassifierUseCase?.let {
                it.startDetector().collectLatest { result ->
                    if (result == settings?.detectionMode) {
                        settings?.let {
                            toggleFlashlight(
                                context = this@DetectorService,
                                it.flashMode
                            )
                            toggleVibration(
                                context = this@DetectorService,
                                it.vibrationMode
                            )
                        }
                        musicPlayer?.startMusic()
                    }
                }
            }
        }

        val serviceIntent = Intent(this, DetectorService::class.java).apply {
            action = Actions.STOP.toString()
        }
        val stopServicePendingIntent: PendingIntent = PendingIntent.getService(
            this,
            0,
            serviceIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            1,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            this, DETECTOR_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(this.getString(R.string.detection_active))
            .setContentText("${this.getString(R.string.detection_mode)} ${settings?.detectionMode}")
            .setContentIntent(mainPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                UiText.StringResource(R.string.stop)
                    .asString(this@DetectorService),
                stopServicePendingIntent
            )
            .build()
        startForeground(1, notification)
    }

    private fun updateNotification() {
        val notification = NotificationCompat.Builder(
            this, "Detector"
        ).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(this.getString(R.string.detection_active))
            .setContentText("${this.getString(R.string.detection_mode)} ${settings?.detectionMode}")
            .build()
        startForeground(1, notification)  // Update the notification
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        audioClassifierUseCase?.stopDetector()
        stopFlashing(this@DetectorService)
        stopVibration()
        toggleFlashlight(this@DetectorService, Modes.Off)
        musicPlayer?.stopMusic()
        unregisterReceiver(screenUnlockReceiver)
        CoroutineScope(Dispatchers.IO).launch {
            settings?.let {
                settingsManager.saveSettings(
                    it.copy(
                        active = ActiveButton.Off
                    )
                )
            }
        }
        stopSelf()
    }

    private fun toggleFlashlight(context: Context, flash: Modes) {
        when (flash) {
            Modes.Fixed -> {
                val cameraManager =
                    context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraId =
                    getCameraWithFlash(context) ?: return
                cameraManager.setTorchMode(cameraId, true)
            }
            Modes.Intermittent -> {
                startFlashing(context)
            }
            else -> {
                stopFlashing(context)
            }
        }
    }

    private fun toggleVibration(context: Context, flash: Modes) {
        when (flash) {
            Modes.Fixed -> {
                vibratePhone(context, 5000)
            }

            Modes.Intermittent -> {
                vibratePhone(context)
            }

            else -> {
                stopVibration()
            }
        }
    }

    private fun startFlashing(context: Context) {
        val cameraManager =
            context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = getCameraWithFlash(context) ?: return

        flashJob?.cancel()

        flashJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                cameraManager.setTorchMode(cameraId, true)
                delay(1000)
                cameraManager.setTorchMode(cameraId, false)
                delay(500)
            }
        }
    }

    private fun stopFlashing(context: Context) {
        val cameraManager =
            context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = getCameraWithFlash(context) ?: return

        flashJob?.cancel()
        cameraManager.setTorchMode(cameraId, false)
    }

    private fun vibratePhone(context: Context, durationMs: Long = 500) {
        vibrationJob?.cancel()

        vibrationJob = CoroutineScope(Dispatchers.IO).launch {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            while (isActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            durationMs,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }

                delay(1000)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            500,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }

                delay(1000)
            }
        }
    }

    private fun getCameraWithFlash(context: Context): String? {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return cameraManager.cameraIdList.firstOrNull { cameraId ->
            cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }

    private fun stopVibration() {
        vibrationJob?.cancel()
    }

    enum class Actions {
        START, STOP
    }
}