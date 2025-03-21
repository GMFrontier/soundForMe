package com.frommetoyou.soundforme.presentation.di

import android.content.Context
import androidx.datastore.dataStore
import com.frommetoyou.soundforme.data.storage.SettingConfigSerializer
import com.frommetoyou.soundforme.domain.use_case.AudioClassifierUseCase
import com.frommetoyou.soundforme.domain.use_case.DetectorService
import com.frommetoyou.soundforme.domain.use_case.MusicPlayer
import com.frommetoyou.soundforme.domain.use_case.SettingsManager
import com.frommetoyou.soundforme.presentation.ui.screens.AdsViewModel
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
        SettingsManager(get())
    }
    single {
        MusicPlayer(get(), get())
    }
    single {
        AudioClassifierUseCase(get())
    }
    single {
        DetectorService()
    }
    viewModel {
        HomeViewModel(get())
    }
    single {
        AdsViewModel()
    }
}