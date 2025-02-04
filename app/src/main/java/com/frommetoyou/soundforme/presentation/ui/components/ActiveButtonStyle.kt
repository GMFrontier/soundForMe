package com.frommetoyou.soundforme.presentation.ui.components

sealed class ActiveButton {
    object On: ActiveButton()
    object Off: ActiveButton()

    operator fun not(): ActiveButton {
        return if(this is On) Off else On
    }
}