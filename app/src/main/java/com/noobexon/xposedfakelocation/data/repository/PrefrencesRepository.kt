// PreferencesRepository.kt
package com.noobexon.xposedfakelocation.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.noobexon.xposedfakelocation.data.*
import com.noobexon.xposedfakelocation.data.model.FavoriteLocation
import com.noobexon.xposedfakelocation.data.model.LastClickedLocation
import com.noobexon.xposedfakelocation.manager.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SHARED_PREFS_FILE)

class PreferencesRepository(private val context: Context) {
    private val tag = "PreferencesRepository"

    private fun remotePrefs(): SharedPreferences? = App.service?.getRemotePreferences(REMOTE_PREFS_GROUP)

    // A long-lived scope so the bind callback can fire-and-forget the sync.
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Pushes the manager's entire current settings snapshot (from DataStore) into the
     * LSPosed remote preferences, so a freshly-bound XposedService / newly-enabled module
     * sees a complete, consistent state. Safe to call repeatedly. No-op if not bound.
     */
    fun syncAllToRemote() {
        val remote = App.service?.getRemotePreferences(REMOTE_PREFS_GROUP)

        if (remote == null) {
            Log.w(tag, "syncAllToRemote: XposedService not bound; skipping")
            return
        }

        syncScope.launch {
            try {
                val prefs = context.dataStore.data.first()   // single DataStore snapshot

                remote.edit().apply {
                    // --- Booleans ---
                    putBoolean(KEY_IS_PLAYING, prefs[PreferenceKeys.IS_PLAYING] ?: false)
                    putBoolean(KEY_USE_ACCURACY, prefs[PreferenceKeys.USE_ACCURACY] ?: DEFAULT_USE_ACCURACY)
                    putBoolean(KEY_USE_ALTITUDE, prefs[PreferenceKeys.USE_ALTITUDE] ?: DEFAULT_USE_ALTITUDE)
                    putBoolean(KEY_USE_RANDOMIZE, prefs[PreferenceKeys.USE_RANDOMIZE] ?: DEFAULT_USE_RANDOMIZE)
                    putBoolean(KEY_USE_VERTICAL_ACCURACY, prefs[PreferenceKeys.USE_VERTICAL_ACCURACY] ?: DEFAULT_USE_VERTICAL_ACCURACY)
                    putBoolean(KEY_USE_MEAN_SEA_LEVEL, prefs[PreferenceKeys.USE_MEAN_SEA_LEVEL] ?: DEFAULT_USE_MEAN_SEA_LEVEL)
                    putBoolean(KEY_USE_MEAN_SEA_LEVEL_ACCURACY, prefs[PreferenceKeys.USE_MEAN_SEA_LEVEL_ACCURACY] ?: DEFAULT_USE_MEAN_SEA_LEVEL_ACCURACY)
                    putBoolean(KEY_USE_SPEED, prefs[PreferenceKeys.USE_SPEED] ?: DEFAULT_USE_SPEED)
                    putBoolean(KEY_USE_SPEED_ACCURACY, prefs[PreferenceKeys.USE_SPEED_ACCURACY] ?: DEFAULT_USE_SPEED_ACCURACY)
                    putBoolean(KEY_HIDE_FAKE_LOCATION_TOAST, prefs[PreferenceKeys.HIDE_FAKE_LOCATION_TOAST] ?: DEFAULT_HIDE_FAKE_LOCATION_TOAST)
                    putBoolean(KEY_USE_INAPP_TARGET_APPS, prefs[PreferenceKeys.USE_INAPP_TARGET_APPS] ?: DEFAULT_USE_INAPP_TARGET_APPS)
                    putBoolean(KEY_ENABLE_BROADCAST_CONTROL, prefs[PreferenceKeys.ENABLE_BROADCAST_CONTROL] ?: DEFAULT_ENABLE_BROADCAST_CONTROL)
                    // --- Doubles (stored as raw long bits, matching the hook's decode) ---
                    putLong(KEY_ACCURACY, java.lang.Double.doubleToRawLongBits(prefs[PreferenceKeys.ACCURACY] ?: DEFAULT_ACCURACY))
                    putLong(KEY_ALTITUDE, java.lang.Double.doubleToRawLongBits(prefs[PreferenceKeys.ALTITUDE] ?: DEFAULT_ALTITUDE))
                    putLong(KEY_RANDOMIZE_RADIUS, java.lang.Double.doubleToRawLongBits(prefs[PreferenceKeys.RANDOMIZE_RADIUS] ?: DEFAULT_RANDOMIZE_RADIUS))
                    putLong(KEY_MEAN_SEA_LEVEL, java.lang.Double.doubleToRawLongBits(prefs[PreferenceKeys.MEAN_SEA_LEVEL] ?: DEFAULT_MEAN_SEA_LEVEL))
                    // --- Floats ---
                    putFloat(KEY_VERTICAL_ACCURACY, prefs[PreferenceKeys.VERTICAL_ACCURACY] ?: DEFAULT_VERTICAL_ACCURACY)
                    putFloat(KEY_MEAN_SEA_LEVEL_ACCURACY, prefs[PreferenceKeys.MEAN_SEA_LEVEL_ACCURACY] ?: DEFAULT_MEAN_SEA_LEVEL_ACCURACY)
                    putFloat(KEY_SPEED, prefs[PreferenceKeys.SPEED] ?: DEFAULT_SPEED)
                    putFloat(KEY_SPEED_ACCURACY, prefs[PreferenceKeys.SPEED_ACCURACY] ?: DEFAULT_SPEED_ACCURACY)
                    // --- Strings ---
                    putString(KEY_LANGUAGE_TAG, prefs[PreferenceKeys.LANGUAGE_TAG] ?: DEFAULT_LANGUAGE_TAG)
                    // --- Nullable JSON strings: write if present, otherwise clear ---
                    prefs[PreferenceKeys.LAST_CLICKED_LOCATION]
                        ?.let { putString(KEY_LAST_CLICKED_LOCATION, it) }
                        ?: remove(KEY_LAST_CLICKED_LOCATION)
                    prefs[PreferenceKeys.TARGET_APPS]
                        ?.let { putString(KEY_TARGET_APPS, it) }
                        ?: remove(KEY_TARGET_APPS)
                }.apply()

                Log.d(tag, "syncAllToRemote: pushed all settings to remote prefs")
            } catch (e: Exception) {
                Log.e(tag, "syncAllToRemote failed: ${e.message}")
            }
        }
    }

