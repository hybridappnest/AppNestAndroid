package com.ymy.push.ali.cus.meizu

import android.content.Context
import android.text.TextUtils
import com.alibaba.sdk.android.push.MeizuPushReceiver
import com.alibaba.sdk.android.push.register.ThirdPushManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.meizu.cloud.pushsdk.platform.message.RegisterStatus
import com.taobao.accs.utl.ALog
import com.ymy.push.ali.PushTokenData

/**
 * Created on 2/3/21 17:13.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class CusMeizuPushReceiver : MeizuPushReceiver() {
    override fun onRegisterStatus(context: Context?, registerStatus: RegisterStatus?) {
        super.onRegisterStatus(context, registerStatus)
        if (registerStatus != null && !TextUtils.isEmpty(registerStatus.pushId)) {
            ALog.i(
                "MeizuPushReceiver",
                "onRegister",
                *arrayOf<Any>("status", registerStatus.toString())
            )
            LiveEventBus.get(PushTokenData::class.java)
                .post(
                    PushTokenData(
                        ThirdPushManager.ThirdPushReportKeyword.MEIZU.thirdTokenKeyword,
                        registerStatus.pushId
                    )
                )
        } else {
            ALog.e(
                "MeizuPushReceiver", "onRegisterStatus", *arrayOf<Any>(
                    "status",
                    registerStatus?.toString() ?: ""
                )
            )
            LiveEventBus.get(PushTokenData::class.java)
                .post(
                    PushTokenData(
                        ThirdPushManager.ThirdPushReportKeyword.MEIZU.thirdTokenKeyword,
                        ""
                    )
                )
        }
    }
}