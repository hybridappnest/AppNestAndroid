package com.ymy.push.ali.cus.xiaomi

import android.content.Context
import android.text.TextUtils
import com.alibaba.sdk.android.push.register.ThirdPushManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.orhanobut.logger.Logger
import com.taobao.accs.utl.ALog
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.ymy.push.ali.PushTokenData

/**
 * Created on 2/3/21 08:08.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
//class CusMiPushBroadcastReceiver : MiPushBroadcastReceiver() {
//    override fun onReceiveRegisterResult(context: Context, message: MiPushCommandMessage) {
//        super.onReceiveRegisterResult(context, message)
//        val command = message.command
//        val arguments = message.commandArguments
//        val cmdArg1 = if (arguments != null && arguments.size > 0) arguments[0] else null
//        var regId: String? = null
//        Logger.d(
//            "MPS:MiPushBroadcastReceiver",
//            "onReceiveRegisterResult command : $command"
//        )
//        if ("register" == command) {
//            Logger.d(
//                "MPS:MiPushBroadcastReceiver",
//                "onReceiveRegisterResult result code: " + message.resultCode + "success is: 0"
//            )
//            if (message.resultCode == 0L) {
//                regId = cmdArg1
//            }
//        }
//
//        ALog.d(
//            "MPS:MiPushBroadcastReceiver",
//            "onReceiveRegisterResult regId:$regId", *arrayOfNulls(0)
//        )
//        if (!TextUtils.isEmpty(regId)) {
//            LiveEventBus.get(PushTokenData::class.java)
//                .post(
//                    PushTokenData(ThirdPushManager.ThirdPushReportKeyword.XIAOMI.thirdTokenKeyword, regId!!)
//                )
//        }
//    }
//}