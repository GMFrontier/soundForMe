package com.frommetoyou.soundforme.presentation.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.model.Classification
import com.frommetoyou.soundforme.domain.model.Modes
import com.frommetoyou.soundforme.domain.model.SettingConfig
import com.frommetoyou.soundforme.presentation.navigation.MusicSelectionScreen
import com.frommetoyou.soundforme.presentation.ui.dialogs.PrivacyDialog
import com.frommetoyou.soundforme.presentation.ui.util.UiText
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingScreen(modifier: Modifier = Modifier, navController: NavController) {
    val viewModel = koinViewModel<HomeViewModel>()
    val adsViewModel = koinViewModel<AdsViewModel>()
    val settings = viewModel.settings.collectAsState()
    val rewardedAd by adsViewModel.rewardedAd.collectAsState()

    val detectionMode =
        if (settings.value.detectionMode == Classification.Whistle) UiText.StringResource(
            R.string.whistles
        ) else UiText.StringResource(R.string.claps)

    val context = LocalContext.current

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
            .padding(top = 56.dp)
            .padding(horizontal = 16.dp)
    ) {

        Card(
            colors = CardDefaults.cardColors(
                containerColor = CardDefaults.cardColors().containerColor.copy(alpha = 0.5f) // Apply 50% transparency
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),

                ) {

                Text(
                    text = UiText.StringResource(R.string.settings_title).asString(
                        LocalContext.current
                    ),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = UiText.StringResource(R.string.whistle_or_clap)
                                .asString(
                                    LocalContext.current
                                ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = UiText.StringResource(R.string.choose_mode).asString(
                                LocalContext.current
                            ),
                            fontSize = 13.sp,
                            color = Color(0xFFB5B9BC)
                        )
                    }
                    Button(
                        modifier = Modifier
                            .height(32.dp)
                            .weight(.35f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        ),
                        onClick = {
                            viewModel.saveSettings(
                                settings = settings.value.copy(detectionMode = !settings.value.detectionMode)
                            )
                        }
                    ) {
                        Text(
                            text = detectionMode.asString(LocalContext.current),
                            fontSize = 14.sp
                        )
                    }
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
                        text = UiText.StringResource(R.string.select_audio).asString(
                            LocalContext.current
                        ),
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )

                    val coroutineScope = rememberCoroutineScope()
                    Button(
                        enabled = rewardedAd != null,
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
                                adsViewModel.showRewardedAd(context) {
                                    navController.navigate(MusicSelectionScreen)
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier,
                        ) {
                            if (rewardedAd == null)

                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .align(Alignment.Center),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            Text(
                                text = UiText.StringResource(R.string.seleccionar)
                                    .asString(
                                        LocalContext.current
                                    ), fontSize = 14.sp
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

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
            }
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

@Composable
fun SettingItem(
    text: String,
    options: List<Modes>,
    selectedItem: Modes,
    onClick: (item: Modes) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text
        )
        Box {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.pointerInput(true) {
                    detectTapGestures {
                        expanded = true
                    }
                }
            ) {
                Text(
                    text = selectedItem.text.asString(
                        LocalContext.current
                    )
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24),
                    contentDescription = "DropDown Icon",
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                offset = DpOffset(x = 0.dp, y = 8.dp)
            ) {
                options.forEachIndexed { index, item ->
                    DropdownMenuItem(text = {
                        Text(text = item.text.asString(LocalContext.current))
                    },
                        onClick = {
                            expanded = false
                            onClick(item)
                        })
                }
            }
        }
    }
}

fun openPrivacyPolicy(context: Context) {
    val openURL = Intent(Intent.ACTION_VIEW)
    openURL.data =
        Uri.parse("https://franciscogmontero.wixsite.com/soundforme/")
    context.startActivity(openURL, null)
}