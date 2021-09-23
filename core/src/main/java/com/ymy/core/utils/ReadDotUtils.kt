package com.ymy.core.utils

/**
 * Created on 3/11/21 13:54.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object ReadDotUtils {

    fun getUnreadText(mBadgeNumber: Int): String {
        return when {
            mBadgeNumber > 99 -> {
                "99+"
            }
            mBadgeNumber in 1..99 -> {
                mBadgeNumber.toString()
            }
            else -> {
                ""
            }
        }
    }
}