package com.ymy.core.ok3

import com.orhanobut.logger.Logger
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created on 2020/9/9 16:40.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class ResponseHeaderInterceptor @JvmOverloads constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            throw e
        }
        try {
            val responseHeaderToken = response.headers["x-access-token"]
            responseHeaderToken?.run {
                Logger.e("responseHeaderToken:${responseHeaderToken}")
//                YmyUserManager.token = this
            }
        } catch (e: Exception) {
            throw e
        }
        return response
    }
}
