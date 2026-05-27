package com.noobexon.xposedfakelocation.manager.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.noobexon.xposedfakelocation.R
import com.noobexon.xposedfakelocation.data.model.FavoriteLocation
import com.noobexon.xposedfakelocation.manager.ui.map.MapViewModel
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    mapViewModel: MapViewModel,
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    val favorites by favoritesViewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_favorites_added))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(favorites) { favorite ->
                    FavoriteItem(
                        favorite = favorite,
                        onClick = {
                            mapViewModel.updateClickedLocation(GeoPoint(favorite.latitude, favorite.longitude))
                            navController.navigateUp()
                        },
                        onDelete = {
                            favoritesViewModel.removeFavorite(favorite)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteItem(
    favorite: FavoriteLocation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        ListItem(
            headlineContent = { Text(favorite.name) },
            supportingContent = {
                Text(
                    text = stringResource(
                        R.string.favorite_coordinates,
                        favorite.latitude.toString(),
                        favorite.longitude.toString()
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        )
    }
}
