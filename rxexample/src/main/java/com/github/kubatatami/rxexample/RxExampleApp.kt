package com.github.kubatatami.rxexample

import android.app.Application
import com.github.kubatatami.rxweb.RxLoginWebView

class RxExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        RxLoginWebView.initialization(this)
    }
}
