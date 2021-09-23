package com.ymy.core.permission

import android.util.Log
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * Created on 2020/7/10 20:00.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class Weak<T : Any>(initializer: () -> T?) {
    var weakReference = WeakReference<T?>(initializer())

    constructor() : this({
        null
    })

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        Log.d("Weak Delegate", "-----------getValue")
        return weakReference.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        Log.d("Weak Delegate", "-----------setValue")
        weakReference = WeakReference(value)
    }

}