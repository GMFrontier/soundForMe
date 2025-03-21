package com.frommetoyou.soundforme.domain.model

import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.model.Modes.entries
import kotlinx.serialization.Serializable

@Serializable
sealed class Classification {
    abstract val text: Int

    @Serializable
    data object Whistle : Classification() {
        override val text: Int
            get() = R.string.whistles
    }

    @Serializable
    data object Clap : Classification() {
        override val text: Int
            get() = R.string.claps
    }

    @Serializable
    data object Unknown : Classification() {
        override val text: Int
            get() = R.string.whistles
    }

    @Serializable
    data object Cat : Classification() {
        override val text: Int
            get() = R.string.cats
    }

    @Serializable
    data object Dog : Classification() {
        override val text: Int
            get() = R.string.dogs
    }

    @Serializable
    data object Animal : Classification() {
        override val text: Int
            get() = R.string.animal
    }

    @Serializable
    data object BabyCry : Classification() {
        override val text: Int
            get() = R.string.babycry
    }

    @Serializable
    data object Cough : Classification() {
        override val text: Int
            get() = R.string.coughs
    }

    @Serializable
    data object Dishes : Classification() {
        override val text: Int
            get() = R.string.dishes
    }
    @Serializable
    data object Laughter : Classification() {
        override val text: Int
            get() = R.string.laughter
    }


    companion object {
        val allClassifications = listOf(
            Whistle, Clap, Cat, Dog, Animal, BabyCry, Cough, Dishes, Laughter
        )
//        val allClassifications: List<Classification> = Classification::class.sealedSubclasses.filter { it.objectInstance != Unknown }.mapNotNull { it.objectInstance }
        val premiumClassifications = listOf(BabyCry,Cough,Dishes,Laughter)
    }

/*
    operator fun not(): Classification {
        return if (this is Whistle) Clap else Whistle
    }*/
}