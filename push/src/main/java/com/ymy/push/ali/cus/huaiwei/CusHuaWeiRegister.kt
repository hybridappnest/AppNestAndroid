package com.ymy.push.ali.cus.huaiwei

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister
import com.alibaba.sdk.android.push.impl.HuaweiMsgParseImpl
import com.alibaba.sdk.android.push.register.ThirdPushManager
import com.alibaba.sdk.android.push.register.ThirdPushManager.ThirdPushReportKeyword
import com.alibaba.sdk.android.push.utils.SysUtils
import com.alibaba.sdk.android.push.utils.ThreadUtil
import com.huawei.hms.aaid.HmsInstanceId
import com.jeremyliao.liveeventbus.LiveEventBus
import com.taobao.accs.utl.ALog
import com.ymy.push.ali.PushTokenData

/**
 * Created on 2/4/21 13:50.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:自定义的HuaWeiRegister，阿里移动推送不会吐出厂商token，只能通过重新阿里移动推送sdk中对应的类来获取token
 * 因各个厂商渠道的token获取方式不一致，有的需重写对应的Register，有的需要重写对应的Service或receiver
 */
object CusHuaWeiRegister {
    private val TAG = "MPS:CusHuaWeiRegister"
    var isChannelRegister = false

    fun register(application: Application): Boolean {
        return registerBundle(application, false)
    }

    fun registerBundle(application: Application, channelRegister: Boolean): Boolean {
        try {
            isChannelRegister = channelRegister
            if (!isChannelRegister && !SysUtils.isMainProcess(application)) {
                ALog.e(
                    "MPS:HuaWeiRegister",
                    "register not in main process, return",
                    *arrayOfNulls(0)
                )
                return false
            }
            if (checkDevice() && Build.VERSION.SDK_INT >= 17) {
                ThirdPushManager.registerImpl(HuaweiMsgParseImpl())
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    ALog.i(
                        "MPS:HuaWeiRegister",
                        "register begin isChannel:" + HuaWeiRegister.isChannelRegister,
                        *arrayOfNulls(0)
                    )
                    getToken(application.applicationContext)
                }, 5000L)
                return true
            }
            ALog.i("MPS:HuaWeiRegister", "register checkDevice false", *arrayOfNulls(0))
        } catch (var3: Throwable) {
            ALog.e("MPS:HuaWeiRegister", "register", var3, *arrayOfNulls(0))
        }
        return false
    }

    private fun getToken(context: Context) {
        ThreadUtil.getExecutor().execute {
            try {
                val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                val value = appInfo.metaData.getString("com.huawei.hms.client.appid")
                var appId = ""
                if (!TextUtils.isEmpty(value)) {
                    appId = value!!.replace("appid=", "")
                }
                ALog.i(
                    "MPS:HuaWeiRegister",
                    "onToken",
                    *arrayOf<Any>("appId", appId)
                )
                val token: String
                token = if (TextUtils.isEmpty(appId)) {
                    HmsInstanceId.getInstance(context).token
                } else {
                    HmsInstanceId.getInstance(context).getToken(appId, "HCM")
                }
                if (!TextUtils.isEmpty(token)) {
                    ALog.i(
                        "MPS:HuaWeiRegister",
                        "onToken",
                        *arrayOf<Any>("token", token)
                    )
                    token?.run {
                        LiveEventBus.get(PushTokenData::class.java)
                            .post(
                                PushTokenData(
                                    ThirdPushReportKeyword.HUAWEI.thirdTokenKeyword,
                                    token
                                )
                            )
                    }
                    ThirdPushManager.reportToken(
                        context,
                        ThirdPushReportKeyword.HUAWEI.thirdTokenKeyword,
                        token
                    )
                }
            } catch (var5: Exception) {
                ALog.e(
                    "MPS:HuaWeiRegister",
                    "getToken failed.",
                    var5,
                    *arrayOfNulls(0)
                )
            }
        }
    }

    private fun checkDevice(): Boolean {
        var result = false
        if (Build.BRAND.equals("huawei", ignoreCase = true) || Build.BRAND.equals(
                "honor",
                ignoreCase = true
            )
        ) {
            result = true
        }
        return result
    }
}