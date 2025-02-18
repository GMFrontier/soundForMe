package com.frommetoyou.soundforme.domain.model

import android.media.RingtoneManager
import kotlinx.serialization.Serializable

@Serializable
data class MusicItem(
    val id: Long = -1,
    val name: String = "Alarm",
    val uri: String = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString(),
)