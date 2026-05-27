package com.noobexon.xposedfakelocation.manager.ui.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noobexon.xposedfakelocation.R
import com.noobexon.xposedfakelocation.manager.ui.map.MapViewModel

@Composable
fun GoToPointDialog(
    mapViewModel: MapViewModel,
    onDismissRequest: () -> Unit,
    onGoToPoint: (latitude: Double, longitude: Double) -> Unit
) {
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val goToPointState = uiState.goToPointState

    val latitudeInput = goToPointState.first.value
    val longitudeInput = goToPointState.second.value
    val latitudeError = goToPointState.first.errorMessageResId
    val longitudeError = goToPointState.second.errorMessageResId

    AlertDialog(
        onDismissRequest = {
            mapViewModel.clearGoToPointInputs()
            onDismissRequest()
        },
        title = { Text(stringResource(R.string.go_to_point)) },
        text = {
            Column {
                OutlinedTextField(
                    value = latitudeInput,
                    onValueChange = { mapViewModel.updateGoToPointField("latitude", it) },
                    label = { Text(stringResource(R.string.latitude)) },
                    isError = latitudeError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (latitudeError != null) {
                    Text(
                        text = stringResource(latitudeError),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = longitudeInput,
                    onValueChange = { mapViewModel.updateGoToPointField("longitude", it) },
                    label = { Text(stringResource(R.string.longitude)) },
                    isError = longitudeError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (longitudeError != null) {
                    Text(
                        text = stringResource(longitudeError),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    mapViewModel.validateAndGo { latitude, longitude ->
                        onGoToPoint(latitude, longitude)
                    }
                }
            ) {
                Text(stringResource(R.string.go))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    mapViewModel.clearGoToPointInputs()
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
