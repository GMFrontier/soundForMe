package com.frommetoyou.soundforme.domain.model

import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.presentation.ui.util.UiText

enum class Modes(val text: UiText) {
    Off(UiText.StringResource(R.string.off)),
    Intermittent(UiText.StringResource(R.string.intermittent)),
    Fixed(UiText.StringResource(R.string.fixed));

    companion object {
        val availableModes = entries.toList()
    }
}