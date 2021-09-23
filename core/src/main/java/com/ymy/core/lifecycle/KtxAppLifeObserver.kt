package com.ymy.core.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.orhanobut.logger.Logger

/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object KtxAppLifeObserver : LifecycleObserver {
    var appForeGround = false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
        appForeGround = true
        Logger.v("onForeground：$appForeGround")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
        appForeGround = false
        Logger.v("onBackground：$appForeGround")
    }
}