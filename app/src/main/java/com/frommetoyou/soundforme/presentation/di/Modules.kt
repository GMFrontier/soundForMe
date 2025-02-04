package com.frommetoyou.soundforme.presentation.di

import com.frommetoyou.soundforme.domain.use_case.AudioClassifierUseCase
import com.frommetoyou.soundforme.domain.use_case.MusicPlayer
import com.frommetoyou.soundforme.presentation.ui.screens.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.tensorflow.lite.task.audio.classifier.AudioClassifier

val modules = module {
    single {
        val modelPath = "sound_class.tflite"
        val classifier =
            AudioClassifier.createFromFile(androidContext(), modelPath)
        classifier
    }
    single {
        MusicPlayer(get())
    }
    single {
        AudioClassifierUseCase(get())
    }
    viewModel { HomeViewModel(get(), get()) }
}