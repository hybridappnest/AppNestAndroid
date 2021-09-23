package com.ymy.core.ok3

import com.ymy.core.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Created on 2020/9/9 16:40.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class HeaderInterceptor @JvmOverloads constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val newRequest: Request = addHeader(oldRequest)
        return chain.proceed(newRequest)
    }

    private fun addParams(oldRequest: Request): Request {
        return oldRequest.newBuilder().method(oldRequest.method, oldRequest.body)
            .url(oldRequest
                .url
                .newBuilder()
                .setEncodedQueryParameter("x-access-token","")
                .build())
            .build()
    }

    private fun addHeader(oldRequest: Request): Request {
//        val value = YmyUserManager.token
        return oldRequest.newBuilder()
            .header("x-access-token", "")
            .header("appVersion", BuildConfig.VERSION_NAME)
            .build()
    }

}