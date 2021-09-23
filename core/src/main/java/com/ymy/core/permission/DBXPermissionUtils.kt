package com.ymy.core.permission

import android.Manifest
import android.content.Context
import com.orhanobut.logger.Logger
import pub.devrel.easypermissions.EasyPermissions

/**
 * Created on 2020/7/10 15:56.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class DBXPermissionUtils(context: Context) {

    companion object {
        const val CAMERA = Manifest.permission.CAMERA
        const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
        const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

        fun with(context: Context): DBXPermissionUtils {
            return DBXPermissionUtils(context)
        }
    }

    // 权限申请回调
    private lateinit var callback: PermissionCallback

    // 需要申请的权限
    lateinit var permissions: Array<String>
    lateinit var notice: String
    private var context by Weak {
        context
    }

    fun permission(notice: String, permissions: Array<String>): DBXPermissionUtils {
        this.permissions = permissions
        this.notice = notice
        return this
    }

    fun callback(callback: PermissionCallback): DBXPermissionUtils {
        this.callback = callback
        return this
    }

    fun request() {
        context?.run {
            if (EasyPermissions.hasPermissions(this, *permissions)) {
                Logger.e("hasRequestLocationPermission4")
                callback.onPermissionGranted()
            } else {
                Logger.e("hasRequestLocationPermission5")
                RequestPermissionActivity.request(this, notice, permissions, callback)
            }
        }
    }

    fun hasPermissions(permissions: Array<String>) =
        context?.run {
            EasyPermissions.hasPermissions(this, *permissions)
        } ?: false

}

/*
 * 权限申请回调
 */
interface PermissionCallback {
    /**
     * 用户给权限
     */
    fun onPermissionGranted()

    /**
     * 用户坚决不给权限
     */
    fun onPermissionsDenied()
}


/**
 * 权限请求
 * @param context Context 上下文对象
 * @param notice String 提示文案
 * @param permissions Array<String> 权限集合
 * @param actionGranted Function0<Unit> 请求权限成功后执行的代码块
 * @param actionDenied Function0<Unit> 请求权限失败后执行的代码块
 */
fun requestPermission(
    context: Context,
    notice: String,
    permissions: Array<String>,
    actionGranted: () -> Unit,
    actionDenied: () -> Unit,
) {
    DBXPermissionUtils.with(context).permission(notice, permissions)
        .callback(object : PermissionCallback {
            override fun onPermissionGranted() {
                Logger.e("onPermissionGranted2")
                return actionGranted.invoke()
            }

            override fun onPermissionsDenied() {
                Logger.e("onPermissionsDenied2")
                return actionDenied.invoke()
            }

        }).request()
}

fun hasPermission(
    context: Context,
    permissions: Array<String>,
) = DBXPermissionUtils.with(context).hasPermissions(permissions)
