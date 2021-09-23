package com.ymy.core.ok3

/**
 * Created on 2020/7/31 14:35.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

const val CODE_200 = 200

//token过期
const val ERROR_CODE_401 = 401

class DBXHttpError constructor(
    var mErrorCode: Int = 0,
    var mErrorMessage: String = "网络请求异常",
    var e: Exception?
) :
    Exception() {

    override fun toString(): String {
        return "ErrorCode:$mErrorCode : mErrorMessage: $mErrorMessage"
    }
}