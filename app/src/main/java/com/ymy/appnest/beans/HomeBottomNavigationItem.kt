package com.ymy.appnest.beans

import com.ymy.appnest.R
import java.io.Serializable

/**
 * Created on 2021/3/17 10:43.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

object HomeTabType {
    /**
     * 空行
     */
    const val empty = "empty"


    /**
     * web页面
     */
    const val web = "web"

    /**
     * 我的页面
     */
    const val mine = "mine"

    /**
     * IM
     */
    const val IM = "IM"
}

data class HomeBottomNavigation(
    val name: String = "",
    val imageRes: Int = R.mipmap.icon_daily_normal,
    val imageResSelected: Int = R.mipmap.icon_daily_normal,
    val iconUrl: String = "",
    val textColorRes: String = "#595F7E",
    val textColorResSelected: String = "#32B8EC",
    var url: String = "https://www.baidu.com/",
    var isSelected: Boolean = false,
    var unRead: Int = 0,
    var type: String = "",
) : Serializable