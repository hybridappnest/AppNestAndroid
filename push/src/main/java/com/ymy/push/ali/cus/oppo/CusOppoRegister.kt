package com.ymy.push.ali.cus.oppo

import android.content.Context
import android.os.Build
import com.alibaba.sdk.android.push.impl.OppoMsgParseImpl
import com.alibaba.sdk.android.push.register.ThirdPushManager
import com.alibaba.sdk.android.push.register.ThirdPushManager.ThirdPushReportKeyword
import com.alibaba.sdk.android.push.utils.SysUtils
import com.heytap.msp.push.HeytapPushManager
import com.heytap.msp.push.callback.ICallBackResultService
import com.jeremyliao.liveeventbus.LiveEventBus
import com.taobao.accs.utl.ALog
import com.ymy.push.ali.PushTokenData

/**
 * Created on 2/2/21 20:32.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object CusOppoRegister {

    const val TAG = "MPS:OPush"
    private const val TOKEN = "OPPO_TOKEN"

    fun register(context: Context, appKey: String?, appSecret: String?): Boolean {
        try {
            if (Build.VERSION.SDK_INT < 19) {
                return false
            }
            val finalContext = context.applicationContext
            if (!SysUtils.isMainProcess(finalContext)) {
                ALog.i("MPS:OPush", "not in main process, return", *arrayOfNulls(0))
                return false
            }
            HeytapPushManager.init(finalContext, finalContext.applicationInfo.flags and 2 != 0)
            if (HeytapPushManager.isSupportPush()) {
                ThirdPushManager.registerImpl(OppoMsgParseImpl())
                ALog.i("MPS:OPush", "register oppo begin ", *arrayOfNulls(0))
                HeytapPushManager.register(
                    finalContext,
                    appKey,
                    appSecret,
                    object : ICallBackResultService {
                        override fun onRegister(responseCode: Int, registerID: String) {
                            ALog.i(
                                "MPS:OPush",
                                "onRegister code=$responseCode regid=$registerID", *arrayOfNulls(0)
                            )
                            if (responseCode == 0) {
                                ThirdPushManager.reportToken(
                                    finalContext,
                                    ThirdPushReportKeyword.OPPO.thirdTokenKeyword,
                                    registerID
                                )
                                LiveEventBus.get(PushTokenData::class.java)
                                    .post(
                                        PushTokenData(ThirdPushReportKeyword.OPPO.thirdTokenKeyword, registerID)
                                    )
                            } else {
                                ThirdPushManager.reportToken(
                                    finalContext,
                                    ThirdPushReportKeyword.OPPO.thirdTokenKeyword,
                                    ""
                                )
                                LiveEventBus.get(PushTokenData::class.java)
                                    .post(
                                        PushTokenData(ThirdPushReportKeyword.OPPO.thirdTokenKeyword, "")
                                    )
                            }
                        }

                        override fun onUnRegister(responseCode: Int) {
                            ALog.e("MPS:OPush", "onUnRegister code=$responseCode", *arrayOfNulls(0))
                        }

                        override fun onSetPushTime(responseCode: Int, pushTime: String) {
                            ALog.i(
                                "MPS:OPush",
                                "onSetPushTime code=$responseCode pushTime is $pushTime",
                                *arrayOfNulls(0)
                            )
                        }

                        override fun onGetPushStatus(responseCode: Int, status: Int) {
                            ALog.i(
                                "MPS:OPush",
                                "onGetPushStatus code=$responseCode status=$status",
                                *arrayOfNulls(0)
                            )
                        }

                        override fun onGetNotificationStatus(responseCode: Int, status: Int) {
                            ALog.i(
                                "MPS:OPush",
                                "onGetNotificationStatus code=$responseCode status=$status",
                                *arrayOfNulls(0)
                            )
                        }
                    })
                return true
            }
            ALog.i("MPS:OPush", "not support oppo push", *arrayOfNulls(0))
        } catch (var4: Throwable) {
            ALog.e("MPS:OPush", "register error", var4, *arrayOfNulls(0))
        }
        return false
    }
}