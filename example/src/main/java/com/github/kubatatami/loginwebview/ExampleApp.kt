package com.github.kubatatami.loginwebview

import android.app.Application
import com.github.kubatatami.web.LoginWebView

class ExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LoginWebView.initialization(this)
    }
}
