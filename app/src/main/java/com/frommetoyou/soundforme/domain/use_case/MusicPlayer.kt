package com.frommetoyou.soundforme.domain.use_case

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.coroutines.coroutineContext

class MusicPlayer(
    private val context: Context
) {

    var mediaPlayer: MediaPlayer? = null
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    var previousVolume: Int = 0

    val alarmUri: Uri =  RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ?: Settings.System.DEFAULT_ALARM_ALERT_URI

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
            setDataSource(context, alarmUri)
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