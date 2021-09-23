package com.ymy.core.utils

import android.content.Context
import com.ymy.core.Ktx

/**
 * Created on 2020/7/9 20:18.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object DensityUtil {
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 获得x方向的dp转像素
     * @param dpvalue
     * @return
     */
    fun dip2pxX(dpvalue: Float): Int {
        return (dpvalue * Ktx.app.getResources()
            .getDisplayMetrics().xdpi / 160 + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(pxValue: Float): Int {
        val scale = Ktx.app.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun px2sp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    /**
     * 获得y方向的dp转像素
     * @param dpvalue
     * @return
     */
    fun dip2pxY(dpvalue: Float): Int {
        return (dpvalue * Ktx.app.getResources()
            .getDisplayMetrics().ydpi / 160 + 0.5f).toInt()
    }
}