package com.frommetoyou.soundforme.data.storage

import androidx.datastore.core.Serializer
import com.frommetoyou.soundforme.domain.model.SettingConfig
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object SettingConfigSerializer : Serializer<SettingConfig> {
    override val defaultValue: SettingConfig
        get() = SettingConfig()

    override suspend fun readFrom(input: InputStream): SettingConfig {
        return try {
            Json.decodeFromString(
                deserializer = SettingConfig.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: SettingConfig, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = SettingConfig.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }

}