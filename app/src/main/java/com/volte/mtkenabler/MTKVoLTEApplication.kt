package com.volte.mtkenabler

import android.app.Application
import android.util.Log
import com.topjohnwu.superuser.Shell

class MTKVoLTEApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize libsu
        Shell.enableVerboseLogging = BuildConfig.DEBUG
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )

        Log.d(TAG, "MTK VoLTE Enabler Application Started")
    }

    companion object {
        private const val TAG = "MTKVoLTEApp"
        lateinit var instance: MTKVoLTEApplication
            private set
    }
}
