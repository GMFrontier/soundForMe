package com.frommetoyou.soundforme.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Classification {
    @Serializable
    data object Whistle : Classification()
    @Serializable
    data object Clap : Classification()
    @Serializable
    data object Unknown : Classification()

    operator fun not(): Classification {
        return if (this is Whistle) Clap else Whistle
    }
}