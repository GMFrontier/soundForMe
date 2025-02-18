package com.frommetoyou.soundforme.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ActiveButton {
    @Serializable
    data object On: ActiveButton()
    @Serializable
    data object Off: ActiveButton()

    operator fun not(): ActiveButton {
        return if(this is On) Off else On
    }
}