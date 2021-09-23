package com.ymy.core.exts

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created on 2021/5/8 15:00.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
/**
 * 消抖行为
 *
 */
fun debounceSuspend(delayMs: Long = 500L, coroutineScope: CoroutineScope, f: () -> Unit): () -> Unit {
    var job: Job? = null
    return {
        job?.cancel()
        job = coroutineScope.launch {
            delay(delayMs)
            job = null
            f()
        }
    }
}

/**
 * 主线程的消抖行为
 */
fun <T> debounceOnMainThread(delayMs: Long = 500L, callback: (T) -> Unit): (T) -> Unit {
    val handle = Handler(Looper.getMainLooper())
    var runnable: Runnable? = null
    return {t->
        runnable?.let {
            handle.removeCallbacks(it)
        }
        runnable = Runnable {
            callback(t)
        }
        handle.postDelayed(runnable!!, delayMs)
    }
}