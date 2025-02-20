package com.frommetoyou.soundforme.presentation.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdsViewModel(
) : ViewModel() {

    private val _rewardedAd = MutableStateFlow<RewardedAd?>(null)
    val rewardedAd: StateFlow<RewardedAd?> = _rewardedAd

    fun loadRewardedAd(context: Context) = viewModelScope.launch {
        RewardedAd.load(
            context,
            "ca-app-pub-3940256099942544/5224354917",
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    _rewardedAd.value = null
                }

                override fun onAdLoaded(p0: RewardedAd) {
                    super.onAdLoaded(p0)
                    _rewardedAd.value = p0
                }
            })
    }


    fun showRewardedAd(context: Context, onAdDissmised: () -> Unit) =
        viewModelScope.launch {
            _rewardedAd.value?.let {
                it.show(context as Activity, OnUserEarnedRewardListener {
                    viewModelScope.launch {
                        loadRewardedAd(context)
                    }
                    onAdDissmised()
                    _rewardedAd.value = null
                })
            }
        }
}
