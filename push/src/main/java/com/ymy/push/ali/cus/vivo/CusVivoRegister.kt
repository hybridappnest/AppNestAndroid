package com.ymy.push.ali.cus.vivo

import android.content.Context
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alibaba.sdk.android.push.VivoBadgeReceiver
import com.alibaba.sdk.android.push.impl.VivoMsgParseImpl
import com.alibaba.sdk.android.push.register.ThirdPushManager
import com.alibaba.sdk.android.push.register.ThirdPushManager.ThirdPushReportKeyword
import com.alibaba.sdk.android.push.utils.SysUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.taobao.accs.utl.ALog
import com.vivo.push.PushClient
import com.ymy.push.ali.PushTokenData

/**
 * Created on 2/2/21 20:32.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object CusVivoRegister {
    const val TAG = "MPS:vPush"
    private const val VIVO_TOKEN = "VIVO_TOKEN"
    private var mContext: Context? = null
    private var vivoBadgeReceiver: VivoBadgeReceiver? = null

    fun register(context: Context?): Boolean {
        try {
            if (context == null) {
                return false
            }
            mContext = context.applicationContext
            if (!SysUtils.isMainProcess(context)) {
                ALog.i("MPS:vPush", "not in main process, return", *arrayOfNulls(0))
                return false
            }
            if (PushClient.getInstance(context).isSupport) {
                ALog.i("MPS:vPush", "register start", *arrayOfNulls(0))
                ThirdPushManager.registerImpl(VivoMsgParseImpl())
                PushClient.getInstance(context).initialize()
                PushClient.getInstance(context).turnOnPush { state ->
                    ALog.i(
                        "MPS:vPush",
                        "oPushRegister turnOnPush",
                        *arrayOf<Any>("state", state)
                    )
                    if (state == 0) {
                        val regId = PushClient.getInstance(context).regId
                        ALog.i(
                            "MPS:vPush",
                            "onReceiveRegId regId:$regId",
                            *arrayOfNulls(0)
                        )
                        ThirdPushManager.reportToken(
                            context,
                            ThirdPushReportKeyword.VIVO.thirdTokenKeyword,
                            regId
                        )
                        LiveEventBus.get(PushTokenData::class.java)
                            .post(
                                PushTokenData(ThirdPushReportKeyword.VIVO.thirdTokenKeyword, regId)
                            )
                    }
                }
                vivoBadgeReceiver = VivoBadgeReceiver()
                val filter = IntentFilter()
                filter.addAction("msg.action.ACTION_MPM_MESSAGE_BOX_UNREAD")
                LocalBroadcastManager.getInstance(context)
                    .registerReceiver(vivoBadgeReceiver!!, filter)
                return true
            }
            ALog.i("MPS:vPush", "this device is not support vPush", *arrayOfNulls(0))
        } catch (var2: Throwable) {
            ALog.e("MPS:vPush", "register", var2, *arrayOfNulls(0))
        }
        return false
    }

    fun unregister() {
        ALog.i("MPS:vPush", "unregister", *arrayOfNulls(0))
        if (vivoBadgeReceiver != null) {
            LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(vivoBadgeReceiver!!)
        }
        PushClient.getInstance(mContext).turnOffPush { state ->
            ALog.d(
                "MPS:vPush",
                "turnOffPush state:$state",
                *arrayOfNulls(0)
            )
        }
    }
}