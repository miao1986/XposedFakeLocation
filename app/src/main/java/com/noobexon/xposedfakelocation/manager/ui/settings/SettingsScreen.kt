package com.noobexon.xposedfakelocation.manager.ui.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.noobexon.xposedfakelocation.R
import com.noobexon.xposedfakelocation.manager.control.ControlReceiver
import com.noobexon.xposedfakelocation.manager.localization.LanguageOption
import com.noobexon.xposedfakelocation.manager.localization.LocaleController

private object Dimensions {
    val SPACING_EXTRA_SMALL = 4.dp
    val SPACING_SMALL = 8.dp
    val SPACING_MEDIUM = 16.dp
    val SPACING_LARGE = 24.dp
    val CARD_CORNER_RADIUS = 12.dp
    val CARD_ELEVATION = 2.dp
}

private data class SettingsCategory(
    val title: String,
    val settings: List<SettingData>
)

private object SettingDefinitions {
    @Composable
    fun getCategories(viewModel: SettingsViewModel): List<SettingsCategory> {
        val meter = stringResource(R.string.unit_meter)
        val meterPerSecond = stringResource(R.string.unit_meter_per_second)

        return listOf(
            SettingsCategory(
                title = stringResource(R.string.location),
                settings = listOf(
                    DoubleSettingData(
                        title = stringResource(R.string.randomize_nearby_location),
                        description = stringResource(R.string.randomize_nearby_location_description),
                        useValueState = viewModel.useRandomize.collectAsState(),
                        valueState = viewModel.randomizeRadius.collectAsState(),
                        setUseValue = viewModel::setUseRandomize,
                        setValue = viewModel::setRandomizeRadius,
                        label = stringResource(R.string.randomization_radius),
                        unit = meter,
                        minValue = 0f,
                        maxValue = 2000f,
                        step = 0.1f
                    ),
                    DoubleSettingData(
                        title = stringResource(R.string.custom_horizontal_accuracy),
                        description = stringResource(R.string.custom_horizontal_accuracy_description),
                        useValueState = viewModel.useAccuracy.collectAsState(),
                        valueState = viewModel.accuracy.collectAsState(),
                        setUseValue = viewModel::setUseAccuracy,
                        setValue = viewModel::setAccuracy,
                        label = stringResource(R.string.horizontal_accuracy),
                        unit = meter,
                        minValue = 0f,
                        maxValue = 100f,
                        step = 1f
                    ),
                    FloatSettingData(
                        title = stringResource(R.string.custom_vertical_accuracy),
                        description = stringResource(R.string.custom_vertical_accuracy_description),
                        useValueState = viewModel.useVerticalAccuracy.collectAsState(),
                        valueState = viewModel.verticalAccuracy.collectAsState(),
                        setUseValue = viewModel::setUseVerticalAccuracy,
                        setValue = viewModel::setVerticalAccuracy,
                        label = stringResource(R.string.vertical_accuracy),
                        unit = meter,
                        minValue = 0f,
                        maxValue = 100f,
                        step = 1f
                    )
                )
            ),
            SettingsCategory(
                title = stringResource(R.string.altitude),
                settings = listOf(
                    DoubleSettingData(
                        title = stringResource(R.string.custom_altitude),
                        description = stringResource(R.string.custom_altitude_description),
                        useValueState = viewModel.useAltitude.collectAsState(),
                        valueState = viewModel.altitude.collectAsState(),
                        setUseValue = viewModel::setUseAltitude,
                        setValue = viewModel::setAltitude,
                        label = stringResource(R.string.altitude),
                        unit = meter,
                        minValue = 0f,
                        maxValue = 2000f,
                        step = 0.5f
                    ),
                    DoubleSettingData(
                        title = stringResource(R.string.custom_msl),
                        description = stringResource(R.string.custom_msl_description),
                        useValueState = viewModel.useMeanSeaLevel.collectAsState(),
                        valueState = viewModel.meanSeaLevel.collectAsState(),
                        setUseValue = viewModel::setUseMeanSeaLevel,
                        setValue = viewModel::setMeanSeaLevel,
                        label = stringResource(R.string.msl),
                        unit = meter,
                        minValue = -400f,
                        maxValue = 2000f,
                        step = 0.5f
                    ),
                    FloatSettingData(
                        title = stringResource(R.string.custom_msl_accuracy),
                        description = stringResource(R.string.custom_msl_accuracy_description),
                        useValueState = viewModel.useMeanSeaLevelAccuracy.collectAsState(),
                        valueState = viewModel.meanSeaLevelAccuracy.collectAsState(),
                        setUseValue = viewModel::setUseMeanSeaLevelAccuracy,
                        setValue = viewModel::setMeanSeaLevelAccuracy,
                        label = stringResource(R.string.msl_accuracy),
                        unit = meter,
                        minValue = 0f,
                        maxValue = 100f,
                        step = 1f
                    )
                )
            ),
            SettingsCategory(
                title = stringResource(R.string.movement),
                settings = listOf(
                    FloatSettingData(
                        title = stringResource(R.string.custom_speed),
                        description = stringResource(R.string.custom_speed_description),
                        useValueState = viewModel.useSpeed.collectAsState(),
                        valueState = viewModel.speed.collectAsState(),
                        setUseValue = viewModel::setUseSpeed,
                        setValue = viewModel::setSpeed,
                        label = stringResource(R.string.speed),
                        unit = meterPerSecond,
                        minValue = 0f,
                        maxValue = 30f,
                        step = 0.1f
                    ),
                    FloatSettingData(
                        title = stringResource(R.string.custom_speed_accuracy),
                        description = stringResource(R.string.custom_speed_accuracy_description),
                        useValueState = viewModel.useSpeedAccuracy.collectAsState(),
                        valueState = viewModel.speedAccuracy.collectAsState(),
                        setUseValue = viewModel::setUseSpeedAccuracy,
                        setValue = viewModel::setSpeedAccuracy,
                        label = stringResource(R.string.speed_accuracy),
                        unit = meterPerSecond,
                        minValue = 0f,
                        maxValue = 100f,
                        step = 1f
                    )
                )
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showRebootDialog by remember { mutableStateOf(false) }
    val selectedLanguageTag by settingsViewModel.languageTag.collectAsState()
    val selectedLanguage = LanguageOption.fromTag(selectedLanguageTag)
    val settingCategories = SettingDefinitions.getCategories(settingsViewModel)

    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { showRebootDialog = false },
            title = { Text(stringResource(R.string.reboot_required)) },
            text = { Text(stringResource(R.string.reboot_required_description)) },
            confirmButton = {
                TextButton(onClick = { showRebootDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimensions.SPACING_MEDIUM)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))

                CategoryHeader(stringResource(R.string.language))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.SPACING_SMALL),
                    shape = RoundedCornerShape(Dimensions.CARD_CORNER_RADIUS),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION)
                ) {
                    Column(modifier = Modifier.padding(Dimensions.SPACING_SMALL)) {
                        LanguageSettingItem(
                            selectedLanguage = selectedLanguage,
                            onLanguageSelected = { option ->
                                if (option.tag != selectedLanguageTag) {
                                    settingsViewModel.setLanguageTag(option.tag)
                                    LocaleController.persistLanguageTag(context, option.tag)
                                    context.findActivity()?.recreate()
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))

                CategoryHeader(stringResource(R.string.notifications))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.SPACING_SMALL),
                    shape = RoundedCornerShape(Dimensions.CARD_CORNER_RADIUS),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION)
                ) {
                    Column(modifier = Modifier.padding(Dimensions.SPACING_SMALL)) {
                        BooleanSettingItem(
                            title = stringResource(R.string.hide_fake_location_toast),
                            description = stringResource(R.string.hide_fake_location_toast_description),
                            checked = settingsViewModel.hideFakeLocationToast.collectAsState().value,
                            onCheckedChange = settingsViewModel::setHideFakeLocationToast
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))

                CategoryHeader(stringResource(R.string.target_apps))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.SPACING_SMALL),
                    shape = RoundedCornerShape(Dimensions.CARD_CORNER_RADIUS),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION)
                ) {
                    Column(modifier = Modifier.padding(Dimensions.SPACING_SMALL)) {
                        BooleanSettingItem(
                            title = stringResource(R.string.use_builtin_target_app_selection),
                            description = stringResource(R.string.use_builtin_target_app_selection_description),
                            checked = settingsViewModel.useInAppTargetApps.collectAsState().value,
                            onCheckedChange = { newValue ->
                                settingsViewModel.setUseInAppTargetApps(newValue)
                                if (newValue) {
                                    showRebootDialog = true
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))

                CategoryHeader(stringResource(R.string.external_control))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.SPACING_SMALL),
                    shape = RoundedCornerShape(Dimensions.CARD_CORNER_RADIUS),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION)
                ) {
                    Column(modifier = Modifier.padding(Dimensions.SPACING_SMALL)) {
                        BooleanSettingItem(
                            title = stringResource(R.string.allow_external_broadcast_control),
                            description = stringResource(R.string.allow_external_broadcast_control_description),
                            checked = settingsViewModel.enableBroadcastControl.collectAsState().value,
                            onCheckedChange = { newValue ->
                                settingsViewModel.setEnableBroadcastControl(newValue)
                                setControlReceiverEnabled(context, newValue)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))

                settingCategories.forEach { category ->
                    CategoryHeader(category.title)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimensions.SPACING_SMALL),
                        shape = RoundedCornerShape(Dimensions.CARD_CORNER_RADIUS),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION)
                    ) {
                        Column(modifier = Modifier.padding(Dimensions.SPACING_SMALL)) {
                            category.settings.forEachIndexed { index, setting ->
                                when (setting) {
                                    is DoubleSettingData -> DoubleSettingComposable(setting)
                                    is FloatSettingData -> FloatSettingComposable(setting)
                                }
                                if (index != category.settings.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = Dimensions.SPACING_SMALL),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))
                }

                Spacer(modifier = Modifier.height(Dimensions.SPACING_LARGE))
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun setControlReceiverEnabled(context: Context, enabled: Boolean) {
    val component = ComponentName(context, ControlReceiver::class.java)
    val newState = if (enabled) {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    } else {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }
    context.packageManager.setComponentEnabledSetting(
        component,
        newState,
        PackageManager.DONT_KILL_APP
    )
}

