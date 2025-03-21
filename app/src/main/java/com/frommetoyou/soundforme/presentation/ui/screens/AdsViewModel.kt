package com.frommetoyou.soundforme.presentation.ui.screens

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.frommetoyou.soundforme.BuildConfig
import com.frommetoyou.soundforme.R
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdsViewModel(
) : ViewModel() {

    private val _rewardedAd = MutableStateFlow<RewardedAd?>(null)
    val rewardedAd: StateFlow<RewardedAd?> = _rewardedAd

    private val _nativeAd = MutableStateFlow<NativeAd?>(null)
    val nativeAd: StateFlow<NativeAd?> = _nativeAd

    private val _userOwnsPremium = MutableStateFlow<Boolean?>(null)
    val userOwnsPremium: StateFlow<Boolean?> = _userOwnsPremium

    private val _openPurchaseDialog = MutableStateFlow(false)
    val openPurchaseDialog: StateFlow<Boolean> = _openPurchaseDialog

    private var billingClient: BillingClient? = null

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                _userOwnsPremium.value = purchases.any { item -> item.products.contains("premium") }
                _openPurchaseDialog.value = true
            }
        }

    fun setPurchaseDialogShown() {
        _openPurchaseDialog.value = false
    }

    fun startBillingConnection(context: Context) {
        if (billingClient == null)
            billingClient =
                BillingClient.newBuilder(context)
                    .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                            .enableOneTimeProducts().build()
                    )
                    .setListener(purchasesUpdatedListener)
                    .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkUserOwnsPremium()
                } else {
                    _userOwnsPremium.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    fun checkUserOwnsPremium() = viewModelScope.launch {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP) // Check one-time purchases
            .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val ownsPremium = purchases.any { it.products.contains("premium") }
                _userOwnsPremium.value = ownsPremium
            }
        }
    }

    fun processPurchases(activity: Activity) = viewModelScope.launch {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        withContext(Dispatchers.IO) {
            billingClient?.queryProductDetails(params.build()).also {
                it?.productDetailsList?.firstOrNull()?.let { product ->
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    billingClient?.launchBillingFlow(activity, billingFlowParams)
                }

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingClient?.endConnection()
    }

    fun loadRewardedAd(context: Context) = viewModelScope.launch {
        if (rewardedAd.value == null) {
            val id = if(BuildConfig.DEBUG) R.string.music_ad_id_test else R.string.music_ad_id
            RewardedAd.load(
                context,
                context.getString(id),
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

    }

    fun loadNativeAd(context: Context) = viewModelScope.launch {
        val id = if(BuildConfig.DEBUG) R.string.native_ad_id_test else R.string.native_ad_id
        if (_nativeAd.value == null) {
            val adLoader = AdLoader.Builder(
                context,
                context.getString(id)
            )
                .forNativeAd { ad: NativeAd ->
                    _nativeAd.value = ad
                }
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
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
