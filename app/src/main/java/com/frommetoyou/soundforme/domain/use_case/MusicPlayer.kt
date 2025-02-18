package com.frommetoyou.soundforme.domain.use_case

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri
import com.frommetoyou.soundforme.domain.model.SettingConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MusicPlayer(
    private val context: Context,
    private val settingsManager: SettingsManager
) {
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var previousVolume: Int = 0

    private var alarmUri: Uri? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            settingsManager.getSettings().collectLatest {
                alarmUri = it.musicItem.uri.toUri()
            }
        }
    }
    fun setAlarmUri(uri: Uri){
        alarmUri = uri
    }

    fun startMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                println(it.isPlaying)
                return
            }
        }
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        // Set alarm volume to max
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )

        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, alarmUri ?: SettingConfig().musicItem.uri.toUri())
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM) // Ensures it plays as an alarm
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setOnPreparedListener { start() }
            setOnCompletionListener {
                stopMusic()
            }
            prepareAsync()
        }
    }

    fun stopMusic() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null

        // Restore previous volume
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousVolume, 0)

    }
}