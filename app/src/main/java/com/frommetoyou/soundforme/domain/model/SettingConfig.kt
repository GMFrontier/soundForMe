package com.frommetoyou.soundforme.domain.model

import android.media.RingtoneManager
import kotlinx.serialization.Serializable

@Serializable
data class SettingConfig(
    val active: ActiveButton = ActiveButton.Off,
    val detectionMode: Classification = Classification.Whistle,
    val flashMode : Modes = Modes.Intermittent,
    val vibrationMode: Modes = Modes.Intermittent,
    val musicItem: MusicItem = MusicItem(),
    val isPrivacyAccepted: Boolean = false
)