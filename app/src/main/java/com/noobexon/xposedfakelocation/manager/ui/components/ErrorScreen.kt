package com.noobexon.xposedfakelocation.manager.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.noobexon.xposedfakelocation.R

@Composable
fun ErrorScreen(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.module_not_active)) },
        text = {
            Text(stringResource(R.string.module_not_active_description))
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
