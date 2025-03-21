package com.frommetoyou.soundforme.presentation.ui.screens

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.RECORD_AUDIO
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.graphics.withRotation
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.model.ActiveButton
import com.frommetoyou.soundforme.domain.model.SettingConfig
import com.frommetoyou.soundforme.presentation.ui.composables.NativeAdContent
import com.frommetoyou.soundforme.presentation.ui.composables.NativeAdViewHelper
import com.frommetoyou.soundforme.presentation.ui.dialogs.NotificationPermissionTextProvider
import com.frommetoyou.soundforme.presentation.ui.dialogs.PermissionDialog
import com.frommetoyou.soundforme.presentation.ui.dialogs.RecordPermissionTextProvider
import com.frommetoyou.soundforme.presentation.ui.util.UiText
import com.frommetoyou.soundforme.presentation.ui.util.findAndroidActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
    val settings = viewModel.settings.collectAsState()
    val dialogQueue = viewModel.visiblePermissionDialogQueue
    val adsViewModel = koinViewModel<AdsViewModel>()

    val nativeAd by adsViewModel.nativeAd.collectAsState()
    val ownsPremium by adsViewModel.userOwnsPremium.collectAsState()

    val context = LocalContext.current

    val activity = LocalContext.current.findAndroidActivity()

    val permissionsToRequest =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(RECORD_AUDIO, POST_NOTIFICATIONS)
        } else {
            arrayOf(RECORD_AUDIO)
        }

    val recordPermissionState =
        rememberPermissionState(permission = RECORD_AUDIO) { isGranted ->
            if (isGranted) handleDetector(settings, viewModel, activity!!)
            else viewModel.stopDetector(context)
        }
    val notificationPermissionState =
        rememberPermissionState(permission = POST_NOTIFICATIONS) { isGranted ->
            if (isGranted) handleDetector(settings, viewModel, activity!!)
            else viewModel.stopDetector(context)
        }

    viewModel.setPermissionState(recordPermissionState)
    viewModel.setNotifPermissionState(notificationPermissionState)
    val permissionEvent = viewModel.permissionEvent.collectAsState().value

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsToRequest.forEach { permission ->
                viewModel.onPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                )
            }
        }
    )

    LaunchedEffect(permissionEvent) {
        when (permissionEvent) {
            is HomeViewModel.Event.PermissionRequest -> {
                multiplePermissionResultLauncher.launch(permissionsToRequest)
            }

            else -> {}
        }
    }

    val activeMode = settings.value.active

    var circleCenter by remember {
        mutableStateOf(Offset.Zero)
    }
    var radius by remember {
        mutableFloatStateOf(0f)
    }
    val modeText = if (settings.value.active == ActiveButton.Off) {
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
    val premiumIcon = rememberVectorPainter(
        image = ImageVector.vectorResource(R.drawable.ic_crown),
    )
    Box(modifier = Modifier.fillMaxSize()) {


        if (ownsPremium == false) {
            nativeAd?.let {
                NativeAdViewHelper(
                    ad = it,
                    horizontalPadding = 16.dp
                ) { ad, view ->
                    NativeAdContent(ad, view)
                }
            }
        }

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
                            handleDetector(settings, viewModel, it)
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

                if (ownsPremium == true) {
                    with(premiumIcon) {
                        translate(
                            left = circleCenter.x * 5 / 3,
                            top = circleCenter.y - iconSize.height * 2.6f
                        ) {
                            withRotation(
                                degrees = 45f
                            ) {
                                draw(
                                    size = iconSize,
                                )
                            }
                        }
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

                    save()
                    rotate((startAngle + i * angleStep) + 90, x, y)
                    drawText(text[i].toString(), x, y, textPaint)
                    restore()
                }
            }
        }
    }
    dialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    RECORD_AUDIO -> RecordPermissionTextProvider()
                    POST_NOTIFICATIONS -> NotificationPermissionTextProvider()
                    else -> return@forEach
                },
                isPermanentlyDenied = activity?.shouldShowRequestPermissionRationale(
                    permission
                ) ?: true,
                onDismiss = viewModel::dismissDialog,
                onOkClick = {
                    viewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(arrayOf(permission))
                },
                onGoToAppSettingsClick = {
                    activity?.openAppSettings()
                }
            )
        }

    OpenPrivacyDialog(viewModel, context, settings)
}

private fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
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
