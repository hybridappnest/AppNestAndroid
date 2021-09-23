package com.ymy.core.exts

import com.ymy.core.Ktx
import com.ymy.core.utils.ToastUtils

/**
 * Created on 2020/10/7 09:01.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
inline fun appToast(str: String) {
    ToastUtils.showToast(Ktx.app, str)
}

inline fun appImageToast(str: String, success: Boolean) {
    ToastUtils.showImageToast(Ktx.app, str, success)
}

inline fun appTestToast(str: String) {
    ToastUtils.showTestToast(Ktx.app, str)
}