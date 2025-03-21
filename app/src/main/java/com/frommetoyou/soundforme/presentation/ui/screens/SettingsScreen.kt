package com.frommetoyou.soundforme.presentation.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.model.Classification
import com.frommetoyou.soundforme.domain.model.Classification.Companion.allClassifications
import com.frommetoyou.soundforme.domain.model.Modes
import com.frommetoyou.soundforme.domain.model.SettingConfig
import com.frommetoyou.soundforme.presentation.navigation.MusicSelectionScreen
import com.frommetoyou.soundforme.presentation.ui.composables.DetectionItem
import com.frommetoyou.soundforme.presentation.ui.composables.NativeAdContent
import com.frommetoyou.soundforme.presentation.ui.composables.NativeAdViewHelper
import com.frommetoyou.soundforme.presentation.ui.composables.SettingItem
import com.frommetoyou.soundforme.presentation.ui.dialogs.PremiumDialog
import com.frommetoyou.soundforme.presentation.ui.dialogs.PrivacyDialog
import com.frommetoyou.soundforme.presentation.ui.util.AutoSizeText
import com.frommetoyou.soundforme.presentation.ui.util.UiText
import com.frommetoyou.soundforme.presentation.ui.util.findAndroidActivity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingScreen(modifier: Modifier = Modifier, navController: NavController) {
    val viewModel = koinViewModel<HomeViewModel>()
    val adsViewModel = koinViewModel<AdsViewModel>()
    val settings = viewModel.settings.collectAsState()
    val rewardedAd by adsViewModel.rewardedAd.collectAsState()
    val nativeAd by adsViewModel.nativeAd.collectAsState()
    val ownsPremium by adsViewModel.userOwnsPremium.collectAsState()

    val detectionMode = UiText.StringResource(settings.value.detectionMode.text)

    val context = LocalContext.current

    val scrollState = rememberScrollState()

    LaunchedEffect(ownsPremium) {
        if (ownsPremium == false) {
            adsViewModel.loadRewardedAd(context)
            adsViewModel.loadNativeAd(context)
        }
    }

    Image(
        painter = painterResource(R.drawable.home_background72),
        contentDescription = "background",
        modifier = Modifier
            .wrapContentSize(unbounded = true)
            .fillMaxSize(),
        contentScale = ContentScale.Crop,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(scrollState),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier.padding(top = 56.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardDefaults.cardColors().containerColor.copy(
                    alpha = 0.5f
                )
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {

                Text(
                    text = UiText.StringResource(R.string.settings_title)
                        .asString(
                            LocalContext.current
                        ),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))

                DetectionItem(
                    text = LocalContext.current.getString(R.string.choose_mode),
                    allClassifications,
                    selectedItem = settings.value.detectionMode,
                    ownsPremium = (ownsPremium == true)
                ) { item ->
                    viewModel.saveSettings(
                        settings.value.copy(
                            detectionMode = item
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))
                SettingItem(
                    text = LocalContext.current.getString(R.string.flash_mode),
                    Modes.availableModes,
                    selectedItem = settings.value.flashMode
                ) { item ->
                    viewModel.saveSettings(
                        settings.value.copy(
                            flashMode = item
                        )
                    )
                }
                SettingItem(
                    text = LocalContext.current.getString(R.string.vibration_mode),
                    Modes.availableModes,
                    selectedItem = settings.value.vibrationMode
                ) { item ->
                    viewModel.saveSettings(
                        settings.value.copy(
                            vibrationMode = item
                        )
                    )
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = UiText.StringResource(R.string.select_audio)
                            .asString(
                                LocalContext.current
                            ),
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )

                    val coroutineScope = rememberCoroutineScope()
                    Button(
                        enabled = (rewardedAd != null || ownsPremium == true),
                        modifier = Modifier
                            .height(32.dp)
                            .weight(.35f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        ),
                        onClick = {
                            viewModel.stopDetector(context = context)
                            coroutineScope.launch {
                                if (ownsPremium == true) {
                                    navController.navigate(MusicSelectionScreen)
                                } else {
                                    adsViewModel.showRewardedAd(context) {
                                        navController.navigate(
                                            MusicSelectionScreen
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier,
                        ) {
                            if (rewardedAd == null && ownsPremium == false)

                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .align(Alignment.Center),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            AutoSizeText(
                                text = UiText.StringResource(R.string.seleccionar)
                                    .asString(
                                        LocalContext.current
                                    ),
                                minTextSize = 10.sp,
                                maxTextSize = 14.sp,
                            )
                        }
                    }
                }


                Text(
                    text = "${
                        UiText.StringResource(R.string.current_audio).asString(
                            LocalContext.current
                        )
                    } ${
                        settings.value.musicItem.uri.split("/")[settings.value.musicItem.uri.split(
                            "/"
                        ).size - 1]
                    }",
                    fontSize = 13.sp,
                    color = Color(0xFFB5B9BC)
                )
            }
        }


        if (ownsPremium == false) {
            nativeAd?.let {
                NativeAdViewHelper(ad = it) { ad, view ->
                    NativeAdContent(ad, view)
                }
            }
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(24.dp))

        if (ownsPremium == false) {
            TextButton(
                onClick = {
                    context.findAndroidActivity()?.let {
                        adsViewModel.processPurchases(it)
                    }
                }
            ) {
                Text(
                    text = UiText.StringResource(R.string.buy_premium)
                        .asString(
                            LocalContext.current
                        ),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            TextButton(
                onClick = {
                    viewModel.setOpenPrivacyPolicy(true)
                }
            ) {
                Text(
                    text = UiText.StringResource(R.string.privacy_policy)
                        .asString(
                            LocalContext.current
                        ),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
            OpenPrivacyDialog(viewModel, context, settings)
            OpenPurchaseDialog(adsViewModel)
        }
    }
}

@Composable
private fun OpenPurchaseDialog(
    adsViewModel: AdsViewModel
) {
    when {
        adsViewModel.openPurchaseDialog.collectAsState().value -> {
            PremiumDialog(
                modifier = Modifier,
                okClick = {
                    adsViewModel.setPurchaseDialogShown()
                })
        }
    }
}

@Composable
fun OpenPrivacyDialog(
    viewModel: HomeViewModel,
    context: Context,
    settings: State<SettingConfig>,
) {
    when {
        viewModel.openPrivacyPolicy.collectAsState().value -> {
            PrivacyDialog(
                onDismissRequest = {
                    viewModel.stopDetector(context)
                    viewModel.saveSettings(
                        settings.value.copy(
                            isPrivacyAccepted = false,
                        )
                    )
                    viewModel.setOpenPrivacyPolicy(false)

                },
                onConfirmation = {
                    viewModel.saveSettings(
                        settings.value.copy(
                            isPrivacyAccepted = true,
                        )
                    )
                    viewModel.setOpenPrivacyPolicy(false)
                },
                dialogTitle = UiText.StringResource(R.string.privacy_policy)
                    .asString(
                        LocalContext.current
                    ),
                dialogText = UiText.StringResource(R.string.privacy_1).asString(
                    LocalContext.current
                ),
                icon = Icons.Default.Info,
                linkText = UiText.StringResource(R.string.read_privacy)
                    .asString(
                        LocalContext.current
                    ),
                link = {
                    openPrivacyPolicy(context)
                }
            )
        }
    }
}

fun openPrivacyPolicy(context: Context) {
    val openURL = Intent(Intent.ACTION_VIEW)
    openURL.data =
        Uri.parse("https://franciscogmontero.wixsite.com/soundforme/")
    context.startActivity(openURL, null)
}