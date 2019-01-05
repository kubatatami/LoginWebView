package com.github.kubatatami.rxweb

import android.app.Activity
import android.app.Application
import com.github.kubatatami.web.LoginWebView
import io.reactivex.Single
import rx_activity_result2.RxActivityResult
import java.util.concurrent.CancellationException


object RxLoginWebView {

    @JvmStatic
    fun initialization(app: Application) {
        LoginWebView.initialization(app)
        RxActivityResult.register(app)
    }

    @JvmStatic
    fun open(activity: Activity, url: String): Single<Map<String, String>> {
        return RxActivityResult.on(activity).startIntent(LoginWebView.getIntent(activity, url))
            .firstOrError()
            .flatMap { result ->
                val extras = result.data()?.extras
                if (result.resultCode() == Activity.RESULT_OK && extras != null) {
                    Single.just(extras.keySet().associateWith { extras.getString(it) })
                } else {
                    Single.error(CancellationException())
                }
            }
    }

}