package com.ymy.core.ok3

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.ymy.core.retrofiturlmanager.RetrofitUrlManager
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import java.io.File

/**
 * Created on 2020/9/9 15:36.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object DBXRetrofitClient : BaseRetrofitClient() {
    lateinit var appContext: Context
    lateinit var mHttpListener: HttpListener

    fun putDomain(domainName: String, url: String) {
        RetrofitUrlManager.getInstance().putDomain(domainName, url)
    }

    fun initClient(context: Context, httpListener: HttpListener) {
        appContext = context
        mHttpListener = httpListener
    }

    fun cancelAllCalls() {
        client.dispatcher.cancelAll()
    }

    open interface HttpListener {
        fun doLogout()
    }

    private val cookieJar by lazy {
        PersistentCookieJar(
            SetCookieCache(),
            SharedPrefsCookiePersistor(appContext)
        )
    }

    override fun handleBuilder(builder: OkHttpClient.Builder) {

        val httpCacheDirectory = File(appContext.cacheDir, "responses")
        val cacheSize = 20 * 1024 * 1024L // 10 MiB
        val cache = Cache(httpCacheDirectory, cacheSize)
        builder.cache(cache)
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                var request = chain.request()
                if (!NetWorkUtils.isNetworkAvailable(appContext)) {
                    request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build()
                }
                val response = chain.proceed(request)
                if (!NetWorkUtils.isNetworkAvailable(appContext)) {
                    val maxAge = 60 * 60
                    response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=$maxAge")
                        .build()
                } else {
                    val maxStale = 60 * 60 * 24 * 28 // tolerate 4-weeks stale
                    response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                        .build()
                }
                response
            }
    }
}