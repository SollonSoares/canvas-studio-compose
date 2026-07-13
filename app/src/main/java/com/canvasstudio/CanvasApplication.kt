package com.canvasstudio

import android.app.Application
import com.canvasstudio.data.di.AppContainer
import com.canvasstudio.data.di.AppContainerImpl

class CanvasApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}