package com.ymy.push.ali.cus.huaiwei

import com.alibaba.sdk.android.push.huawei.HuaweiPushMessageService
import com.alibaba.sdk.android.push.register.ThirdPushManager
import com.huawei.hms.push.RemoteMessage
import com.jeremyliao.liveeventbus.LiveEventBus
import com.orhanobut.logger.Logger
import com.ymy.push.ali.PushTokenData

/**
 * Created on 2/3/21 08:07.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class CusHuaweiPushMessageService : HuaweiPushMessageService() {
    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Logger.e("CusHuaweiPushMessageService push onNewToken $token")
        token?.run {
            LiveEventBus.get(PushTokenData::class.java)
                .post(
                    PushTokenData(
                        ThirdPushManager.ThirdPushReportKeyword.HUAWEI.thirdTokenKeyword,
                        token
                    )
                )
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        Logger.e("CusHuaweiPushMessageService onMessageReceived message=$remoteMessage")
    }
}