@Composable
fun CategoryHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimensions.SPACING_SMALL)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier
                .weight(2f)
                .padding(start = Dimensions.SPACING_MEDIUM),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingItem(
    selectedLanguage: LanguageOption,
    onLanguageSelected: (LanguageOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.SPACING_SMALL)
    ) {
        OutlinedTextField(
            value = stringResource(selectedLanguage.labelRes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.language)) },
            supportingText = { Text(stringResource(R.string.language_description)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LanguageOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        expanded = false
                        onLanguageSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
fun BooleanSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }
    val infoContentDescription = stringResource(R.string.more_information_about, title)
    val switchContentDescription = if (checked) {
        stringResource(R.string.disable_setting, title)
    } else {
        stringResource(R.string.enable_setting, title)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.SPACING_SMALL)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = { showTooltip = !showTooltip },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = infoContentDescription,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (showTooltip) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimensions.SPACING_EXTRA_SMALL)
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.semantics {
                    contentDescription = switchContentDescription
                }
            )
        }
    }
}

@Composable
fun DoubleSettingItem(
    title: String,
    description: String,
    useValue: Boolean,
    onUseValueChange: (Boolean) -> Unit,
    value: Double,
    onValueChange: (Double) -> Unit,
    label: String,
    unit: String,
    minValue: Float,
    maxValue: Float,
    step: Float
) {
    SettingItem(
        title = title,
        description = description,
        useValue = useValue,
        onUseValueChange = onUseValueChange,
        value = value,
        onValueChange = onValueChange,
        label = label,
        unit = unit,
        minValue = minValue,
        maxValue = maxValue,
        step = step,
        valueFormatter = { "%.2f".format(it) },
        parseValue = { it.toDouble() }
    )
}

