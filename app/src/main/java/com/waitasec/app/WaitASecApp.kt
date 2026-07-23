package com.waitasec.app

import android.app.Application
import com.waitasec.app.data.RestrictedAppsRepository

class WaitASecApp : Application() {
    lateinit var repository: RestrictedAppsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = RestrictedAppsRepository(this)
    }
}