    private val gson = Gson()

    // DataStore preference keys
    private object PreferenceKeys {
        val IS_PLAYING = booleanPreferencesKey(KEY_IS_PLAYING)
        val LAST_CLICKED_LOCATION = stringPreferencesKey(KEY_LAST_CLICKED_LOCATION)
        val USE_ACCURACY = booleanPreferencesKey(KEY_USE_ACCURACY)
        val ACCURACY = doublePreferencesKey(KEY_ACCURACY)
        val USE_ALTITUDE = booleanPreferencesKey(KEY_USE_ALTITUDE)
        val ALTITUDE = doublePreferencesKey(KEY_ALTITUDE)
        val USE_RANDOMIZE = booleanPreferencesKey(KEY_USE_RANDOMIZE)
        val RANDOMIZE_RADIUS = doublePreferencesKey(KEY_RANDOMIZE_RADIUS)
        val USE_VERTICAL_ACCURACY = booleanPreferencesKey(KEY_USE_VERTICAL_ACCURACY)
        val VERTICAL_ACCURACY = floatPreferencesKey(KEY_VERTICAL_ACCURACY)
        val USE_MEAN_SEA_LEVEL = booleanPreferencesKey(KEY_USE_MEAN_SEA_LEVEL)
        val MEAN_SEA_LEVEL = doublePreferencesKey(KEY_MEAN_SEA_LEVEL)
        val USE_MEAN_SEA_LEVEL_ACCURACY = booleanPreferencesKey(KEY_USE_MEAN_SEA_LEVEL_ACCURACY)
        val MEAN_SEA_LEVEL_ACCURACY = floatPreferencesKey(KEY_MEAN_SEA_LEVEL_ACCURACY)
        val USE_SPEED = booleanPreferencesKey(KEY_USE_SPEED)
        val SPEED = floatPreferencesKey(KEY_SPEED)
        val USE_SPEED_ACCURACY = booleanPreferencesKey(KEY_USE_SPEED_ACCURACY)
        val SPEED_ACCURACY = floatPreferencesKey(KEY_SPEED_ACCURACY)
        val FAVORITES = stringPreferencesKey(KEY_FAVORITES)
        val TARGET_APPS = stringPreferencesKey(KEY_TARGET_APPS)
        val HIDE_FAKE_LOCATION_TOAST = booleanPreferencesKey(KEY_HIDE_FAKE_LOCATION_TOAST)
        val USE_INAPP_TARGET_APPS = booleanPreferencesKey(KEY_USE_INAPP_TARGET_APPS)
        val ENABLE_BROADCAST_CONTROL = booleanPreferencesKey(KEY_ENABLE_BROADCAST_CONTROL)
        val LANGUAGE_TAG = stringPreferencesKey(KEY_LANGUAGE_TAG)
    }

