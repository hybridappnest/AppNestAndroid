package com.ymy.core.push

/**
 * Created on 2021/10/8 08:55.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object PushCenter {
    private val allPushTokenObservers: MutableList<PushTokenObserver> = mutableListOf()

    interface PushTokenObserver {
        fun pushToken(channel: String, token: String)
    }

    fun addObserver(observer: PushTokenObserver) {
        allPushTokenObservers.add(observer)
    }

    fun removeObserver(observer: PushTokenObserver) {
        allPushTokenObservers.remove(observer)
    }

    fun setPushToken(channel: String, token: String) {
        allPushTokenObservers.forEach {
            it.pushToken(channel, token)
        }
    }
}