package com.ymy.appnest.web.custom

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
import com.ymy.appnest.R
import com.ymy.appnest.qrcode.HWQRCodeScannerActivity
import com.ymy.appnest.ui.MainActivity
import com.ymy.appnest.ui.gallery.BigImageDisplayActivity
import com.ymy.appnest.ui.gallery.MaxViewV2Activity
import com.ymy.appnest.ui.gallery.adapter.IGallerySourceModel
import com.ymy.appnest.web.custom.*
import com.ymy.appnest.web.custom.ui.FileDisplayActivity
import com.ymy.appnest.web.custom.ui.JSBridgeActivity
import com.ymy.appnest.web.custom.ui.JSLoginBridgeActivity
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

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

object WebViewDataCache {
    var cachePopData: String? = null
}

open class JSBridge(val context: Context?, private val jsCallBack: JSCallBack) {

    companion object {
        const val TAG = "JSBridge"
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
                                                goToAppHomePage()
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
                                    context.let {
                                        BigImageDisplayActivity.actionStart(
                                            it as Context,
                                            urlNoParam,
                                            ""
                                        )
                                    }
                                    return@launch
                                }
                                UrlUtils.FILE_TYPE_VIDEO -> {
                                    context.let {
                                        jumpToVideoPlayer(it, urlNoParam)
                                    }
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
                                name,
                                paramsData,
                                context,
                                jsCallBack,
                                announce
                            )
                        }
                    }
                }
            }
        }
    }

    private fun jumpToVideoPlayer(context: Context, urlNoParam: String) {
        val also = MaxBean().Data().also {
            it.coverUrl = ""
            it.type = IGallerySourceModel.video.toString()
            it.url = urlNoParam
        }
        MaxViewV2Activity.invoke(context, MaxBean().apply {
            index = 0
            data = mutableListOf(also)
        })
    }

    private fun goToAppHomePage() {
        context?.run {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

object JSActionManager {
    const val TAG = "JSActionManager"

    /**
     * 弱引用持有H5容器的上线文对象
     */
    private var locationContext: JSCallBack? = null

    fun sendActionEvent(
        eventName: String = "",
        param: JSONObject? = null,
        context: Context?,
        jsCallBack: JSCallBack,
        announce: Int,
    ) {
        if (param != null) {
            var used = false
            try {
                used = when (eventName) {
                    JSNotificationAction.jsshowGallery + DEFAULT_JS_SUFFIX -> {
                        val json = param.getString("data")
                        gotoMaxView(json)
                        true
                    }
                    JSNotificationAction.jsShowToast + DEFAULT_JS_SUFFIX -> {
                        val data: String = param.getString("data")
                        showToast(context, data)
                    }
                    JSNotificationAction.jsSignature + DEFAULT_JS_SUFFIX -> {
                        val data = param.getJSONObject("data")
                        val questionId = data.getString("id")
                        val status = data.getInteger("status")
                        val groupId = data.getString("groupId")
                        SJPXTestHelper.map[groupId] =
                            SJPXTest(GsonUtils.mGson.toJson(data), status, questionId)
                        true
                    }
                    JSNotificationAction.jscheckPermission + DEFAULT_JS_SUFFIX -> {
                        val name = param.getString("name")
                        checkDBXPermission(jsCallBack, name)
                    }
                    JSNotificationAction.jsAutolockScreen + DEFAULT_JS_SUFFIX -> {
//                        TODO: 1/26/21 逻辑实现
                        context.run {
//                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        }
                        true
                    }
                    JSNotificationAction.jspopTo + DEFAULT_JS_SUFFIX -> {
                        context?.run {
                            val tag = param.getString("data")
                            jsCallBack.setPopToTag(tag)
                        }
                        true
                    }
                    JSNotificationAction.jsneedRefreshOnResume + DEFAULT_JS_SUFFIX -> {
                        jsCallBack.setNeedRefreshOnResume()
                        true
                    }
                    JSLoginBridgeActivity.jsSocialLogin + DEFAULT_JS_SUFFIX -> {
                        if (context != null) {
                            socialLogin(context, jsCallBack, param, announce)
                        }
                        true
                    }
                    JSNotificationAction.jsScan + DEFAULT_JS_SUFFIX -> {
                        if (context != null) {
                            HWQRCodeScannerActivity.invoke(context,jsCallBack,announce)
                        }
                        true
                    }
                    else -> {
                        false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
            }
            if (!used) {
                if (actionsNeedBridgeToDo.contains(eventName)) {
                    context?.run {
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
        } else {
            var used = when (eventName) {
                JSNotificationAction.jsFetchUserInfo + DEFAULT_JS_SUFFIX -> {
                    setUserInfo(jsCallBack)
                }
                else -> false
            }
            if (!used) {
                if (actionsNeedBridgeToDo.contains(eventName)) {
                    context?.let {
                        startBridgeActivity(
                            it,
                            eventName,
                            "",
                            jsCallBack = jsCallBack,
                            announce = announce
                        )
                    }
                } else {
                    jsCallBack.customEventCallBack(eventName, GsonUtils.mGson.toJson(param))
                }
            }
        }
    }

    /**
     * 一键登录
     * @param context Context
     * @param jsCallBack JSCallBack
     * @param param JSONObject
     * @param announce Int
     */
    private fun socialLogin(
        context: Context, jsCallBack: JSCallBack, param: JSONObject, announce: Int
    ) {
        Logger.d(TAG, "start JSLoginBridgeActivity eventName:socialLogin data:$param")
        context.let {
            JSLoginBridgeActivity.invokeNew(
                it,
                jsCallBack,
                param.toString(),
                announce,
            )
        }
    }

    private val actionsNeedBridgeToDo = listOf(
        JSBridgeActivity.jschoseImgWithUpload + DEFAULT_JS_SUFFIX,
        JSBridgeActivity.jsUpload + DEFAULT_JS_SUFFIX,
        JSBridgeActivity.jsChoseFile + DEFAULT_JS_SUFFIX,
    )

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


    /**
     * 写入用户信息
     * @param jsCallBack JSCallBack
     */
    private fun setUserInfo(jsCallBack: JSCallBack): Boolean {
        val mapOf = mapOf(
            "userInfo" to YmyUserManager.user,
            "token" to YmyUserManager.user.token
        )
        val parseObject = JSON.parseObject(JSON.toJSONString(mapOf))
        jsCallBack.sendResult(JSNotificationAction.jsFetchUserInfo, parseObject, 0)
        return true
    }

    /**
     * 检查用户权限
     * @param jsCallBack JSCallBack
     * @param str String
     * @return Boolean
     */
    private fun checkDBXPermission(jsCallBack: JSCallBack, str: String): Boolean {
        if (StringUtils.isNotEmpty(str)) {
            val split = str.split(",")
            val mapOf = mutableMapOf<String, Boolean>()
            if (split.isNotEmpty()) {
                split.forEach {
                    mapOf[it] = DBXPermission.checkHasDBXPermission(it.toLong())
                }
            }
            val parseObject = JSON.parseObject(JSON.toJSONString(mapOf))
            jsCallBack.sendResult(
                JSNotificationAction.jscheckPermission,
                parseObject,
                0
            )
        }
        return true
    }

    private fun gotoMaxView(json: String?) {
        try {
            val gson = Gson()
            val maxBean = gson.fromJson(json, MaxBean::class.java)
            MaxViewV2Activity.invoke(currentActivity!!, maxBean)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}


data class JSParams(
    //弹出toast的内容
    val content: String = "",
    //成功还是失败 0失败，1成功
    val success: Int = 0,
) : Serializable

class JSLiveModel(
    val eventName: String?,
    val param: String?,
) : LiveEvent

data class LocationJSInfo(
    val x: Double,
    val y: Double,
    val floorId: Int,
    val distance: Double,
    val direction: Float,
    val type: String,
    val pathConstraint: Boolean = true,
)

object SJPXTestHelper {
    val map = mutableMapOf<String, SJPXTest>()

    fun getSJPXTest(groupId: String): SJPXTest {
        return map[groupId] ?: SJPXTest()
    }
}

data class SJPXTest(
    var data: String = "",
    var status: Int = 0,
    var id: String = "",
) : Serializable

data class ToWorkOrderData(
    var eventId: Int = 0,
    var title: String = "",
    var eventList: MutableList<String> = mutableListOf(),
    var toWorkOrderUrl: String = "",
) : Serializable