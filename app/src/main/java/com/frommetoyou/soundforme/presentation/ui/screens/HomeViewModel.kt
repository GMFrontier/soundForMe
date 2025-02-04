package com.frommetoyou.soundforme.presentation.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frommetoyou.soundforme.domain.use_case.AudioClassifierUseCase
import com.frommetoyou.soundforme.domain.use_case.Classification
import com.frommetoyou.soundforme.domain.use_case.MusicPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val audioClassifierUseCase: AudioClassifierUseCase,
    private val musicPlayer: MusicPlayer
) : ViewModel() {

    private val _outputStr = MutableStateFlow(Classification.UNKNOWN)
    val outputStr: StateFlow<Classification> = _outputStr // Expose as immutable StateFlow


    fun startDetector() {
        viewModelScope.launch {
            audioClassifierUseCase.startDetector().collectLatest { result ->
                _outputStr.value = result
                println("HomeViewModel")
                if(result == Classification.CLAP || result == Classification.WHISTLE) {
                    musicPlayer.startMusic()
                }
            }
        }
    }

    fun stopDetector() {
        audioClassifierUseCase.stopDetector()
        musicPlayer.stopMusic()
    }
}
