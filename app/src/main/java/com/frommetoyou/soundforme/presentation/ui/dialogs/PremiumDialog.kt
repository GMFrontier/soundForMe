package com.frommetoyou.soundforme.presentation.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.presentation.ui.util.UiText

@Composable
fun PremiumDialog(
    modifier: Modifier = Modifier,
    okClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = okClick
            ) {
                Text(
                    text = UiText.StringResource(R.string.im_the_best)
                        .asString(
                            LocalContext.current
                        ),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        title = {
            Text(
                text = UiText.StringResource(R.string.thank_you_1)
                    .asString(
                        LocalContext.current
                    ),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = UiText.StringResource(R.string.thank_you_2)
                        .asString(
                            LocalContext.current
                        ),
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_crown),
                    contentDescription = "Yay",
                    modifier = Modifier.size(46.dp),
                    tint = Color.Unspecified
                )
            }
        },
        modifier = modifier
    )
}