package com.noobexon.xposedfakelocation.manager.ui.map

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.noobexon.xposedfakelocation.R
import com.noobexon.xposedfakelocation.data.model.FavoriteLocation
import com.noobexon.xposedfakelocation.manager.ui.drawer.DrawerContent
import com.noobexon.xposedfakelocation.manager.ui.map.components.AddToFavoritesDialog
import com.noobexon.xposedfakelocation.manager.ui.map.components.GoToPointDialog
import com.noobexon.xposedfakelocation.manager.ui.map.components.MapViewContainer
import com.noobexon.xposedfakelocation.manager.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current
    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()

    val isPlaying = uiState.isPlaying
    val isFabClickable = uiState.isFabClickable
    val showGoToPointDialog = uiState.goToPointDialogState == DialogState.Visible
    val showAddToFavoritesDialog = uiState.addToFavoritesDialogState == DialogState.Visible

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showOptionsMenu by remember { mutableStateOf(false) }
    val fakeLocationSet = stringResource(R.string.fake_location_set)
    val unsetFakeLocation = stringResource(R.string.unset_fake_location)

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
                navController = navController
            )
        },
        scrimColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
        drawerState = drawerState,
        gesturesEnabled = false,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.menu)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                mapViewModel.triggerCenterMapEvent()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = stringResource(R.string.center)
                            )
                        }
                        IconButton(
                            onClick = {
                                showOptionsMenu = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.options)
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationSearching,
                                        contentDescription = stringResource(R.string.go_to_point)
                                    )
                                },
                                text = { Text(stringResource(R.string.go_to_point)) },
                                onClick = {
                                    showOptionsMenu = false
                                    mapViewModel.showGoToPointDialog()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = stringResource(R.string.add_to_favorites)
                                    )
                                },
                                text = { Text(stringResource(R.string.add_to_favorites)) },
                                onClick = {
                                    showOptionsMenu = false
                                    mapViewModel.showAddToFavoritesDialog()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = stringResource(R.string.favorites)
                                    )
                                },
                                text = { Text(stringResource(R.string.favorites)) },
                                onClick = {
                                    showOptionsMenu = false
                                    navController.navigate(Screen.Favorites.route)
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_location)
                                    )
                                },
                                text = { Text(stringResource(R.string.clear_location)) },
                                onClick = {
                                    showOptionsMenu = false
                                    mapViewModel.updateClickedLocation(null)
                                },
                                enabled = isFabClickable
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (isFabClickable) {
                            val wasPlaying = uiState.isPlaying
                            mapViewModel.togglePlaying()
                            if (!wasPlaying) {
                                Toast.makeText(context, fakeLocationSet, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, unsetFakeLocation, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp),
                    containerColor = if (isFabClickable) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    },
                    contentColor = if (isFabClickable) {
                        contentColorFor(MaterialTheme.colorScheme.primary)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = if (isFabClickable) 6.dp else 0.dp,
                        pressedElevation = if (isFabClickable) 12.dp else 0.dp
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) {
                            stringResource(R.string.stop)
                        } else {
                            stringResource(R.string.play)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MapViewContainer(mapViewModel)
            }
        }

        if (showGoToPointDialog) {
            GoToPointDialog(
                onDismissRequest = { mapViewModel.hideGoToPointDialog() },
                onGoToPoint = { latitude, longitude ->
                    mapViewModel.goToPoint(latitude, longitude)
                    mapViewModel.hideGoToPointDialog()
                },
                mapViewModel = mapViewModel
            )
        }

        if (showAddToFavoritesDialog) {
            val lastClickedLocation = uiState.lastClickedLocation

            LaunchedEffect(lastClickedLocation) {
                mapViewModel.prefillCoordinatesFromMarker(
                    lastClickedLocation?.latitude,
                    lastClickedLocation?.longitude
                )
            }

            AddToFavoritesDialog(
                mapViewModel = mapViewModel,
                onDismissRequest = { mapViewModel.hideAddToFavoritesDialog() },
                onAddFavorite = { name, latitude, longitude ->
                    val favorite = FavoriteLocation(name, latitude, longitude)
                    mapViewModel.addFavoriteLocation(favorite)
                    mapViewModel.hideAddToFavoritesDialog()
                }
            )
        }
    }
}
