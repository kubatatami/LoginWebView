package com.github.kubatatami.rxexample


import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kubatatami.oauth.OAuth1Helper
import com.github.kubatatami.rxweb.RxLoginWebView
import com.github.kubatatami.web.LoginWebView
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.URLDecoder

class MainActivity : AppCompatActivity() {

    private var logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(logging).build()
    private val oAuthHelper = OAuth1Helper(CONSUMER_KEY, CONSUMER_SECRET)
    private lateinit var oauthToken: String
    private lateinit var oauthTokenSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        open.setOnClickListener { open() }
    }

    @SuppressLint("CheckResult")
    private fun open() {
        val request = oAuthHelper.requestToken(REQUEST_TOKEN_URL + LoginWebView.getCallbackUrl(this))
        okHttpClient.rxEnqueue(request)
            .flatMap { response ->
                val uri = Uri.parse("?" + response.body()!!.string())
                val url = parseParam(uri, "login_url")
                oauthToken = parseParam(uri, "oauth_token")
                oauthTokenSecret = parseParam(uri, "oauth_token_secret")
                RxLoginWebView.open(this@MainActivity, url)
            }
            .flatMap {
                val accessTokenRequest =
                    oAuthHelper.accessToken(ACCCESS_TOKEN_URL + it["oauth_verifier"], oauthToken, oauthTokenSecret)
                okHttpClient.rxEnqueue(accessTokenRequest)
            }.subscribe({ response ->
                val uri = Uri.parse("?" + response.body()!!.string())
                oauthToken = parseParam(uri, "oauth_token")
                oauthTokenSecret = parseParam(uri,"oauth_token_secret")
                Log.i("etsy", uri.toString())
            }, Throwable::printStackTrace)
    }

    private fun parseParam(uri: Uri, name: String) =
        URLDecoder.decode(uri.getQueryParameter(name), "utf-8")

    companion object {
        private const val ACCCESS_TOKEN_URL = "https://openapi.etsy.com/v2/oauth/access_token?oauth_verifier="
        private const val REQUEST_TOKEN_URL =
            "https://openapi.etsy.com/v2/oauth/request_token?scope=email_r&oauth_callback="
        private const val CONSUMER_KEY = ""
        private const val CONSUMER_SECRET = ""
    }
}
