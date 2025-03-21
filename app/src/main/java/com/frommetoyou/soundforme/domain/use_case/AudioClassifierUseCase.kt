package com.frommetoyou.soundforme.domain.use_case

import android.media.AudioRecord
import android.util.Log
import com.frommetoyou.soundforme.domain.model.Classification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class AudioClassifierUseCase(
    private val classifier: AudioClassifier
) {
    private val classificationMap = mapOf(
        "Whistling" to Classification.Whistle,
        "Whistle" to Classification.Whistle,

        "Clap" to Classification.Clap,
        "Clapping" to Classification.Clap,

        "Animal" to Classification.Animal,
        "Domestic animals" to Classification.Animal,
        "Wild animals" to Classification.Animal,
        "pets" to Classification.Animal,

        "Purr" to Classification.Cat,
        "Cat" to Classification.Cat,

        "Dog" to Classification.Dog,
        "Bark" to Classification.Dog,
        "Bow-wow" to Classification.Dog,

        "silverware" to Classification.Dishes,
        "Dishes" to Classification.Dishes,

        "Crying" to Classification.BabyCry,
        "Baby cry" to Classification.BabyCry,
        "Baby cry" to Classification.BabyCry,
        "infant cry" to Classification.BabyCry,

        "Cough" to Classification.Cough,
        "Throat clearing" to Classification.Cough,

        "Laughter" to Classification.Laughter,
        "Belly laugh" to Classification.Laughter,
        "Belly laugh" to Classification.Laughter,
        "Baby laughter" to Classification.Laughter,
        "Snicker" to Classification.Laughter,
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
            if (detector == null) {
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
                        .firstOrNull() ?: Classification.Unknown

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