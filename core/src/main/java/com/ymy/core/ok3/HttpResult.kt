package com.ymy.core.ok3

import android.text.TextUtils
import com.ymy.core.ok3.DBXHttpError

/**
 * Created on 2020/7/17 14:15.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
sealed class HttpResult<out T : Any> {

    data class Success<out T : Any>(val data: T) : HttpResult<T>()
    data class Error(val exception: Exception) : HttpResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}

inline fun <T : Any> HttpResult<T>.checkResult(
    crossinline onSuccess: (T) -> Unit,
    crossinline onError: (String?) -> Unit
) {
    if (this is HttpResult.Success) {
        onSuccess(data)
    } else if (this is HttpResult.Error) {
        onError(
            when {
                exception is DBXHttpError -> {
                    exception.mErrorMessage
                }
                !(TextUtils.isEmpty(exception.message)) -> {
                    exception.message
                }
                else -> ""
            }
        )
    }
}

inline fun <T : Any> HttpResult<T>.checkSuccess(success: (T) -> Unit) {
    if (this is HttpResult.Success) {
        success(data)
    }
}