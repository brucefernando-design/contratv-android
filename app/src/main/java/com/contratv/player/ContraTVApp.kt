package com.contratv.player

import android.app.Application

class ContraTVApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ContraTVApp
            private set
    }
}
