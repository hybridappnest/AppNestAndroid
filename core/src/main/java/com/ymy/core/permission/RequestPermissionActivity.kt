package com.ymy.core.permission

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.orhanobut.logger.Logger
import com.ymy.core.base.RootActivity
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest


class RequestPermissionActivity : RootActivity(), EasyPermissions.PermissionCallbacks {
    companion object {
        const val KEY_PERMISSIONS = "permissions"
        const val NOTICE = "NOTICE"
        private const val RC_REQUEST_PERMISSION = 100
        private var CALLBACK: PermissionCallback? = null

        fun request(
            context: Context,
            notice: String,
            permissions: Array<String>,
            callback: PermissionCallback,
        ) {
            CALLBACK = callback
            val intent = Intent(context, RequestPermissionActivity::class.java)
            val bundle = Bundle()
            bundle.putStringArray(KEY_PERMISSIONS, permissions)
            bundle.putString(NOTICE, notice)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    var permissions: Array<String>? = null
    var notice: String = ""

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras?.run {
            permissions = this.getStringArray(KEY_PERMISSIONS) as Array<String>
            notice = this.getString(NOTICE,"")
            if (permissions != null) {
                val request: PermissionRequest =
                    PermissionRequest.Builder(this@RequestPermissionActivity,
                        RC_REQUEST_PERMISSION,
                        *permissions as Array<String>).build()
                request.helper.directRequestPermissions(RC_REQUEST_PERMISSION,
                    *permissions as Array<String>)
            } else {
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (permissions != null) {
                //从设置页面返回，判断权限是否申请。
                if (EasyPermissions.hasPermissions(this, *permissions as Array<String>)) {
//                    Toast.makeText(this, "权限申请成功!", Toast.LENGTH_SHORT).show()
                    Logger.v("onPermissionGranted 成功")
                    CALLBACK?.onPermissionGranted()
                } else {
//                    Toast.makeText(this, "权限申请失败!", Toast.LENGTH_SHORT).show()
                    Logger.v("onPermissionGranted 失败")
                    CALLBACK?.onPermissionsDenied()
                }
            }
        }
        finish()
    }

    /**
     * 申请成功时调用
     * @param requestCode 请求权限的唯一标识码
     * @param perms 一系列权限
     */
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Logger.v("onPermissionGranted 成功")
        if (EasyPermissions.hasPermissions(this, *permissions as Array<String>)) {
            Logger.v("onPermissionGranted 成功1")
            CALLBACK?.onPermissionGranted()
            finish()
        } else {
            Logger.v("onPermissionGranted 成功2")
            AppSettingsDialog.Builder(this)
                .setRationale(notice)
                .setTitle("必需权限")
                .setPositiveButton("去设置")
                .setNegativeButton("拒绝")
                .build()
                .show()
        }
    }

    /**
     * 申请被拒绝
     * @param requestCode 请求权限的唯一标识码
     * @param perms 一系列权限
     */
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Logger.v("onPermissionsDenied 成功")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Logger.v("onPermissionsDenied 失败")
            AppSettingsDialog.Builder(this)
                .setRationale(notice)
                .setTitle("必需权限")
                .setPositiveButton("去设置")
                .setNegativeButton("拒绝")
                .build()
                .show()
            return
        }
        CALLBACK?.onPermissionsDenied()
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}