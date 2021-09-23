package com.ymy.core.ok3.base

import com.google.gson.annotations.SerializedName

/**
 * Created on 2020/7/16 16:20.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
data class BaseResponse<out T>(
    val message: String = "",
    val code: Int = 0,
    val success: Boolean,
    @SerializedName("data")
    val body: T
)


data class QuickBaseResponse<out T>(
    val message: String = "",
    val code: Int = 0,
    val success: Boolean,
    @SerializedName("result")
    val body: T
)
