package com.ymy.appnest.beans

import java.io.Serializable

/**
 * Created on 2020/8/11 10:43.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

/**
 * 我的tab中可以执行的动作
 */
object MineListAction {
    /**
     * 空行
     */
    const val empty = "empty"

    /**
     * 设置中心
     */
    const val settingCenter = "settingCenter"

    /**
     * 扫码
     */
    const val QRCodeScanner = "QRCodeScanner"

    /**
     * 安全中心
     */
    const val about = "about"

    /**
     * web页面
     */
    const val web = "web"
}

/**
 * 我的tab下的list
 * @property name String 显示名称
 * @property iconRes Int 图标资源id
 * @property iconUrl String 图标url
 * @property action String 动作
 * @property redDot Int 未读红点数
 * @property url String 跳转url
 * @constructor
 */
data class MineItemBean(
    val name: String = "",
    val iconRes: Int = -1,
    val iconUrl: String = "",
    val action: String = MineListAction.empty,
    var redDot: Int = 0,
    val url: String = "",
) : Serializable