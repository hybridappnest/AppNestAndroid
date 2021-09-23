package com.ymy.core.utils

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager


/**
 * Created on 2020/7/23 09:41.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object ScreenUtils {

    /**
     * 获取屏幕宽度
     *
     * @param context Context
     * @return 屏幕宽度（px）
     */
    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.defaultDisplay.getRealSize(point)
        } else {
            wm.defaultDisplay.getSize(point)
        }
        return point.x
    }

    /**
     * 获取屏幕高度
     *
     * @param context Context
     * @return 屏幕高度（px）
     */
    @JvmStatic
    fun getScreenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.defaultDisplay.getRealSize(point)
        } else {
            wm.defaultDisplay.getSize(point)
        }
        return point.y
    }


    /**
     * dp 转换成 px
     */
    @JvmStatic
    fun dp2px(context: Context, dpValue: Int): Int {
        return (dpValue * context.resources.displayMetrics.density + 0.5).toInt()
    }

    @JvmStatic
    fun scaledSize(
        containerWidth: Int,
        containerHeight: Int,
        realWidth: Int,
        realHeight: Int
    ): IntArray? {
        val deviceRate = containerWidth.toFloat() / containerHeight.toFloat()
        val rate = realWidth.toFloat() / realHeight.toFloat()
        var width = 0
        var height = 0
        if (rate < deviceRate) {
            height = containerHeight
            width = (containerHeight * rate).toInt()
        } else {
            width = containerWidth
            height = (containerWidth / rate).toInt()
        }
        return intArrayOf(width, height)
    }
}