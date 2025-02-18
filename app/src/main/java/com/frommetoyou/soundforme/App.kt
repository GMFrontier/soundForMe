package com.frommetoyou.soundforme

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.datastore.dataStore
import com.frommetoyou.soundforme.data.storage.SettingConfigSerializer
import com.frommetoyou.soundforme.domain.use_case.DETECTOR_CHANNEL_ID
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

val Context.datastore by dataStore("settings.json", SettingConfigSerializer)

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DETECTOR_CHANNEL_ID,
                this.getString(R.string.detector_notification),
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        startKoin {
            androidContext(this@App)
            modules(com.frommetoyou.soundforme.presentation.di.modules)
        }
    }
}