package com.frommetoyou.soundforme.presentation.ui.screens

import android.Manifest.permission.RECORD_AUDIO
import android.app.Activity
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.model.ActiveButton
import com.frommetoyou.soundforme.domain.model.SettingConfig
import com.frommetoyou.soundforme.presentation.ui.util.UiText
import com.frommetoyou.soundforme.presentation.ui.util.findAndroidActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<HomeViewModel>()
    val adsViewModel = koinViewModel<AdsViewModel>()
    val settings = viewModel.settings.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        adsViewModel.loadRewardedAd(context)
    }

    val activity = LocalContext.current.findAndroidActivity()

    val recordPermissionState =
        rememberPermissionState(permission = RECORD_AUDIO) { isGranted ->
            if (isGranted) handleDetector(settings, viewModel, activity!!)
        }

    var activeMode = settings.value.active

    var circleCenter by remember {
        mutableStateOf(Offset.Zero)
    }
    var radius by remember {
        mutableFloatStateOf(0f)
    }
    val modeText = if(settings.value.active == ActiveButton.Off) {
        UiText.StringResource(
            R.string.detector_off
        ).asString(LocalContext.current)
    } else {
        UiText.StringResource(
            R.string.home_mode, arrayOf(settings.value.detectionMode)
        ).asString(LocalContext.current)
    }

    LaunchedEffect(settings.value.active) {
        activity?.let {
            if (settings.value.active == ActiveButton.On) viewModel.startDetector(
                it
            )
        }
    }
    Image(
        painter = painterResource(R.drawable.home_background72),
        contentDescription = "background",
        modifier = Modifier
            .fillMaxSize()
            .offset()
            .wrapContentSize(unbounded = true)
            .graphicsLayer {
                scaleX = -1f
            },
        contentScale = ContentScale.None,
    )
    val screenDimensions = LocalConfiguration.current
    val activeColor =
        if (activeMode == ActiveButton.On) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.error
    val iconColor =
        if (activeMode == ActiveButton.On) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onError
    val shadowColor =
        if (activeMode == ActiveButton.On) MaterialTheme.colorScheme.primary.toArgb() else MaterialTheme.colorScheme.error.toArgb()
    val icon = rememberVectorPainter(
        image = ImageVector.vectorResource(R.drawable.on_off),

        )
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier
            .fillMaxSize()

            .pointerInput(true) {
                detectTapGestures {
                    val distance = sqrt(
                        (it.x - circleCenter.x).pow(2) + (it.y - circleCenter.y).pow(
                            2
                        )
                    )
                    if (distance <= radius) {
                        activity?.let {

                            if (recordPermissionState.status.isGranted) {
                                handleDetector(settings, viewModel, it)
                            } else {
                                recordPermissionState.launchPermissionRequest()
                            }
                        }
                    }
                }
            }) {
            radius = (screenDimensions.screenWidthDp.dp / 2 - 30.dp).toPx()

            circleCenter = Offset(
                this.center.x, (this.center.y * 2 + 40.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.apply {
                drawCircle(
                    circleCenter.x,
                    circleCenter.y,
                    radius,
                    Paint().apply {
                        color = activeColor.toArgb()
                        setShadowLayer(
                            200f, 0f, 0f, shadowColor
                        )
                    },
                )
                val iconSize = Size(72.dp.toPx(), 72.dp.toPx())

                with(icon) {
                    translate(
                        left = circleCenter.x - iconSize.width / 2,
                        top = circleCenter.y - iconSize.height * 5 / 3
                    ) {
                        draw(
                            size = iconSize,
                            colorFilter = ColorFilter.tint(iconColor),
                        )
                    }
                }
                val text = modeText
                val textPaint = Paint().apply {
                    color = Color.White.toArgb()
                    textSize = 40f
                    typeface = Typeface.DEFAULT_BOLD
                }

                val textRadius = radius + 45f
                val angleStep = 45f / (text.length)
                val startAngle = -90f - ((angleStep * (text.length - 1)) / 2)

                for (i in text.indices) {
                    val angle =
                        Math.toRadians(startAngle + (i * angleStep).toDouble())

                    val x = (circleCenter.x + textRadius * cos(angle)).toFloat()
                    val y = (circleCenter.y + textRadius * sin(angle)).toFloat()

                    // Save the canvas state
                    save()
                    rotate((startAngle + i * angleStep) + 90, x, y)
                    drawText(text[i].toString(), x, y, textPaint)
                    restore()
                }
            }
        }
    }
    OpenPrivacyDialog(viewModel, context, settings)
}

private fun handleDetector(
    settings: State<SettingConfig>, viewModel: HomeViewModel, it: Activity
) {
    if (settings.value.active == ActiveButton.On) {
        viewModel.stopDetector(it)
    } else {
        viewModel.startDetector(it)
    }
}