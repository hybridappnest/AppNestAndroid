package com.ymy.core.ok3.base

import com.orhanobut.logger.Logger
import com.ymy.core.ok3.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException

/**
 * Created by hanxueqiang
 * on 2019/4/10 9:41
 */
open class BaseRepository {
    val TAG = "BaseRepository"

    suspend fun <T : Any> safeApiCall(
        call: suspend () -> HttpResult<T>,
        errorMessage: String = "",
    ): HttpResult<T> {
        return try {
            call()
        } catch (e: Exception) {
            if (e is HttpException) {
                if (e.code() == ERROR_CODE_401) {
                    DBXRetrofitClient.mHttpListener.doLogout()
                }
                Logger.e(e, e.toString())
                HttpResult.Error(DBXHttpError(e.code(), "网络异常", e = e))
            } else {
                Logger.e(e, e.toString())
                HttpResult.Error(DBXHttpError(mErrorMessage = "数据解析异常", e = e))
            }
        }
    }

    suspend fun <T : Any> executeResponse(
        baseResponse: BaseResponse<T>,
        successBlock: (suspend CoroutineScope.() -> Unit)? = null,
        errorBlock: (suspend CoroutineScope.() -> Unit)? = null,
    ): HttpResult<T> {
        return coroutineScope {
            baseResponse.run {
                if (code == CODE_200) {
                    successBlock?.let { it() }
                    HttpResult.Success(body)
                } else {
                    if (code == ERROR_CODE_401) {
                        DBXRetrofitClient.mHttpListener.doLogout()
                    }
                    errorBlock?.let { it() }
                    HttpResult.Error(
                        DBXHttpError(
                            code,
                            message,
                            null,
                        )
                    )
                }
            }
        }
    }
}