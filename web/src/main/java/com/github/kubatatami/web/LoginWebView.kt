package com.github.kubatatami.web

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import saschpe.android.customtabs.CustomTabsHelper

class LoginWebView : AppCompatActivity() {

    private var finishOnNextResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIntent(intent) && savedInstanceState == null) {
            val url = intent.getStringExtra(EXTRA_URL)!!
            val customTabsIntent = CustomTabsIntent.Builder().enableUrlBarHiding().build()
            CustomTabsHelper.openCustomTab(this, customTabsIntent, Uri.parse(url), BrowserFallback())
            setResult(Activity.RESULT_CANCELED)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent): Boolean {
        val data = intent.data
        if (data is Uri) {
            val intentData = Intent().apply {
                data.queryParameterNames.forEach { name ->
                    val value = data.getQueryParameter(name)
                    putExtra(name, value)
                    Log.i("LoginWebView", "$name, $value")
                }
            }
            setResult(RESULT_OK, intentData)
            finish()
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (finishOnNextResume) {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        finishOnNextResume = true
    }

    class BrowserFallback : CustomTabsHelper.CustomTabFallback {

        override fun openUri(context: Context, uri: Uri) = context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }


    companion object {

        @JvmStatic
        fun initialization(app: Application) {
            // comment reason: https://github.com/saschpe/android-customtabs/issues/5
//            app.registerActivityLifecycleCallbacks(CustomTabsActivityLifecycleCallbacks())
        }

        @JvmStatic
        fun open(activity: Activity, url: String) {
            activity.startActivityForResult(LoginWebView.getIntent(activity,url), REQUEST_CODE)
        }

        @JvmStatic
        fun getCallbackUrl(context: Context) = "callback://${context.packageName}"

        @JvmStatic
        fun getIntent(context: Context, url: String): Intent {
            return Intent(context, LoginWebView::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
        }

        private const val EXTRA_URL = "url"
        const val REQUEST_CODE = 46356
    }
}