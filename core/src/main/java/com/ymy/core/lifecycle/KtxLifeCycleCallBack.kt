package com.ymy.core.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.orhanobut.logger.Logger

/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object KtxLifeCycleCallBack : Application.ActivityLifecycleCallbacks {

    var LifeCycleListener:LifeCycleListener? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Logger.v("onActivityCreated : ${activity.componentName}")
        KtxManager.pushActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        Logger.v("onActivityStarted : ${activity.componentName}")
    }

    override fun onActivityResumed(activity: Activity) {
        Logger.v("onActivityResumed : ${activity.componentName}")
        LifeCycleListener?.onActivityResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        Logger.v( "onActivityPaused : ${activity.componentName}")
        LifeCycleListener?.onActivityPaused(activity)
    }


    override fun onActivityDestroyed(activity: Activity) {
        Logger.v("onActivityDestroyed : ${activity.componentName}")
        KtxManager.popActivity(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        Logger.v("onActivityStopped : ${activity.componentName}")
    }

    /**
     * Called when the Activity calls
     * [super.onSaveInstanceState()][Activity.onSaveInstanceState].
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }


}

interface LifeCycleListener{
    fun onActivityResumed(activity: Activity)
    fun onActivityPaused(activity: Activity)
}