    // Generic helper for DataStore flows with error handling
    private fun <T> getPreferenceFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(tag, "Error reading preferences: ${exception.message}")
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    // Helper to write both to DataStore and legacy SharedPreferences
    private suspend inline fun <reified T> savePreference(
        key: Preferences.Key<T>,
        value: T,
        sharedPrefsKey: String,
        sharedPrefsValue: Any
    ) {
        try {
            // Save to DataStore
            context.dataStore.edit { preferences ->
                preferences[key] = value
            }
            
            // Save to remote SharedPreferences for Xposed Module
            remotePrefs()?.edit()?.apply() {
                when (value) {
                    is Boolean -> putBoolean(sharedPrefsKey, value)
                    is String -> putString(sharedPrefsKey, value)
                    is Float -> putFloat(sharedPrefsKey, value)
                    is Double -> putLong(sharedPrefsKey, java.lang.Double.doubleToRawLongBits(value))
                    is Long -> putLong(sharedPrefsKey, value)
                    is Int -> putInt(sharedPrefsKey, value)
                }
            }?.apply()

            Log.d(tag, "Saved $sharedPrefsKey: $value")
        } catch (e: Exception) {
            Log.e(tag, "Error saving preference $sharedPrefsKey: ${e.message}")
        }
    }

    // Is Playing
    fun getIsPlayingFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.IS_PLAYING, false)
    }
    
    suspend fun saveIsPlaying(isPlaying: Boolean) {
        savePreference(PreferenceKeys.IS_PLAYING, isPlaying, KEY_IS_PLAYING, isPlaying)
    }

    // Is Playing
    fun getIsPlaying(): Boolean = remotePrefs()?.getBoolean(KEY_IS_PLAYING, false) ?: false

    // Last Clicked Location
    fun getLastClickedLocationFlow(): Flow<LastClickedLocation?> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(tag, "Error reading preferences: ${exception.message}")
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[PreferenceKeys.LAST_CLICKED_LOCATION]
                if (json != null) {
                    try {
                        gson.fromJson(json, LastClickedLocation::class.java)
                    } catch (e: JsonSyntaxException) {
                        Log.e(tag, "Error parsing LastClickedLocation: ${e.message}")
                        null
                    }
                } else {
                    null
                }
            }
    }
    
    suspend fun saveLastClickedLocation(latitude: Double, longitude: Double) {
        try {
            val location = LastClickedLocation(latitude, longitude)
            val json = gson.toJson(location)
            savePreference(PreferenceKeys.LAST_CLICKED_LOCATION, json, KEY_LAST_CLICKED_LOCATION, json)
        } catch (e: Exception) {
            Log.e(tag, "Error saving LastClickedLocation: ${e.message}")
        }
    }

    // Last Clicked Location
    fun getLastClickedLocation(): LastClickedLocation? {
        val json = remotePrefs()?.getString(KEY_LAST_CLICKED_LOCATION, null) ?: return null
        return try {
            gson.fromJson(json, LastClickedLocation::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e(tag, "Error parsing LastClickedLocation: ${e.message}")
            null
        }
    }

    suspend fun clearLastClickedLocation() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(PreferenceKeys.LAST_CLICKED_LOCATION)
            }

            remotePrefs()?.edit()?.apply { remove(KEY_LAST_CLICKED_LOCATION) }?.apply()

            saveIsPlaying(false)
            Log.d(tag, "Cleared 'LastClickedLocation' from preferences and set 'IsPlaying' to false")
        } catch (e: Exception) {
            Log.e(tag, "Error clearing LastClickedLocation: ${e.message}")
        }
    }

    // Use Accuracy
    fun getUseAccuracyFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_ACCURACY, DEFAULT_USE_ACCURACY)
    }
    
    suspend fun saveUseAccuracy(useAccuracy: Boolean) {
        savePreference(PreferenceKeys.USE_ACCURACY, useAccuracy, KEY_USE_ACCURACY, useAccuracy)
    }
    
    // For backward compatibility
    fun getUseAccuracy(): Boolean = remotePrefs()?.getBoolean(KEY_USE_ACCURACY, DEFAULT_USE_ACCURACY) ?: DEFAULT_USE_ACCURACY

    // Accuracy
    fun getAccuracyFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.ACCURACY, DEFAULT_ACCURACY)
    }
    
    suspend fun saveAccuracy(accuracy: Double) {
        savePreference(PreferenceKeys.ACCURACY, accuracy, KEY_ACCURACY, accuracy)
    }

    // Accuracy
    fun getAccuracy(): Double = getRemoteDouble(KEY_ACCURACY, DEFAULT_ACCURACY)

    // Use Altitude
    fun getUseAltitudeFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_ALTITUDE, DEFAULT_USE_ALTITUDE)
    }
    
    suspend fun saveUseAltitude(useAltitude: Boolean) {
        savePreference(PreferenceKeys.USE_ALTITUDE, useAltitude, KEY_USE_ALTITUDE, useAltitude)
    }

    // Use Altitude
    fun getUseAltitude(): Boolean = remotePrefs()?.getBoolean(KEY_USE_ALTITUDE, DEFAULT_USE_ALTITUDE) ?: DEFAULT_USE_ALTITUDE

    // Altitude
    fun getAltitudeFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.ALTITUDE, DEFAULT_ALTITUDE)
    }
    
    suspend fun saveAltitude(altitude: Double) {
        savePreference(PreferenceKeys.ALTITUDE, altitude, KEY_ALTITUDE, altitude)
    }
    
    // Altitude
    fun getAltitude(): Double = getRemoteDouble(KEY_ALTITUDE, DEFAULT_ALTITUDE)

    // Use Randomize
    fun getUseRandomizeFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_RANDOMIZE, DEFAULT_USE_RANDOMIZE)
    }
    
    suspend fun saveUseRandomize(randomize: Boolean) {
        savePreference(PreferenceKeys.USE_RANDOMIZE, randomize, KEY_USE_RANDOMIZE, randomize)
    }

    // Use Randomize
    fun getUseRandomize(): Boolean = remotePrefs()?.getBoolean(KEY_USE_RANDOMIZE, DEFAULT_USE_RANDOMIZE) ?: DEFAULT_USE_RANDOMIZE

    // Randomize Radius
    fun getRandomizeRadiusFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.RANDOMIZE_RADIUS, DEFAULT_RANDOMIZE_RADIUS)
    }
    
    suspend fun saveRandomizeRadius(radius: Double) {
        savePreference(PreferenceKeys.RANDOMIZE_RADIUS, radius, KEY_RANDOMIZE_RADIUS, radius)
    }

    // Randomize Radius
    fun getRandomizeRadius(): Double = getRemoteDouble(KEY_RANDOMIZE_RADIUS, DEFAULT_RANDOMIZE_RADIUS)

    // Favorites
    fun getFavoritesFlow(): Flow<List<FavoriteLocation>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(tag, "Error reading preferences: ${exception.message}")
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[PreferenceKeys.FAVORITES]
                if (json != null) {
                    try {
                        val type = object : TypeToken<List<FavoriteLocation>>() {}.type
                        gson.fromJson(json, type)
                    } catch (e: JsonSyntaxException) {
                        Log.e(tag, "Error parsing Favorites: ${e.message}")
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            }
    }
    
    suspend fun addFavorite(favorite: FavoriteLocation) {
        try {
            val favorites = getFavoritesFlow().firstOrNull() ?: emptyList()
            val updatedFavorites = favorites.toMutableList().apply { add(favorite) }
            saveFavorites(updatedFavorites)
            Log.d(tag, "Added Favorite: $favorite")
        } catch (e: Exception) {
            Log.e(tag, "Error adding favorite: ${e.message}")
        }
    }
    
    private suspend fun saveFavorites(favorites: List<FavoriteLocation>) {
        try {
            val json = gson.toJson(favorites)
            savePreference(PreferenceKeys.FAVORITES, json, KEY_FAVORITES, json)
        } catch (e: Exception) {
            Log.e(tag, "Error saving favorites: ${e.message}")
        }
    }
    
    suspend fun removeFavorite(favorite: FavoriteLocation) {
        try {
            val favorites = getFavoritesFlow().firstOrNull() ?: emptyList()
            val updatedFavorites = favorites.toMutableList().apply { remove(favorite) }
            saveFavorites(updatedFavorites)
            Log.d(tag, "Removed Favorite: $favorite from preferences")
        } catch (e: Exception) {
            Log.e(tag, "Error removing favorite: ${e.message}")
        }
    }

    // Favorites
    fun getFavorites(): List<FavoriteLocation> {
        val json = remotePrefs()?.getString(KEY_FAVORITES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<FavoriteLocation>>() {}.type
            gson.fromJson(json, type)
        } catch (e: JsonSyntaxException) {
            Log.e(tag, "Error parsing Favorites: ${e.message}")
            emptyList()
        }
    }

    fun getTargetAppsFlow(): Flow<Set<String>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(tag, "Error reading target apps: ${exception.message}")
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                parseTargetApps(preferences[PreferenceKeys.TARGET_APPS])
            }
    }

    suspend fun saveTargetApps(packageNames: Set<String>) {
        val normalized = packageNames
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        val json = gson.toJson(normalized)
        savePreference(PreferenceKeys.TARGET_APPS, json, KEY_TARGET_APPS, json)
    }

    private fun parseTargetApps(json: String?): Set<String> {
        if (json.isNullOrBlank()) return emptySet()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type).toSet()
        } catch (e: JsonSyntaxException) {
            Log.e(tag, "Error parsing target apps: ${e.message}")
            emptySet()
        }
    }

    // Vertical Accuracy
    fun getUseVerticalAccuracyFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_VERTICAL_ACCURACY, DEFAULT_USE_VERTICAL_ACCURACY)
    }
    
    suspend fun saveUseVerticalAccuracy(useVerticalAccuracy: Boolean) {
        savePreference(PreferenceKeys.USE_VERTICAL_ACCURACY, useVerticalAccuracy, KEY_USE_VERTICAL_ACCURACY, useVerticalAccuracy)
    }

    // Use Vertical Accuracy
    fun getUseVerticalAccuracy(): Boolean = remotePrefs()?.getBoolean(KEY_USE_VERTICAL_ACCURACY, DEFAULT_USE_VERTICAL_ACCURACY) ?: DEFAULT_USE_VERTICAL_ACCURACY


    // Vertical Accuracy Value
    fun getVerticalAccuracyFlow(): Flow<Float> {
        return getPreferenceFlow(PreferenceKeys.VERTICAL_ACCURACY, DEFAULT_VERTICAL_ACCURACY)
    }
    
    suspend fun saveVerticalAccuracy(verticalAccuracy: Float) {
        savePreference(PreferenceKeys.VERTICAL_ACCURACY, verticalAccuracy, KEY_VERTICAL_ACCURACY, verticalAccuracy)
    }

    // Vertical Accuracy
    fun getVerticalAccuracy(): Float = remotePrefs()?.getFloat(KEY_VERTICAL_ACCURACY, DEFAULT_VERTICAL_ACCURACY) ?: DEFAULT_VERTICAL_ACCURACY


    // Use Mean Sea Level
    fun getUseMeanSeaLevelFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_MEAN_SEA_LEVEL, DEFAULT_USE_MEAN_SEA_LEVEL)
    }
    
    suspend fun saveUseMeanSeaLevel(useMeanSeaLevel: Boolean) {
        savePreference(PreferenceKeys.USE_MEAN_SEA_LEVEL, useMeanSeaLevel, KEY_USE_MEAN_SEA_LEVEL, useMeanSeaLevel)
    }

    // Use Mean Sea Level
    fun getUseMeanSeaLevel(): Boolean = remotePrefs()?.getBoolean(KEY_USE_MEAN_SEA_LEVEL, DEFAULT_USE_MEAN_SEA_LEVEL) ?: DEFAULT_USE_MEAN_SEA_LEVEL


    // Mean Sea Level
    fun getMeanSeaLevelFlow(): Flow<Double> {
        return getPreferenceFlow(PreferenceKeys.MEAN_SEA_LEVEL, DEFAULT_MEAN_SEA_LEVEL)
    }
    
    suspend fun saveMeanSeaLevel(meanSeaLevel: Double) {
        savePreference(PreferenceKeys.MEAN_SEA_LEVEL, meanSeaLevel, KEY_MEAN_SEA_LEVEL, meanSeaLevel)
    }
    
    // Mean Sea Level
    fun getMeanSeaLevel(): Double = getRemoteDouble(KEY_MEAN_SEA_LEVEL, DEFAULT_MEAN_SEA_LEVEL)

    // Use Mean Sea Level Accuracy
    fun getUseMeanSeaLevelAccuracyFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_MEAN_SEA_LEVEL_ACCURACY, DEFAULT_USE_MEAN_SEA_LEVEL_ACCURACY)
    }
    
    suspend fun saveUseMeanSeaLevelAccuracy(useMeanSeaLevelAccuracy: Boolean) {
        savePreference(PreferenceKeys.USE_MEAN_SEA_LEVEL_ACCURACY, useMeanSeaLevelAccuracy, KEY_USE_MEAN_SEA_LEVEL_ACCURACY, useMeanSeaLevelAccuracy)
    }

    // Use Mean Sea Level Accuracy
    fun getUseMeanSeaLevelAccuracy(): Boolean = remotePrefs()?.getBoolean(KEY_USE_MEAN_SEA_LEVEL_ACCURACY, DEFAULT_USE_MEAN_SEA_LEVEL_ACCURACY) ?: DEFAULT_USE_MEAN_SEA_LEVEL_ACCURACY


    // Mean Sea Level Accuracy
    fun getMeanSeaLevelAccuracyFlow(): Flow<Float> {
        return getPreferenceFlow(PreferenceKeys.MEAN_SEA_LEVEL_ACCURACY, DEFAULT_MEAN_SEA_LEVEL_ACCURACY)
    }
    
    suspend fun saveMeanSeaLevelAccuracy(meanSeaLevelAccuracy: Float) {
        savePreference(PreferenceKeys.MEAN_SEA_LEVEL_ACCURACY, meanSeaLevelAccuracy, KEY_MEAN_SEA_LEVEL_ACCURACY, meanSeaLevelAccuracy)
    }

    // Mean Sea Level Accuracy
    fun getMeanSeaLevelAccuracy(): Float = remotePrefs()?.getFloat(KEY_MEAN_SEA_LEVEL_ACCURACY, DEFAULT_MEAN_SEA_LEVEL_ACCURACY) ?: DEFAULT_MEAN_SEA_LEVEL_ACCURACY

    // Use Speed
    fun getUseSpeedFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_SPEED, DEFAULT_USE_SPEED)
    }
    
    suspend fun saveUseSpeed(useSpeed: Boolean) {
        savePreference(PreferenceKeys.USE_SPEED, useSpeed, KEY_USE_SPEED, useSpeed)
    }

    // Use Speed
    fun getUseSpeed(): Boolean = remotePrefs()?.getBoolean(KEY_USE_SPEED, DEFAULT_USE_SPEED) ?: DEFAULT_USE_SPEED


    // Speed
    fun getSpeedFlow(): Flow<Float> {
        return getPreferenceFlow(PreferenceKeys.SPEED, DEFAULT_SPEED)
    }
    
    suspend fun saveSpeed(speed: Float) {
        savePreference(PreferenceKeys.SPEED, speed, KEY_SPEED, speed)
    }

    // Speed
    fun getSpeed(): Float = remotePrefs()?.getFloat(KEY_SPEED, DEFAULT_SPEED) ?: DEFAULT_SPEED

    // Use Speed Accuracy
    fun getUseSpeedAccuracyFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_SPEED_ACCURACY, DEFAULT_USE_SPEED_ACCURACY)
    }
    
    suspend fun saveUseSpeedAccuracy(useSpeedAccuracy: Boolean) {
        savePreference(PreferenceKeys.USE_SPEED_ACCURACY, useSpeedAccuracy, KEY_USE_SPEED_ACCURACY, useSpeedAccuracy)
    }

    // Use Speed Accuracy
    fun getUseSpeedAccuracy(): Boolean = remotePrefs()?.getBoolean(KEY_USE_SPEED_ACCURACY, DEFAULT_USE_SPEED_ACCURACY) ?: DEFAULT_USE_SPEED_ACCURACY


    // Speed Accuracy
    fun getSpeedAccuracyFlow(): Flow<Float> {
        return getPreferenceFlow(PreferenceKeys.SPEED_ACCURACY, DEFAULT_SPEED_ACCURACY)
    }
    
    suspend fun saveSpeedAccuracy(speedAccuracy: Float) {
        savePreference(PreferenceKeys.SPEED_ACCURACY, speedAccuracy, KEY_SPEED_ACCURACY, speedAccuracy)
    }

    // Speed Accuracy
    fun getSpeedAccuracy(): Float = remotePrefs()?.getFloat(KEY_SPEED_ACCURACY, DEFAULT_SPEED_ACCURACY) ?: DEFAULT_SPEED_ACCURACY


    // Hide Fake Location Toast
    fun getHideFakeLocationToastFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.HIDE_FAKE_LOCATION_TOAST, DEFAULT_HIDE_FAKE_LOCATION_TOAST)
    }

    suspend fun saveHideFakeLocationToast(hideFakeLocationToast: Boolean) {
        savePreference(PreferenceKeys.HIDE_FAKE_LOCATION_TOAST, hideFakeLocationToast, KEY_HIDE_FAKE_LOCATION_TOAST, hideFakeLocationToast)
    }

    // Hide Fake Location Toast
    fun getHideFakeLocationToast(): Boolean = remotePrefs()?.getBoolean(KEY_HIDE_FAKE_LOCATION_TOAST, DEFAULT_HIDE_FAKE_LOCATION_TOAST) ?: DEFAULT_HIDE_FAKE_LOCATION_TOAST

    // Use In-App Target Apps Selection
    fun getUseInAppTargetAppsFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.USE_INAPP_TARGET_APPS, DEFAULT_USE_INAPP_TARGET_APPS)
    }

    suspend fun saveUseInAppTargetApps(useInAppTargetApps: Boolean) {
        savePreference(PreferenceKeys.USE_INAPP_TARGET_APPS, useInAppTargetApps, KEY_USE_INAPP_TARGET_APPS, useInAppTargetApps)
    }

    // Enable Broadcast Control Receiver
    fun getEnableBroadcastControlFlow(): Flow<Boolean> {
        return getPreferenceFlow(PreferenceKeys.ENABLE_BROADCAST_CONTROL, DEFAULT_ENABLE_BROADCAST_CONTROL)
    }

    suspend fun saveEnableBroadcastControl(enable: Boolean) {
        savePreference(PreferenceKeys.ENABLE_BROADCAST_CONTROL, enable, KEY_ENABLE_BROADCAST_CONTROL, enable)
    }

    fun getLanguageTagFlow(): Flow<String> {
        return getPreferenceFlow(PreferenceKeys.LANGUAGE_TAG, DEFAULT_LANGUAGE_TAG)
    }

    suspend fun saveLanguageTag(languageTag: String) {
        savePreference(PreferenceKeys.LANGUAGE_TAG, languageTag, KEY_LANGUAGE_TAG, languageTag)
    }

    private fun getRemoteDouble(key: String, default: Double): Double {
        val prefs = remotePrefs() ?: return default
        val bits = prefs.getLong(key, java.lang.Double.doubleToRawLongBits(default))
        return java.lang.Double.longBitsToDouble(bits)
    }
}
