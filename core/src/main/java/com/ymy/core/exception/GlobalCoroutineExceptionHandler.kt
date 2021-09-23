package com.ymy.core.exception

import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

//For coroutines.
class GlobalCoroutineExceptionHandler: CoroutineExceptionHandler {
    override val key = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        Logger.e(exception, "Unhandled Coroutine Exception with ${context[Job]}")
    }
}