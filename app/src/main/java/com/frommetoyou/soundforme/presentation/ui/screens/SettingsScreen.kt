package com.frommetoyou.soundforme.presentation.ui.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.model.Classification
import com.frommetoyou.soundforme.domain.model.Modes
import com.frommetoyou.soundforme.presentation.navigation.MusicSelectionScreen
import com.frommetoyou.soundforme.presentation.ui.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingScreen(modifier: Modifier = Modifier, navController: NavController) {
    val viewModel = koinViewModel<HomeViewModel>()
    val settings = viewModel.settings.collectAsState()

    val detectionMode =
        if (settings.value.detectionMode == Classification.Whistle) UiText.StringResource(
            R.string.whistles
        ) else UiText.StringResource(R.string.claps)

    val screenDimensions = LocalConfiguration.current

    Image(
        painter = painterResource(R.drawable.home_background7),
        contentDescription = "background",
        modifier = Modifier
            .offset(x = 100.dp)
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
        Text(
            text = UiText.StringResource(R.string.settings_title).asString(
                LocalContext.current
            ),
            fontSize = 25.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
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
                    fontSize = 14.sp,
                    color = Color(0xFFB5B9BC)
                )
            }
            Button(
                modifier = Modifier
                    .height(32.dp)
                    .weight(.3f),
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

            Button(
                modifier = Modifier
                    .height(32.dp)
                    .weight(.3f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ),
                onClick = {
                    navController.navigate(MusicSelectionScreen)
                }
            ) {
                Text(
                    text = UiText.StringResource(R.string.seleccionar).asString(
                        LocalContext.current
                    ), fontSize = 14.sp
                )
            }
        }

        Text(
            text = "${UiText.StringResource(R.string.current_audio).asString(
                    LocalContext . current
        )
    } ${settings.value.musicItem.uri.split("/")[settings.value.musicItem.uri.split("/").size-1]}",
            fontSize = 14.sp,
            color = Color(0xFFB5B9BC)
        )

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