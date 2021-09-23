package com.ymy.appnest.web.beans


import com.google.gson.annotations.SerializedName
import com.ymy.core.user.YmyUserManager
import com.ymy.appnest.web.WebUrlConstant

data class WebEnvs(
    @SerializedName("baseUrl")
    val baseUrl: String = "",

    @SerializedName("quick_url")
    val quickUrl: String = WebUrlConstant.quickWebBaseUrl,

    @SerializedName("h5_url")
    val h5Url: String = WebUrlConstant.hybridWebUrl,

    @SerializedName("javaUrl")
    val javaUrl: String = "",

    @SerializedName("company_alias")
    val companyAlias: String = YmyUserManager.user.companyName,

    @SerializedName("company_id")
    val companyId: String = YmyUserManager.user.companyId.toString(),

    @SerializedName("companyId")
    val company_Id: String = YmyUserManager.user.companyId.toString(),

    @SerializedName("companyName")
    val companyName: String = YmyUserManager.user.companyName,

    @SerializedName("mapid")
    val mapid: String = YmyUserManager.mapId,

    @SerializedName("token")
    val token: String = YmyUserManager.token,

    @SerializedName("userID")
    val userID: String = YmyUserManager.user.userId,

    @SerializedName("platform")
    val platform: String = "",
)