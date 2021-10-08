package com.ymy.appnest.web

import android.content.Context
import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.ymy.appnest.qrcode.HWQRCodeScannerActivity
import com.ymy.appnest.ui.gallery.MaxViewV2Activity
import com.ymy.core.bean.MaxBean
import com.ymy.core.lifecycle.KtxManager
import com.ymy.web.custom.*

/**
 * Created on 2021/9/29 09:40.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object AppJSActionHandler : JSNotificationHandler() {
    init {
        notificationList.add(JSNotificationAction.jsshowGallery + DEFAULT_JS_SUFFIX)
        notificationList.add(JSNotificationAction.jsSocialLogin + DEFAULT_JS_SUFFIX)
        notificationList.add(JSNotificationAction.jsScan + DEFAULT_JS_SUFFIX)
    }

    override fun onNewNotification(
        context: Context,
        eventName: String,
        param: JSONObject,
        announce: Int,
        jsCallBack: JSCallBack
    ): Boolean {
        return when (eventName) {
            JSNotificationAction.jsshowGallery + DEFAULT_JS_SUFFIX -> {
                val json = param.getString("data")
                try {
                    val gson = Gson()
                    val maxBean = gson.fromJson(json, MaxBean::class.java)
                    MaxViewV2Activity.invoke(KtxManager.currentActivity!!, maxBean)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                true
            }
            JSNotificationAction.jsSocialLogin + DEFAULT_JS_SUFFIX -> {
                socialLogin(context, jsCallBack, param, announce)
                true
            }
            JSNotificationAction.jsScan + DEFAULT_JS_SUFFIX -> {
                HWQRCodeScannerActivity.invoke(context, jsCallBack, announce)
                true
            }
            else -> false
        }
    }


    /**
     * 一键登录
     * @param context Context
     * @param jsCallBack JSCallBack
     * @param param JSONObject
     * @param announce Int
     */
    private fun socialLogin(
        context: Context, jsCallBack: JSCallBack, param: JSONObject, announce: Int
    ) {
        Logger.d(
            JSActionManager.TAG,
            "start JSLoginBridgeActivity eventName:socialLogin data:$param"
        )
        context.let {
            JSLoginBridgeActivity.invokeNew(
                it,
                jsCallBack,
                param.toString(),
                announce,
            )
        }
    }
}