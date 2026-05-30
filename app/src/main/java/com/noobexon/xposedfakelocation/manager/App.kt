package com.noobexon.xposedfakelocation.manager

import android.app.Application
import com.noobexon.xposedfakelocation.data.repository.PreferencesRepository
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

class App : Application(), XposedServiceHelper.OnServiceListener {
    companion object {
        @Volatile var service: XposedService? = null; private set
    }

    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(this)   // exactly once
    }

    override fun onServiceBind(service: XposedService) {
        Companion.service = service
        PreferencesRepository(this).syncAllToRemote()
    }

    override fun onServiceDied(service: XposedService) {
        Companion.service = null
    }
}