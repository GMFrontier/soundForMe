package com.frommetoyou.soundforme.presentation.ui.composables

import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.presentation.ui.util.UiText
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdViewHelper(
    ad: NativeAd,
    horizontalPadding: Dp = 0.dp,
    adContent: @Composable (ad: NativeAd, contentView: View) -> Unit,
) {
    val contentViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val adViewId by remember { mutableIntStateOf(View.generateViewId()) }
    AndroidView(
        modifier = Modifier
            .padding(
                top = 16.dp,
                start = horizontalPadding,
                end = horizontalPadding
            )
            .zIndex(1f),
        factory = { context ->
            val contentView = ComposeView(context).apply {
                id = contentViewId
            }
            NativeAdView(context).apply {
                id = adViewId
                addView(contentView)
            }
        },
        update = { view ->
            val adView = view.findViewById<NativeAdView>(adViewId)
            val contentView = view.findViewById<ComposeView>(contentViewId)

            adView.setNativeAd(ad)
            adView.callToActionView = contentView

            val adAttributionView =
                contentView.findViewWithTag<View>("AdAttributionTag")
            adView.advertiserView = adAttributionView

            contentView.setContent { adContent(ad, contentView) }
        }
    )
}

@Composable
fun NativeAdContent(
    ad: NativeAd,
    view: View
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = CardDefaults.cardColors().containerColor.copy(
                alpha = 0.5f
            )
        )
    ) {

            Column {
                Card(
                    modifier = Modifier
                        .padding(2.dp) ,
                    shape = RoundedCornerShape(2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardDefaults.cardColors().containerColor.copy(
                            red = 255f,
                            green = 255f,
                            blue = 255f
                        )
                    ),
                ) {
                    Text(
                        text = UiText.StringResource(R.string.ad)
                            .asString(
                                LocalContext.current
                            ),
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .align(Alignment.CenterHorizontally)
                            .testTag("AdAttributionTag")
                    )
                }
                Row(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp
                    ),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        ad.icon?.drawable?.let { drawable ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .drawWithContent {
                                        drawIntoCanvas {
                                            drawable.setBounds(
                                                0,
                                                0,
                                                size.width.toInt(),
                                                size.height.toInt()
                                            )
                                            drawable.draw(it.nativeCanvas)
                                        }
                                    }
                            )
                        }
                    }

                    Column {
                        Text(
                            text = ad.headline.toString(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                        )
                        ad.starRating?.let { rating ->
                            StarRating(rating = rating.toFloat())
                        }

                        Text(
                            text = (ad.body ?: ad.advertiser).toString(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                        )
                    }
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    content = { Text(text = ad.callToAction.toString()) },
                    onClick = { view.performClick() },
                )
            }
        }
    }
