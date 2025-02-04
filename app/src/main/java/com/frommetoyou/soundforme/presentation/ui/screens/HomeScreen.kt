package com.frommetoyou.soundforme.presentation.ui.screens

import android.graphics.Paint
import android.text.Layout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.use_case.Classification
import com.frommetoyou.soundforme.presentation.ui.components.ActiveButton
import org.koin.androidx.compose.koinViewModel
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<HomeViewModel>()

    var activeMode by remember {
        mutableStateOf<ActiveButton>(ActiveButton.Off)
    }
    var circleCenter by remember {
        mutableStateOf(Offset.Zero)
    }
    var radius by remember {
        mutableFloatStateOf(0f)
    }
    val outputSrt by viewModel.outputStr.collectAsStateWithLifecycle()

    when (outputSrt) {
        Classification.WHISTLE, Classification.CLAP -> {
            println("HomeScreen")
        }

        else -> {
            println("NADA DETECTADO $outputSrt")
        }
    }
    DisposableEffect(activeMode) {
        if (activeMode == ActiveButton.On) {
            println("Iniciando detección")

            viewModel.startDetector()
        }
        onDispose {
            println("Deteniendo detección")
            viewModel.stopDetector()
        }
    }
    Image(
        painter = painterResource(R.drawable.home_background7),
        contentDescription = "background",
        modifier = Modifier
            .fillMaxSize()
            .offset(x = (-100).dp)
            .wrapContentSize(unbounded = true)
            .graphicsLayer {
                scaleX = -1f
            },
        contentScale = ContentScale.None,
    )
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val activeColor =
        if (activeMode == ActiveButton.On) Color(0xFFFFFFFF) else
            MaterialTheme.colorScheme.error
    val iconColor =
        if (activeMode == ActiveButton.On) MaterialTheme.colorScheme.primary else
            MaterialTheme.colorScheme.onError
    val shadowColor =
        if (activeMode == ActiveButton.On) MaterialTheme.colorScheme.primary
            .toArgb() else MaterialTheme.colorScheme.error.toArgb()
    val icon = rememberVectorPainter(
        image = ImageVector.vectorResource(R.drawable.on_off),

        )
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text =
                if (activeMode == ActiveButton.On) "Detección activada \n${viewModel.outputStr.collectAsState().value}"
                else "Detección desactivada",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
        }
        Canvas(modifier = Modifier
            .fillMaxSize()

            .pointerInput(true) {
                detectTapGestures {
                    val distance = sqrt(
                        (it.x - circleCenter.x).pow(2) +
                                (it.y - circleCenter.y).pow(2)
                    )
                    if (distance <= radius) {
                        activeMode = !activeMode
                    }
                }
            }) {
            radius = (screenWidth.dp / 2 - 30.dp).toPx()

            circleCenter = Offset(
                this.center.x,
                (this.center.y * 2 + 40.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.apply {
                drawCircle(
                    circleCenter.x,
                    circleCenter.y,
                    radius,
                    Paint().apply {
                        color = activeColor.toArgb()
                        setShadowLayer(
                            200f,
                            0f,
                            0f,
                            shadowColor
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
            }
        }
    }

}