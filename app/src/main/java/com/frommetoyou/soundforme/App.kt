package com.frommetoyou.soundforme

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(com.frommetoyou.soundforme.presentation.di.modules)
        }
    }
}