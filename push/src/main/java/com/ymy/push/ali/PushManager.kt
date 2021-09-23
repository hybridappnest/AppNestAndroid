package com.ymy.push.ali

import androidx.lifecycle.LifecycleOwner
import com.alibaba.sdk.android.push.CloudPushService
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.alibaba.sdk.android.push.register.MeizuRegister
import com.jeremyliao.liveeventbus.LiveEventBus
import com.orhanobut.logger.Logger
import com.xiaomi.mipush.sdk.MiPushClient
import com.ymy.core.Ktx
import com.ymy.core.user.YmyUserManager
import com.ymy.core.utils.BrandUtil
import com.ymy.push.PushConts
import com.ymy.push.ali.cus.huaiwei.CusHuaWeiRegister
import com.ymy.push.ali.cus.oppo.CusOppoRegister
import com.ymy.push.ali.cus.vivo.CusVivoRegister

/**
 * Created on 2/4/21 15:03.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object PushManager {
    val appContext = Ktx.app
    val mPushTokenCallBack: PushTokenCallBack? = null

    interface PushTokenCallBack {
        fun getPushToken(channel: String, token: String)
    }

    private val cloudPushService: CloudPushService by lazy {
        PushServiceFactory.getCloudPushService()
    }

    fun registerPush(act: LifecycleOwner) {
        LiveEventBus.get(PushTokenData::class.java).observe(act) {
            it?.run {
                Logger.e("绑定定厂商渠道 keyword$keyword pushToken $token")
                mPushTokenCallBack?.run {
                    this.getPushToken(keyword, token)
                }
            }
        }
        when {
            BrandUtil.isBrandXiaoMi() -> {
                // 初始化小米辅助推送
                MiPushClient.registerPush(
                    appContext,
                    PushConts.XIAOMI_APP_ID,
                    PushConts.XIAOMI_APP_KEY
                )
            }
            BrandUtil.isBrandHuawei() -> {
                CusHuaWeiRegister.register(appContext) // 接入华为辅助推送
            }
            BrandUtil.isBrandMeizu() -> {
                MeizuRegister.register(
                    appContext,
                    PushConts.MEIZU_APP_ID,
                    PushConts.MEIZU_APP_KEY
                )
            }
            BrandUtil.isBrandOppo() -> {
                CusOppoRegister.register(
                    appContext,
                    PushConts.OPPO_APP_KEY,
                    PushConts.OPPO_APP_SECRET
                )
            }
            BrandUtil.isBrandVivo() -> {
                CusVivoRegister.register(appContext)
            }
        }
    }

    fun binderUser() {
        cloudPushService.bindAccount(YmyUserManager.user.userId, object : CommonCallback {
            override fun onSuccess(s: String) {
                Logger.e("bindAccount ${YmyUserManager.user.userId} success")
            }

            override fun onFailed(errorCode: String, errorMsg: String) {
                Logger.e("bindAccount ${YmyUserManager.user.userId} failed.errorCode: $errorCode, errorMsg:$errorMsg")
            }
        })
    }

    fun unBinderUser() {
        cloudPushService.unbindAccount(object : CommonCallback {
            override fun onSuccess(s: String) {
                Logger.e("unbindAccount success")
            }

            override fun onFailed(errorCode: String, errorMsg: String) {
                Logger.e("unbindAccount failed.errorCode: $errorCode, errorMsg:$errorMsg")
            }
        })
    }

}