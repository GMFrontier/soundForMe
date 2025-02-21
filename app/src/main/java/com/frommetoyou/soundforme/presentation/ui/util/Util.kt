package com.frommetoyou.soundforme.presentation.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.android.gms.ads.identifier.AdvertisingIdClient

fun Context.findAndroidActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@WorkerThread
fun getAdvertisingId(context: Context): String? {
    return try {
        val idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
        val isLimitAdTrackingEnabled = idInfo.isLimitAdTrackingEnabled
        Log.d("AppLog", "isLimitAdTrackingEnabled? $isLimitAdTrackingEnabled")
        idInfo.id
    } catch (e: Exception) {
        Log.d("AppLog", "error:$e")
        e.printStackTrace()
        null
    }
}