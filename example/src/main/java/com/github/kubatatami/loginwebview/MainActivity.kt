package com.github.kubatatami.loginwebview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.github.kubatatami.oauth.OAuth1Helper
import com.github.kubatatami.web.LoginWebView
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
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

    private fun open() {
        val request = oAuthHelper.requestToken(REQUEST_TOKEN__URL + LoginWebView.getCallbackUrl(this))
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val uri = Uri.parse("?" + response.body()!!.string())
                    val url = URLDecoder.decode(uri.getQueryParameter("login_url"), "utf-8")
                    oauthToken = URLDecoder.decode(uri.getQueryParameter("oauth_token"), "utf-8")
                    oauthTokenSecret = URLDecoder.decode(uri.getQueryParameter("oauth_token_secret"), "utf-8")
                    LoginWebView.open(this@MainActivity, url)
                }
            }
        })
    }

    private fun callForAccessToken(verifier: String) {
        val request = oAuthHelper.accessToken(
            "https://openapi.etsy.com/v2/oauth/access_token?oauth_verifier=$verifier", oauthToken, oauthTokenSecret
        )
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val uri = Uri.parse("?" + response.body()!!.string())
                    Log.i("etsy", uri.toString())
                    oauthToken = URLDecoder.decode(uri.getQueryParameter("oauth_token"), "utf-8")
                    oauthTokenSecret = URLDecoder.decode(uri.getQueryParameter("oauth_token_secret"), "utf-8")
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LoginWebView.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val verifier = data.getStringExtra("oauth_verifier")
            callForAccessToken(verifier)
        }
    }

    companion object {
        private const val REQUEST_TOKEN__URL =
            "https://openapi.etsy.com/v2/oauth/request_token?scope=email_r&oauth_callback="
        private const val CONSUMER_KEY = ""
        private const val CONSUMER_SECRET = ""
    }
}
