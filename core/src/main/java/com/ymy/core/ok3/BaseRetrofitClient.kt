package com.ymy.core.ok3

import android.util.Log
import com.ymy.core.BuildConfig
import com.ymy.core.retrofiturlmanager.RetrofitUrlManager
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


/**
 * Created on 2020/9/9 15:36.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
abstract class BaseRetrofitClient {

    companion object {
        private const val TIME_OUT = 15
    }


    protected val client: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
            val logging = FilterHttpLoggingInterceptor(object : FilterHttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.i("YMY_APP", "okhttp:$message")
                }
            }, listOf())
            if (BuildConfig.DEBUG) {
                logging.level = FilterHttpLoggingInterceptor.Level.BODY
            } else {
                logging.level = FilterHttpLoggingInterceptor.Level.BASIC
            }
            val trustEveryoneManager = TrustEveryoneManager()
            handleBuilder(builder
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .retryOnConnectionFailure(true)
                .addInterceptor(ResponseHeaderInterceptor())
                .addInterceptor(HeaderInterceptor())
                .addInterceptor(logging)
                .connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory(trustEveryoneManager),
                    trustEveryoneManager)
                .hostnameVerifier(TrustAllHostnameVerifier()))
            return RetrofitUrlManager.getInstance().with(builder).build()
        }

    protected abstract fun handleBuilder(builder: OkHttpClient.Builder)

    fun <S> getService(serviceClass: Class<S>, baseUrl: String): S {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonUtils.mGson))
            .baseUrl(baseUrl)
            .build()
            .create(serviceClass)
    }

    /**
     * 默认信任所有的证书
     * @return
     */
    open fun createSSLSocketFactory(trustEveryoneManager: TrustEveryoneManager): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        val trustManagers = arrayOf<TrustManager>(trustEveryoneManager)
        sslContext.init(null, trustManagers, null)
        return sslContext.socketFactory
    }

    class TrustEveryoneManager : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate>? {
            return arrayOf()
        }
    }

    private class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            return true
        }
    }
}
