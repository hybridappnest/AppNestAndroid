package com.ymy.core.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ActivityCompat


/**
 * Created on 3/25/21 15:27.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object CallPhoneAndSendEmailUtils {
    /**
     * 拨打电话
     * @param num
     */
    @JvmStatic
    fun dialNum(context: Context,num: String?) {
        if (num != null && num.length > 0) {
            call(num, context)
        }
    }

    /**
     * 调用邮箱
     * @param address
     */
    @JvmStatic
    fun sendEmail(context: Context,address: String) {
        val receive = arrayOf(address)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "plain/text"
        intent.putExtra(Intent.EXTRA_EMAIL, receive)
        context.startActivity(Intent.createChooser(intent, ""))
    }


    private fun call(mobile: String?, activity: Context) {
        if (mobile == null || mobile.length == 0) {
            Toast.makeText(activity, "电话号码为空", Toast.LENGTH_SHORT).show()
            return
        }
        var phone = mobile.toLowerCase()
        if (!phone.startsWith("tel:")) {
            phone = "tel:$mobile"
        }
        val callMobile = phone

        //适配6.0系统，申请权限
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.CALL_PHONE
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity as Activity, arrayOf<String>(Manifest.permission.CALL_PHONE),
                0
            )
        } else {
            callPhone(activity, callMobile)
        }
    }

    private fun callPhone(activity: Context, callMobile: String?) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse(callMobile))
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.CALL_PHONE
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        activity.startActivity(intent)
    }


}