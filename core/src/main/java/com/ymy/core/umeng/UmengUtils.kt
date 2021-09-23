package com.ymy.core.umeng

import android.content.Context
import com.orhanobut.logger.Logger
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.commonsdk.statistics.common.DeviceConfig
import com.ymy.core.Ktx
import com.ymy.core.ok3.GsonUtils

/**
 * Created on 3/23/21 10:11.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object UmengUtils {
    private var appkey = ""

    /**
     * umeng
     */
    private const val standard_umeng_appkey = ""
    private const val local_umeng_appkey = ""

    fun perInit() {
        UMConfigure.preInit(Ktx.app, appkey, "Umeng")
        UMConfigure.setLogEnabled(false)
    }

    fun init() {
        UMConfigure.init(
            Ktx.app, appkey,
            "Umeng",
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
    }

    fun getTestID(context: Context) {
        try {
            val umengTestinfo = UmengTestInfo(
                DeviceConfig.getDeviceIdForGeneral(context),
                DeviceConfig.getMac(context)
            )
            Logger.e("UmengUtils:testId:${GsonUtils.mGson.toJson(umengTestinfo)}")
        } catch (e: Exception) {

        }
    }

    fun uploadEvent(
        context: Context,
        eventName: String,
        map: MutableMap<String, String> = mutableMapOf()
    ) {
        MobclickAgent.onEvent(context, eventName, map)
    }
}

data class UmengTestInfo(
    var device_id: String = "",
    var mac: String = "",
)