@Composable
fun FloatSettingItem(
    title: String,
    description: String,
    useValue: Boolean,
    onUseValueChange: (Boolean) -> Unit,
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    unit: String,
    minValue: Float,
    maxValue: Float,
    step: Float
) {
    SettingItem(
        title = title,
        description = description,
        useValue = useValue,
        onUseValueChange = onUseValueChange,
        value = value,
        onValueChange = onValueChange,
        label = label,
        unit = unit,
        minValue = minValue,
        maxValue = maxValue,
        step = step,
        valueFormatter = { "%.2f".format(it) },
        parseValue = { it }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Number> SettingItem(
    title: String,
    description: String,
    useValue: Boolean,
    onUseValueChange: (Boolean) -> Unit,
    value: T,
    onValueChange: (T) -> Unit,
    label: String,
    unit: String,
    minValue: Float,
    maxValue: Float,
    step: Float,
    valueFormatter: (T) -> String,
    parseValue: (Float) -> T
) {
    var showTooltip by remember { mutableStateOf(false) }
    val infoContentDescription = stringResource(R.string.more_information_about, title)
    val switchContentDescription = if (useValue) {
        stringResource(R.string.disable_setting, title)
    } else {
        stringResource(R.string.enable_setting, title)
    }
    val sliderContentDescription = stringResource(R.string.adjust_setting_value, title)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.SPACING_SMALL)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = { showTooltip = !showTooltip },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = infoContentDescription,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (showTooltip) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimensions.SPACING_EXTRA_SMALL)
                    )
                }
            }

            Switch(
                checked = useValue,
                onCheckedChange = onUseValueChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.semantics {
                    contentDescription = switchContentDescription
                }
            )
        }

        if (useValue) {
            Spacer(modifier = Modifier.height(Dimensions.SPACING_MEDIUM))

            var sliderValue by remember { mutableFloatStateOf(value.toFloat()) }
            var showExactValue by remember { mutableStateOf(false) }

            LaunchedEffect(value) {
                if (sliderValue != value.toFloat()) {
                    sliderValue = value.toFloat()
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.SPACING_SMALL),
                modifier = Modifier.fillMaxWidth()
            ) {
                val displayText = "$label: ${valueFormatter(parseValue(sliderValue))} $unit"
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showExactValue = !showExactValue }
                )

                OutlinedIconButton(
                    onClick = {
                        val newValue = (sliderValue - step).coerceAtLeast(minValue)
                        sliderValue = newValue
                        onValueChange(parseValue(newValue))
                    },
                    enabled = sliderValue > minValue,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "−",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                OutlinedIconButton(
                    onClick = {
                        val newValue = (sliderValue + step).coerceAtMost(maxValue)
                        sliderValue = newValue
                        onValueChange(parseValue(newValue))
                    },
                    enabled = sliderValue < maxValue,
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "+",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.SPACING_SMALL)
            ) {
                Text(
                    text = "${minValue.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${maxValue.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                },
                onValueChangeFinished = {
                    onValueChange(parseValue(sliderValue))
                },
                valueRange = minValue..maxValue,
                steps = ((maxValue - minValue) / step).toInt() - 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = sliderContentDescription
                    }
            )
        }
    }
}

