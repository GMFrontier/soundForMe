package com.frommetoyou.soundforme.presentation.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.presentation.ui.util.UiText

@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDenied: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = if (isPermanentlyDenied)
                    onOkClick
                else
                    onGoToAppSettingsClick
            ) {
                Text(
                    text = if (isPermanentlyDenied)
                        UiText.StringResource(R.string.grant_permission)
                            .asString(
                                LocalContext.current
                            ) else UiText.StringResource(
                        R.string.ok
                    ).asString(LocalContext.current),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        /*dismissButton = {
            TextButton(
                onClick = onOkClick
            ) {
                Text("Confirm")
            }
        },*/
        title = {
            Text(
                text = UiText.StringResource(R.string.permissions_required)
                    .asString(
                        LocalContext.current
                    ),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = permissionTextProvider.getDescription(isPermanentlyDenied).asString(
                    LocalContext.current),
                fontWeight = FontWeight.Bold,
            )
        },
        modifier = modifier
    )
}

interface PermissionTextProvider {
    fun getDescription(isPermanentlyDenied: Boolean) : UiText.StringResource
}

class RecordPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDenied: Boolean): UiText.StringResource {
        return if(isPermanentlyDenied){
            UiText.StringResource(R.string.record_permanently_declined)
        } else
            UiText.StringResource(R.string.ask_record_permission)
    }
}

class NotificationPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDenied: Boolean): UiText.StringResource {
        return if(isPermanentlyDenied){
            UiText.StringResource(R.string.notification_permanently_declined)
        } else
            UiText.StringResource(R.string.ask_notification_permission)
    }
}