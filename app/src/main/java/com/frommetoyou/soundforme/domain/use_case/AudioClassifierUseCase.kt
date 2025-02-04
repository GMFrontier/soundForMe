package com.frommetoyou.soundforme.domain.use_case

import android.media.AudioRecord
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

enum class Classification {
    WHISTLE,
    CLAP,
    UNKNOWN
}

class AudioClassifierUseCase(
    private val classifier: AudioClassifier
) {
    private val classificationMap = mapOf(
        "Whistling" to Classification.WHISTLE,
        "Whistle" to Classification.WHISTLE,
        "Clap" to Classification.CLAP,
        "Clapping" to Classification.CLAP,
        "Hands" to Classification.CLAP
    )

    private var probabilityThreshold: Float = 0.3f

    private var tensor: TensorAudio? = null

    private var record: AudioRecord? = null

    private var detector: Timer? = null

    fun startDetector(): Flow<Classification> {
        return callbackFlow {
            tensor = classifier.createInputTensorAudio()
            record = classifier.createAudioRecord()
            record?.startRecording()
            if(detector == null) {
                detector = Timer()
            }
            detector?.apply {
                scheduleAtFixedRate(1, 500) {
                    tensor?.load(record)
                    val output = classifier.classify(tensor)
                    val filteredModelOutput = output[0].categories.filter {
                        it.score > probabilityThreshold
                    }
                    val action = filteredModelOutput
                        .asSequence()
                        .mapNotNull { item ->
                            classificationMap.entries.find {
                                it.key.equals(item.label, ignoreCase = true)
                            }?.value
                        }
                        .firstOrNull() ?: Classification.UNKNOWN

                    trySend(action)
                }
                awaitClose {
                    stopDetector()
                }
            }
        }
    }

    fun stopDetector() {
        detector?.cancel()
        detector?.purge()
        detector = null

        record?.stop()

    }
}