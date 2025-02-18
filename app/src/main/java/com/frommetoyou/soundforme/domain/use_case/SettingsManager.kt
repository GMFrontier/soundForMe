package com.frommetoyou.soundforme.domain.use_case

import android.content.Context
import com.frommetoyou.soundforme.datastore
import com.frommetoyou.soundforme.domain.model.SettingConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach

class SettingsManager(
    private val context: Context
) {
    suspend fun saveSettings(settings: SettingConfig) {
        context.datastore.updateData {
            settings
        }
    }
    fun getSettings(): Flow<SettingConfig> {
        return context.datastore.data
    }
}