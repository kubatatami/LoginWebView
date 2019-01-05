package com.github.kubatatami.oauth

import okhttp3.Request
import java.net.URLEncoder
import kotlin.random.Random


class OAuth1Helper(private val consumerKey: String, private val consumerSecret: String) {

    private val nonce get() = Random.nextInt(0, 1000000)

    fun accessToken(url: String, accessToken: String, tokenSecret: String): Request {
        return createRequest(url, createHeader(accessToken, tokenSecret))
    }

    fun requestToken(url: String): Request {
        return createRequest(url, createHeader())
    }

    fun createHeader(accessToken: String? = null, tokenSecret: String = ""): String {
        var params = mapOf(
            Pair("oauth_version", "1.0"),
            Pair("oauth_consumer_key", consumerKey),
            Pair("oauth_nonce", nonce.toString()),
            Pair("oauth_signature_method", "PLAINTEXT"),
            Pair("oauth_timestamp", (System.currentTimeMillis() / 1000).toString())
        )
        if (accessToken != null) params += Pair("oauth_token", accessToken)
        val paramsList = params.entries.sortedBy { it.key }.map { Pair(it.key, it.value) } + Pair(
            "oauth_signature",
            encode("$consumerSecret&$tokenSecret")
        )
        return paramsList.joinToString(",") { "${it.first}=\"${it.second}\"" }
    }

    private fun createRequest(url: String, paramsHeader: String): Request {
        return Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "OAuth $paramsHeader")
            .build()
    }

    private fun encode(params: String) = URLEncoder.encode(params, "utf-8")

}