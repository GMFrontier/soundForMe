package com.frommetoyou.soundforme.presentation.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.model.Classification
import com.frommetoyou.soundforme.domain.model.Classification.Companion.premiumClassifications
import com.frommetoyou.soundforme.domain.model.Modes
import com.frommetoyou.soundforme.presentation.ui.util.AutoSizeText
import com.frommetoyou.soundforme.presentation.ui.util.UiText

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


@Composable
fun DetectionItem(
    text: String,
    options: List<Classification>,
    selectedItem: Classification,
    ownsPremium: Boolean,
    onClick: (item: Classification) -> Unit
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
        AutoSizeText(
            text = text,
            minTextSize = 10.sp,
            maxTextSize = 14.sp,
            modifier = Modifier.weight(.8f)
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
                AutoSizeText(
                    text = UiText.StringResource(selectedItem.text).asString(
                        LocalContext.current
                    ),
                    minTextSize = 10.sp,
                    maxTextSize = 14.sp,
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
                    val isPremiumFeature = premiumClassifications.contains(item)
                    DropdownMenuItem(
                        enabled = (ownsPremium || !isPremiumFeature),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isPremiumFeature) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_crown),
                                        contentDescription = "Premium feature",
                                        tint = Color.Unspecified,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(end = 4.dp)
                                    )
                                }
                                Text(
                                    text = UiText.StringResource(item.text)
                                        .asString(LocalContext.current)
                                )
                            }
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