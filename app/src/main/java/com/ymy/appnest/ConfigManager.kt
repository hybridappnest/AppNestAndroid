package com.ymy.appnest

import com.ymy.appnest.beans.AppConfig
import com.ymy.appnest.beans.HomeBottomNavigation
import com.ymy.appnest.beans.MineItemBean

/**
 * Created on 2021/9/13 14:44.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object ConfigManager {

    var nodeBaseUrl = "https://quickapi.365trtdoa.cn"

    var mAppConfig: AppConfig = AppConfig()
        set(value) {
            field = value
            mainTabList = value.mainTabList
            mineList = value.mineList
        }

    /**
     * 首页配置
     */
    var mainTabList: MutableList<HomeBottomNavigation> = mAppConfig.mainTabList

    /**
     * 我的页面配置
     */
    var mineList: MutableList<MineItemBean> = mAppConfig.mineList


}