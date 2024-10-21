//SettingsViewModel.kt
package com.noobexon.xposedfakelocation.manager.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noobexon.xposedfakelocation.data.DEFAULT_ACCURACY
import com.noobexon.xposedfakelocation.data.DEFAULT_ALTITUDE
import com.noobexon.xposedfakelocation.data.DEFAULT_USE_ACCURACY
import com.noobexon.xposedfakelocation.data.DEFAULT_USE_ALTITUDE
import com.noobexon.xposedfakelocation.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesRepository = PreferencesRepository(application)

    private val _useAccuracy = MutableStateFlow(DEFAULT_USE_ACCURACY)
    val useAccuracy: StateFlow<Boolean> get() = _useAccuracy

    private val _accuracy = MutableStateFlow(DEFAULT_ACCURACY)
    val accuracy: StateFlow<Float> get() = _accuracy

    private val _useAltitude = MutableStateFlow(DEFAULT_USE_ALTITUDE)
    val useAltitude: StateFlow<Boolean> get() = _useAltitude

    private val _altitude = MutableStateFlow(DEFAULT_ALTITUDE)
    val altitude: StateFlow<Float> get() = _altitude

    private val _randomize = MutableStateFlow(false)
    val randomize: StateFlow<Boolean> get() = _randomize

    init {
        viewModelScope.launch {
            _accuracy.value = preferencesRepository.getAccuracy()
            _altitude.value = preferencesRepository.getAltitude()
            _randomize.value = preferencesRepository.getUseRandomize()
            _useAccuracy.value = preferencesRepository.getUseAccuracy()
            _useAltitude.value = preferencesRepository.getUseAltitude()
        }
    }

    fun setUseAccuracy(value: Boolean) {
        _useAccuracy.value = value
        preferencesRepository.saveUseAccuracy(value)
    }

    fun setAccuracy(value: Float) {
        _accuracy.value = value
        preferencesRepository.saveAccuracy(value)
    }

    fun setUseAltitude(value: Boolean) {
        _useAltitude.value = value
        preferencesRepository.saveUseAltitude(value)
    }

    fun setAltitude(value: Float) {
        _altitude.value = value
        preferencesRepository.saveAltitude(value)
    }

    fun setRandomize(value: Boolean) {
        _randomize.value = value
        preferencesRepository.saveUseRandomize(value)
    }
}