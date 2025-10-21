package com.example.androidgamekt

import android.app.Application
import com.example.androidgamekt.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GameApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@GameApp)
            modules(appModule)
        }
    }
}

