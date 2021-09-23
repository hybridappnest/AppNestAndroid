package com.ymy.core.utils

import android.content.Context
import android.net.ConnectivityManager


/**
 * Created by luyao
 * on 2018/3/13 16:16
 */
object NetWorkUtils {

    fun isNetworkAvailable(context: Context): Boolean {
        val manager = context.applicationContext.getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = manager.activeNetworkInfo
        return !(null == info || !info.isAvailable)
    }
}