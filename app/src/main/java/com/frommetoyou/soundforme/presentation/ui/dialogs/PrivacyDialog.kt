package com.frommetoyou.soundforme.presentation.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.presentation.ui.util.UiText

@Composable
fun PrivacyDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    linkText: String,
    link: () -> Unit,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
           Column(
               horizontalAlignment = Alignment.CenterHorizontally
           ) {
               Text(text = dialogText, textAlign = TextAlign.Center)
               Row(
                   verticalAlignment = Alignment.CenterVertically
               ){
                   Text("•")
                   TextButton(
                       modifier = Modifier.padding(16.dp),
                       onClick = {
                           link()
                       }
                   ) {
                       Text(linkText)
                   }
               }
           }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(UiText.StringResource(R.string.confirm).asString(
                    LocalContext.current))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(UiText.StringResource(R.string.decline).asString(
                    LocalContext.current))
            }
        }
    )
}