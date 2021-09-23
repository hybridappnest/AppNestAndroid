package com.ymy.appnest.net.model

import com.google.gson.annotations.SerializedName
import com.ymy.appnest.BuildConfig
import java.io.Serializable

/**
 * Created on 2021/9/13 15:14.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
data class AppConfigReq(
    @SerializedName("app_id")
    val appId: String = BuildConfig.APPLICATION_ID
) : Serializable