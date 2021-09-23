package com.ymy.appnest.beans

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created on 2021/9/13 15:34.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
data class AppConfig(
    /**
     * 首页tab配置
     */
    @SerializedName("main")
    val mainTabList: MutableList<HomeBottomNavigation> = mutableListOf(),
    /**
     * 我的页面列表配置
     */
    @SerializedName("mine")
    val mineList: MutableList<MineItemBean> = mutableListOf(),
) : Serializable

data class AppConfigResp(
    val id: Long = 0L,
    val company_id: Long = 0L,
    val remark: String = "",
    val app_id: String = "",
    val config: String = "",
) : Serializable
