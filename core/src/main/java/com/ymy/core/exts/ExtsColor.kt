package com.ymy.core.base

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Created on 2020/7/21 10:33.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
fun AppCompatActivity.getColorCompat(colorResId: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(colorResId)
    } else {
        resources.getColor(colorResId)
    }
}

fun Context.getColorCompat(colorResId: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(colorResId)
    } else {
        resources.getColor(colorResId)
    }
}

fun Fragment.getColorCompat(colorResId: Int): Int {
    return resources.getColor(colorResId)
}