package com.frommetoyou.soundforme.domain.use_case

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.frommetoyou.soundforme.presentation.ui.screens.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

class ScreenUnlockReceiver : BroadcastReceiver() {
    private var viewModel: HomeViewModel? = null

    override fun onReceive(context: Context, intent: Intent) {
        viewModel = KoinJavaComponent.getKoin().get()
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel?.stopDetector(context)

            }
        }
    }
}