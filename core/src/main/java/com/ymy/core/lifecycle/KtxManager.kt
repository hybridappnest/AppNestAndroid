package com.ymy.core.lifecycle

import android.app.Activity
import com.umeng.analytics.MobclickAgent
import com.ymy.core.Ktx
import java.util.*

/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object KtxManager {

    private val mActivityList = LinkedList<Activity>()

    fun finishActivityTopOfTagActivity(act: Activity) {
        if (mActivityList.contains(act)) {
            while (true) {
                val removeLast = mActivityList.removeLast()
                if (removeLast != act) {
                    removeLast.finish()
                    continue
                } else {
                    mActivityList.add(removeLast)
                    break
                }
            }
        }
    }

    /**
     * 获取当前Activity下面的页面
     * @return Activity?
     */
    @JvmStatic
    fun getBottomActivity(): Activity? {
        return if (mActivityList.isEmpty()) {
            null
        } else {
            try {
                mActivityList[mActivityList.size - 2]
            } catch (e: Exception) {
                null
            }
        }
    }

    @JvmStatic
    val currentActivity: Activity?
        get() =
            if (mActivityList.isEmpty()) null
            else mActivityList.last

    @JvmStatic
    fun findActivity(name: String): Boolean {
        mActivityList.forEach {
            if (it::class.java.name == name) {
                return true
            }
        }
        return false
    }


    /**
     * push the specified [activity] into the list
     */
    @JvmStatic
    fun pushActivity(activity: Activity) {
        if (mActivityList.contains(activity)) {
            if (mActivityList.last != activity) {
                mActivityList.remove(activity)
                mActivityList.add(activity)
            }
        } else {
            mActivityList.add(activity)
        }
    }

    /**
     * pop the specified [activity] into the list
     */
    @JvmStatic
    fun popActivity(activity: Activity) {
        mActivityList.remove(activity)
    }

    @JvmStatic
    fun finishCurrentActivity() {
        currentActivity?.finish()
    }

    @JvmStatic
    fun finishActivity(activity: Activity) {
        mActivityList.remove(activity)
        activity.finish()
    }

    @JvmStatic
    fun finishActivity(clazz: Class<*>) {
        for (activity in mActivityList)
            if (activity.javaClass == clazz)
                activity.finish()
    }

    @JvmStatic
    fun finishAllActivity() {
        MobclickAgent.onKillProcess(Ktx.app)
        for (activity in mActivityList)
            activity.finish()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

}