sealed class SettingData {
    abstract val title: String
    abstract val description: String
    abstract val useValueState: State<Boolean>
    abstract val setUseValue: (Boolean) -> Unit
    abstract val label: String
    abstract val unit: String
    abstract val minValue: Float
    abstract val maxValue: Float
    abstract val step: Float
}

data class DoubleSettingData(
    override val title: String,
    override val description: String,
    override val useValueState: State<Boolean>,
    val valueState: State<Double>,
    override val setUseValue: (Boolean) -> Unit,
    val setValue: (Double) -> Unit,
    override val label: String,
    override val unit: String,
    override val minValue: Float,
    override val maxValue: Float,
    override val step: Float
) : SettingData()

data class FloatSettingData(
    override val title: String,
    override val description: String,
    override val useValueState: State<Boolean>,
    val valueState: State<Float>,
    override val setUseValue: (Boolean) -> Unit,
    val setValue: (Float) -> Unit,
    override val label: String,
    override val unit: String,
    override val minValue: Float,
    override val maxValue: Float,
    override val step: Float
) : SettingData()

@Composable
fun DoubleSettingComposable(
    setting: DoubleSettingData
) {
    DoubleSettingItem(
        title = setting.title,
        description = setting.description,
        useValue = setting.useValueState.value,
        onUseValueChange = setting.setUseValue,
        value = setting.valueState.value,
        onValueChange = setting.setValue,
        label = setting.label,
        unit = setting.unit,
        minValue = setting.minValue,
        maxValue = setting.maxValue,
        step = setting.step
    )
}

@Composable
fun FloatSettingComposable(
    setting: FloatSettingData
) {
    FloatSettingItem(
        title = setting.title,
        description = setting.description,
        useValue = setting.useValueState.value,
        onUseValueChange = setting.setUseValue,
        value = setting.valueState.value,
        onValueChange = setting.setValue,
        label = setting.label,
        unit = setting.unit,
        minValue = setting.minValue,
        maxValue = setting.maxValue,
        step = setting.step
    )
}
