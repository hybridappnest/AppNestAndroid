package com.ymy.im

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.tencent.qcloud.tim.uikit.TUIKit
import com.tencent.qcloud.tim.uikit.ThirdPushTokenMgr
import com.ymy.core.push.PushCenter
import com.ymy.im.helper.ConfigHelper
import com.ymy.im.signature.GenerateTestUserSig

/**
 * Created on 2021/9/28 16:38.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class IMManager : ContentProvider() {

    companion object {
        lateinit var app: Application
    }


    override fun onCreate(): Boolean {
        val application = context!!.applicationContext as Application
        install(application)
        return true
    }

    private fun install(application: Application) {
        app = application
        /**
         * TUIKit的初始化函数
         *
         * @param context  应用的上下文，一般为对应应用的ApplicationContext
         * @param sdkAppID 您在腾讯云注册应用时分配的sdkAppID
         * @param configs  TUIKit的相关配置项，一般使用默认即可，需特殊配置参考API文档
         */
        TUIKit.init(
            app, GenerateTestUserSig.SDKAPPID, ConfigHelper().getConfigs(app)
        )

        PushCenter.addObserver(object : PushCenter.PushTokenObserver {
            override fun pushToken(channel: String, token: String) {
                ThirdPushTokenMgr.getInstance().thirdPushToken = token
            }
        })
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? = null


    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null
}