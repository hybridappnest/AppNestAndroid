package com.ymy.core.exception

import com.orhanobut.logger.Logger
import java.lang.Thread.UncaughtExceptionHandler

//For threads.
class GlobalThreadUncaughtExceptionHandler : UncaughtExceptionHandler {

    companion object {
        fun setUp() {
            Thread.setDefaultUncaughtExceptionHandler(GlobalThreadUncaughtExceptionHandler())
        }
    }

    //Don't use lazy here.
    private val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        Logger.e(e, "Uncaugth exception in thread: ${t.name}")
        defaultUncaughtExceptionHandler?.uncaughtException(t, e)
    }
}