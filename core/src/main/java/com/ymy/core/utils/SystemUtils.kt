package com.ymy.core.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo


/**
 * Created on 2020/7/24 13:20.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object SystemUtils {

    fun isAppRunning(context: Context, packageName: String): Boolean {
        val uid = getPackageUid(context, packageName)
        return if (uid > 0) {
            val rstA = isAppRunningByPackageName(context, packageName)
            val rstB = isProcessRunning(context, uid)
            rstA || rstB
        } else {
            false
        }
    }

    /**
     * 方法描述：判断某一应用是否正在运行
     * Created by cafeting on 2017/2/4.
     * @param context     上下文
     * @param packageName 应用的包名
     * @return true 表示正在运行，false 表示没有运行
     */
    private fun isAppRunningByPackageName(context: Context, packageName: String): Boolean {
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = am.getRunningTasks(100)
        if (list.size <= 0) {
            return false
        }
        for (info in list) {
            if (info.baseActivity!!.packageName == packageName) {
                return true
            }
        }
        return false
    }


    //获取已安装应用的 uid，-1 表示未安装此应用或程序异常
    private fun getPackageUid(context: Context, packageName: String): Int {
        try {
            val applicationInfo: ApplicationInfo =
                context.getPackageManager().getApplicationInfo(packageName, 0)
            if (applicationInfo != null) {
                return applicationInfo.uid
            }
        } catch (e: Exception) {
            return -1
        }
        return -1
    }

    /**
     * 判断某一 uid 的程序是否有正在运行的进程，即是否存活
     * Created by cafeting on 2017/2/4.
     *
     * @param context     上下文
     * @param uid 已安装应用的 uid
     * @return true 表示正在运行，false 表示没有运行
     */
    private fun isProcessRunning(context: Context, uid: Int): Boolean {
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServiceInfos =
            am.getRunningServices(200)
        if (runningServiceInfos.size > 0) {
            for (appProcess in runningServiceInfos) {
                if (uid == appProcess.uid) {
                    return true
                }
            }
        }
        return false
    }
}