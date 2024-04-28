package com.luisli.cagpt.app
import android.app.Application
import android.content.Context
import com.luisli.cagpt.BuildConfig
import com.luisli.cagpt.R
import com.luisli.cagpt.utils.SharedPref
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
@HiltAndroidApp
class CA4OAGPTApp : Application() {
    companion object {
        var instance: CA4OAGPTApp? = null
            private set
        var appContext: Context? = null
            private set
    }
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        instance = this
        appContext = applicationContext
        SharedPref.setStringPref(
            this,
            SharedPref.KEY_TOKEN_LENGTH,
            getString(R.string.token_length)
        )
    }
}