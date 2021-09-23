package com.ymy.core.base

/**
 * Created on 2020/8/11 17:55.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
interface Refresher {
    fun onRefresh()
}
interface ChangeTaber {
    fun changeTab(tabIndex:Int)
}