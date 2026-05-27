package com.noobexon.xposedfakelocation.manager.ui.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noobexon.xposedfakelocation.R
import com.noobexon.xposedfakelocation.manager.ui.map.MapViewModel

@Composable
fun AddToFavoritesDialog(
    mapViewModel: MapViewModel,
    onDismissRequest: () -> Unit,
    onAddFavorite: (name: String, latitude: Double, longitude: Double) -> Unit
) {
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val addToFavoritesState = uiState.addToFavoritesState

    val favoriteNameInput = addToFavoritesState.name.value
    val favoriteLatitudeInput = addToFavoritesState.latitude.value
    val favoriteLongitudeInput = addToFavoritesState.longitude.value
    val favoriteNameError = addToFavoritesState.name.errorMessageResId
    val favoriteLatitudeError = addToFavoritesState.latitude.errorMessageResId
    val favoriteLongitudeError = addToFavoritesState.longitude.errorMessageResId

    AlertDialog(
        onDismissRequest = {
            mapViewModel.clearAddToFavoritesInputs()
            onDismissRequest()
        },
        title = { Text(stringResource(R.string.add_to_favorites)) },
        text = {
            Column {
                OutlinedTextField(
                    value = favoriteNameInput,
                    onValueChange = { mapViewModel.updateAddToFavoritesField("name", it) },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = favoriteNameError != null
                )
                if (favoriteNameError != null) {
                    Text(
                        text = stringResource(favoriteNameError),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = favoriteLatitudeInput,
                    onValueChange = { mapViewModel.updateAddToFavoritesField("latitude", it) },
                    label = { Text(stringResource(R.string.latitude)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    isError = favoriteLatitudeError != null
                )
                if (favoriteLatitudeError != null) {
                    Text(
                        text = stringResource(favoriteLatitudeError),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = favoriteLongitudeInput,
                    onValueChange = { mapViewModel.updateAddToFavoritesField("longitude", it) },
                    label = { Text(stringResource(R.string.longitude)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    isError = favoriteLongitudeError != null
                )
                if (favoriteLongitudeError != null) {
                    Text(
                        text = stringResource(favoriteLongitudeError),
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
                    mapViewModel.validateAndAddFavorite { name, latitude, longitude ->
                        onAddFavorite(name, latitude, longitude)
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    mapViewModel.clearAddToFavoritesInputs()
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
