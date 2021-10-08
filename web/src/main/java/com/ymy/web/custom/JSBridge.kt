package com.ymy.web.custom

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.webkit.JavascriptInterface
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.core.LiveEvent
import com.orhanobut.logger.Logger
import com.ymy.core.base.getColorCompat
import com.ymy.core.bean.MaxBean
import com.ymy.core.lifecycle.KtxManager.currentActivity
import com.ymy.core.ok3.GsonUtils
import com.ymy.core.permission.DBXPermissionUtils
import com.ymy.core.permission.requestPermission
import com.ymy.core.user.DBXPermission
import com.ymy.core.user.YmyUserManager
import com.ymy.core.utils.StringUtils
import com.ymy.core.utils.ToastUtils
import com.ymy.core.utils.UrlUtils
import com.ymy.web.R
import com.ymy.web.custom.ui.FileDisplayActivity
import com.ymy.web.custom.ui.JSBridgeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable
import kotlin.math.abs

/**
 * Created on 1/28/21 08:48.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
interface JSCallBack {
    fun sendResult(action: String, data: Any, mActionAnnounce: Int)
    fun setTitle(title: String)
    fun setOptionMenu(menuText: String?, textColor: Int?, icon: String?)
    fun popWindow()
    fun pushWindow(url: String, data: String)
    fun init()
    fun setPopToTag(tag: String)
    fun findAndGoPopToActivity(tag: String)
    fun setNeedRefreshOnResume()
    fun customEventCallBack(eventName: String, params: String)
}

interface JSMainActionHandler {
    fun goHomePage(context: Context)

    fun showBigImage(context: Context, ImageUrl: String)

    fun jumpToVideoPlayer(context: Context, str: String)
}

abstract class JSNotificationHandler {

    val notificationList: MutableList<String> = mutableListOf()

    abstract fun onNewNotification(
        context: Context,
        eventName: String = "",
        param: JSONObject,
        announce: Int,
        jsCallBack: JSCallBack,
    ): Boolean
}

object WebViewDataCache {
    var cachePopData: String? = null
}

open class JSBridge(val context: Context?, private val jsCallBack: JSCallBack) {

    companion object {
        const val TAG = "JSBridge"
        var jsMainActionHandler: JSMainActionHandler? = null
    }

    @JavascriptInterface
    fun postMessage(funcName: String, params: String) {
        Logger.d("JSBridge postMessage funcName:$funcName params:$params")
        if (context is LifecycleOwner) {
            context.lifecycleScope.launch(Dispatchers.Main) {
                var data: JSONObject? = null
                try {
                    data = JSONObject.parseObject(params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                when (funcName) {
                    js_fun_setTitle -> {
                        data?.run {
                            val title = getString("title")
                            jsCallBack.setTitle(title)
                        }
                    }
                    js_fun_setOptionMenu -> {
                        data?.run {
                            val title = getString("title")
                            val icon = getString("icon")
                            var parseColor = context.getColorCompat(R.color.white)
                            if (title.isNullOrEmpty()) {
                                val color = getString("color")
                                try {
                                    parseColor = Color.parseColor(color)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            jsCallBack.setOptionMenu(title, parseColor, icon)
                        }
                    }
                    js_fun_popWindow -> {
                        data?.run {
                            try {
                                if (this.containsKey("data")) {
                                    val jsonObject = this.getJSONObject("data")
                                    if (jsonObject.containsKey("toRoot")) {
                                        val toRoot = jsonObject.getBoolean("toRoot")
                                        toRoot?.let {
                                            if (it) {
                                                jsMainActionHandler?.goHomePage(context)
                                                return@launch
                                            }
                                        }
                                    }
                                    if (jsonObject.containsKey("needCheckPopTo")) {
                                        val needCheckPopToTag =
                                            jsonObject.getString("needCheckPopTo")
                                        needCheckPopToTag?.let {
                                            if (it.isNotEmpty()) {
                                                jsCallBack.findAndGoPopToActivity(it)
                                                return@launch
                                            }
                                        }
                                    }
                                    if (jsonObject.containsKey("func")) {
                                        when (jsonObject.getString("func")) {
                                            "pushWindow" -> {
                                                val pushWindowUrl =
                                                    jsonObject.getJSONObject("params")
                                                        .getString("url")
                                                if (pushWindowUrl != null && pushWindowUrl.isNotEmpty()) {
                                                    jsCallBack.pushWindow(pushWindowUrl, "")
                                                }
                                            }
                                        }
                                    }
                                    val cacheData = getString("data")
                                    WebViewDataCache.cachePopData = cacheData
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        jsCallBack.popWindow()
                    }
                    js_fun_pushWindow -> {
                        data?.run {
                            val url = getString("url")
                            val urlNoParam = if (url.contains("?")) {
                                url.split("?")[0]
                            } else {
                                ""
                            }
                            when (UrlUtils.checkUrlIsFile(urlNoParam)) {
                                UrlUtils.FILE_TYPE_OFFICE -> {
                                    context.let {
                                        requestPermission(
                                            context,
                                            "你好:\n" +
                                                    "      为了能够正常使用，我们需要使用储存权限，鉴于您禁用了该权限，请手动设置开启权限:\n" +
                                                    "1、【储存】\n",
                                            arrayOf(DBXPermissionUtils.WRITE_EXTERNAL_STORAGE),
                                            {
                                                FileDisplayActivity.actionStart(it, urlNoParam, "")
                                            },
                                            {})
                                    }
                                    return@launch
                                }
                                UrlUtils.FILE_TYPE_PDF -> {
                                    requestPermission(
                                        context,
                                        "你好:\n" +
                                                "      为了能够正常使用，我们需要使用储存权限，鉴于您禁用了该权限，请手动设置开启权限:\n" +
                                                "1、【储存】\n",
                                        arrayOf(DBXPermissionUtils.WRITE_EXTERNAL_STORAGE),
                                        {
                                            FileDisplayActivity.actionStart(context, urlNoParam, "")
                                        },
                                        {})
                                    return@launch
                                }
                                UrlUtils.FILE_TYPE_IMAGE -> {
                                    jsMainActionHandler?.showBigImage(context, urlNoParam)
                                    return@launch
                                }
                                UrlUtils.FILE_TYPE_VIDEO -> {
                                    jsMainActionHandler?.jumpToVideoPlayer(context, urlNoParam)
                                    return@launch
                                }
                            }
                            val paramsData = getJSONObject("param")
                            var cacheData = ""
                            try {
                                cacheData = paramsData.getString("data")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            jsCallBack.pushWindow(url, cacheData)
                        }
                    }
                    js_fun_init -> {
                        jsCallBack.init()
                    }
                    js_fun_postNotification -> {
                        data?.run {
                            val name = getString("name")
                            val announce = getIntValue("announce")
                            val paramsData = getJSONObject("data")
                            Logger.d(
                                "收到事件 postNotification name:$name data:${
                                    GsonUtils.mGson.toJson(
                                        paramsData
                                    )
                                }"
                            )
                            JSActionManager.sendActionEvent(
                                context,
                                name,
                                paramsData,
                                jsCallBack,
                                announce
                            )
                        }
                    }
                }
            }
        }
    }
}

object JSActionManager {
    const val TAG = "JSActionManager"

    private val actionsNeedBridgeToDo = listOf(
        JSBridgeActivity.jschoseImgWithUpload + DEFAULT_JS_SUFFIX,
        JSBridgeActivity.jsUpload + DEFAULT_JS_SUFFIX,
        JSBridgeActivity.jsChoseFile + DEFAULT_JS_SUFFIX,
    )

    var jsNotificationHandlers: MutableList<JSNotificationHandler> = mutableListOf()

    fun sendActionEvent(
        context: Context,
        eventName: String = "",
        param: JSONObject,
        jsCallBack: JSCallBack,
        announce: Int,
    ) {
        var used = false
        try {
            used = when (eventName) {
                JSNotificationAction.jsShowToast + DEFAULT_JS_SUFFIX -> {
                    val data: String = param.getString("data")
                    showToast(context, data)
                }
                JSNotificationAction.jspopTo + DEFAULT_JS_SUFFIX -> {
                    context.run {
                        val tag = param.getString("data")
                        jsCallBack.setPopToTag(tag)
                    }
                    true
                }
                JSNotificationAction.jsneedRefreshOnResume + DEFAULT_JS_SUFFIX -> {
                    jsCallBack.setNeedRefreshOnResume()
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
        if (!used) {
            if (actionsNeedBridgeToDo.contains(eventName)) {
                context.run {
                    startBridgeActivity(
                        this,
                        eventName,
                        param.toJSONString(),
                        jsCallBack,
                        announce = announce
                    )
                }
            } else {
                jsCallBack.customEventCallBack(eventName, GsonUtils.mGson.toJson(param))
            }
        }
        jsNotificationHandlers.forEach {
            if (it.notificationList.contains(eventName)) {
                it.onNewNotification(context, eventName, param, announce, jsCallBack)
            }
        }
    }

    private fun startBridgeActivity(
        context: Context,
        eventName: String,
        data: String,
        jsCallBack: JSCallBack,
        announce: Int,
    ) {
        Logger.d(TAG, "startBridgeActivity eventName:$eventName data:$data")
        context.let {
            JSBridgeActivity.invokeNew(
                it,
                jsCallBack,
                eventName,
                data,
                announce
            )
        }
    }

    /**
     * H5展示原生toast
     * @param param JSONObject?
     */
    private fun showToast(context: Context?, param: String?): Boolean {
        param?.run {
            val fromJson = Gson().fromJson(param, JSParams::class.java)
            ToastUtils.showImageToast(context, fromJson.content, fromJson.success == 1)
        }
        return true
    }
}


data class JSParams(
    //弹出toast的内容
    val content: String = "",
    //成功还是失败 0失败，1成功
    val success: Int = 0,
) : Serializable