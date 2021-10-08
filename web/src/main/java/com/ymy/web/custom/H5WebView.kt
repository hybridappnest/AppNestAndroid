package com.ymy.web.custom

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.alibaba.fastjson.JSONObject
import com.just.agentweb.AgentWeb
import com.just.agentweb.AgentWebConfig
import com.just.agentweb.WebViewClient
import com.orhanobut.logger.Logger
import com.ymy.core.BuildConfig
import com.ymy.core.base.getColorCompat
import com.ymy.core.glide.ImageLoader
import com.ymy.core.lifecycle.KtxManager
import com.ymy.core.ok3.DBXRetrofitClient.appContext
import com.ymy.core.ok3.GsonUtils
import com.ymy.web.R
import com.ymy.web.WebUrlConstant
import com.ymy.web.beans.WebEnvs
import com.ymy.web.custom.ui.WebViewActivity
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.URL

/**
 * Created on 1/29/21 08:13.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class H5WebView(
    private val context: Context,
    private val needBackgrounColor: Boolean = false,
    private val mWebViewTitleBarController: WebViewTitleBarController? = null,
    private var mCustomEventCallBack: CustomEventCallBack? = null,
) {

    interface CustomEventCallBack {
        fun call(eventName: String, params: String)
    }

    companion object {
        const val TAG = "H5WebView"

        /**
         * 被标记的目标页面，String为Popto的tag,在执行该逻辑时会使用key获取到目标activity
         */
        val popToActivityMap = mutableMapOf<String, Activity>()

        val noParamsTList = listOf<String>(WebUrlConstant.tabLearnTikTokUrl, WebUrlConstant.tabWorkBenchUrl)
    }

    private var currentUrl = ""

    init {
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(object : LifecycleObserver {

                @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                fun onPause() {
                    if (::mWebView.isInitialized) {
                        mWebView.webLifeCycle.onPause()
                    }
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                fun onResume() {
                    if (::mWebView.isInitialized) {
                        mWebView.webLifeCycle.onResume()
                        callResumeJsFunction()
//                        if (needRefreshOnResume) {
//                            refreshUrl()
//                        }
                    }
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    if (popToActivityMap.containsValue(context)) {
                        var removeTag = ""
                        popToActivityMap.forEach {
                            if (it.value == context) {
                                removeTag = it.key
                            }
                        }
                        popToActivityMap.remove(removeTag)
                    }
                    if (::mWebView.isInitialized) {
                        mCustomEventCallBack = null
                        mWebView.webLifeCycle.onDestroy()
                    }
                }
            })
        }
    }

    var JSBridgejs = ""
    var errorWeb = ""
    private lateinit var mWebView: AgentWeb

    private fun loadNativeJs() {
        var content = ""
        try {
            val instream: InputStream = context.assets.open("JSBridge.js")
            instream.buffered().reader().use { reader ->
                content = reader.readText()
            }
        } catch (e: FileNotFoundException) {
            Logger.e(TAG, "JSBridge", "The File doesn't not exist.")
        } catch (e: IOException) {
            Logger.e(TAG, "JSBridge", "", e.message)
        }
        JSBridgejs = content
    }

    private fun loadErrorWeb() {
        var content = ""
        try {
            val instream: InputStream = context.assets.open("error_dbx.html")
            instream.buffered().reader().use { reader ->
                content = reader.readText()
            }
        } catch (e: FileNotFoundException) {
            Logger.e(TAG, "errorWeb", "The File doesn't not exist.")
        } catch (e: IOException) {
            Logger.e(TAG, "errorWeb", "", e.message)
        }
        errorWeb = content
    }

    var startUpParams = ""

    fun initWebView(container: FrameLayout, url: String? = null, pushData: String = ""): AgentWeb {
        startUpParams = pushData
        loadNativeJs()
        loadErrorWeb()
        mWebView = createWebView(container)
        url?.run {
            loadUrl(this)
        }
        if (needBackgrounColor) {
            mWebView.webCreator.webView.setBackgroundColor(appContext.getColorCompat(R.color.blue0B297E))
        }
        return mWebView
    }


    fun loadUrl(url: String) {
        currentUrl = url
        var addUrl = url
        if (url.startsWith("http")) {
            addUrl = WebUrlConstant.addUrlParams(url,addTimeParams = !noParamsTList.contains(url))
            checkSyncCookie(addUrl)
        }
        mWebView.urlLoader.loadUrl(addUrl)
    }

    private fun checkSyncCookie(addUrl: String) {
        try {
            val url = URL(addUrl)
            val host = url.host
//            if (WebUrlConstant.webBaseURLList.contains(host)) {
            val webEnvs = WebEnvs()
            AgentWebConfig.syncCookie(host, "baseUrl=${webEnvs.baseUrl}")
            AgentWebConfig.syncCookie(host, "quick_url=${webEnvs.quickUrl}")
            AgentWebConfig.syncCookie(host, "h5_url=${webEnvs.h5Url}")
            AgentWebConfig.syncCookie(host, "javaUrl=${webEnvs.javaUrl}")
            AgentWebConfig.syncCookie(host, "companyAlias=${webEnvs.companyAlias}")
            AgentWebConfig.syncCookie(host, "companyId=${webEnvs.companyId}")
            AgentWebConfig.syncCookie(host, "company_Id=${webEnvs.company_Id}")
            AgentWebConfig.syncCookie(host, "companyName=${webEnvs.companyName}")
            AgentWebConfig.syncCookie(host, "mapid=${webEnvs.mapid}")
            AgentWebConfig.syncCookie(host, "token=${webEnvs.token}")
            AgentWebConfig.syncCookie(host, "userID=${webEnvs.userID}")
            AgentWebConfig.syncCookie(host, "platform=${webEnvs.platform}")
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshUrl() {
        checkSyncCookie(currentUrl)
        mWebView.urlLoader.reload()
    }

    private fun createWebView(rootView: FrameLayout): AgentWeb {
        AgentWebConfig.DEBUG = BuildConfig.DEBUG
        if (BuildConfig.DEBUG) {
            AgentWebConfig.debug()
        }
        val mAgentBuilder = when (context) {
            is Activity -> {
                AgentWeb.with(context)
            }
            is Fragment -> {
                AgentWeb.with(context)
            }
            else -> {
                throw IllegalArgumentException("context${context} 用于创建H5WebView的Context对象不是Activity或Fragment")
            }
        }
        return mAgentBuilder!!
            .setAgentWebParent(
                rootView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            .useDefaultIndicator(context.getColorCompat(R.color.blue32b8ec), 1)
//            .setWebChromeClient(mWebChromeClient)
            .setWebViewClient(mWebViewClient)
            .addJavascriptInterface(jsBridgeName, JSBridge(context, mJsCallBack))
            .createAgentWeb()
            .ready()
            .get()
    }

    private fun callJsFunction(funName: String, params: String) {
        Logger.d("callJsFunction funName:$funName params:$params")
        mWebView.jsAccessEntrace.quickCallJs("JSBridge.trigger", funName, params)
    }

    private fun callLocalJsFunction(funName: String, params: String) {
        Logger.d("callLocalJsFunction funName:$funName params:$params")
        mWebView.jsAccessEntrace.quickCallJs(funName, params)
    }

    fun callJsFunctionByData(funName: String, params: Any, mActionAnnounce: Int = 0) {
        val result = JSONObject()
        result["announce"] = mActionAnnounce
        result["data"] = params
        callJsFunction(funName, GsonUtils.mGson.toJson(result))
    }

    var mInjection = false
    var hasStart = false
    private val mWebViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            url?.let {
                webError = it.startsWith("data:text/html")
            }
            Logger.v("onPageStarted $url")
            if (mInjection) {
                mInjection = false
            }
            onJsLocal()
            hasStart = true
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
            if (mInjection) {
                return
            }
            if (hasStart) {
                url?.run { // 如果是注入到本地html中去掉这个判断
                    // 调用注入方法
//                    onJsLocal()
                    mInjection = true
                }
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Logger.d("onReceivedError $error")
            mWebView.webLifeCycle.onResume()
            if (!BuildConfig.DEBUG) {
                showErrorPage(error)
            }
//            onJsLocal()
//            super.onReceivedError(view, request, error)
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            mWebView.webLifeCycle.onResume()
//            onJsLocal()
            Logger.d("onReceivedHttpError $errorResponse")
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.run {
                val urlStr = url.toString()
                if (!urlStr.startsWith("http") or !urlStr.startsWith("file")) {
                    mWebView.back()
                    return false
                } else {
                    currentUrl = urlStr
                }
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageFinished(view: WebView?, url: String) {
            if (url.startsWith("http") or url.startsWith("file")) {
                mWebViewTitleBarController?.getCloseButton()?.visibility =
                    if (mWebView.webCreator.webView.canGoBack()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
            onJsLocal()
            super.onPageFinished(view, url)
        }
    }

    var webError = false

    private fun showErrorPage(error: WebResourceError?) {
        errorWeb.replace("&&&&&", "页面出现异常，请您稍后重试")
        mWebView.urlLoader.loadData(errorWeb, "text/html; charset=UTF-8", null)
    }

    fun onJsLocal() {
        Logger.d("onJsLocal 加载jsBridge")
        mWebView.jsAccessEntrace.callJs(
            JSBridgejs
        )
    }

    private val mJsCallBack: JSCallBack = object : JSCallBack {
        override fun sendResult(action: String, data: Any, mActionAnnounce: Int) {
            callJsFunctionByData(action, data,mActionAnnounce)
        }

        override fun setTitle(title: String) {
            mWebViewTitleBarController?.getTitle()?.text = title
        }

        override fun setOptionMenu(menuText: String?, textColor: Int?, icon: String?) {
            if (icon != null) {
                if (icon.isNotEmpty()) {
                    mWebViewTitleBarController?.getRightMenuImage()?.run {
                        this.visibility = View.VISIBLE
                        ImageLoader.loadOriginalImage(icon, this, R.drawable.more)
                        this.setOnClickListener {
                            callJsFunction(JSWebViewFunction.FUNC_OPTIONMENU, "")
                        }
                    }
                }
            }
            if (menuText != null) {
                if (menuText.isNotEmpty()) {
                    mWebViewTitleBarController?.getRightMenuText()?.run {
                        this.visibility = View.VISIBLE
                        this.text = menuText
                        this.setOnClickListener {
                            callJsFunction(JSWebViewFunction.FUNC_OPTIONMENU, "")
                        }
                    }
                }
            }
        }

        override fun popWindow() {
            if (context is Activity)
                context.finish()
            if (context is Fragment) {
                context.requireActivity().finish()
            }
        }

        override fun pushWindow(url: String, data: String) {
            invokeNewWebViewActivity(url, data)
        }

        override fun init() {
            callInitJsFunction()
        }

        override fun setPopToTag(tag: String) {
            if (context is Activity) {
                if (!popToActivityMap.containsValue(context)) {
                    popToActivityMap[tag] = context
                }
            }
            if (context is Fragment) {
                if (!popToActivityMap.containsValue(context.requireActivity())) {
                    popToActivityMap[tag] = context.requireActivity()
                }
            }
        }

        override fun findAndGoPopToActivity(tag: String) {
            val activity = popToActivityMap[tag]
            if (activity != null) {
                KtxManager.finishActivityTopOfTagActivity(activity)
            }
        }

        override fun setNeedRefreshOnResume() {
            needRefreshOnResume = true
        }

        override fun customEventCallBack(eventName: String, params: String) {
            mCustomEventCallBack?.run {
                call(eventName,params)
            }
        }
    }

    var needRefreshOnResume = false

    private fun callInitJsFunction() {
        val result = JSONObject()
        result["data"] = startUpParams
        callLocalJsFunction(JSWebViewFunction.FUNC_SETSTARTUPPARAMS, result.toJSONString())
        callLocalJsFunction(JSWebViewFunction.FUNC_SETENVS, GsonUtils.mGson.toJson(WebEnvs()))
    }

    fun callResumeJsFunction() {
        val data = if (WebViewDataCache.cachePopData != null) {
            WebViewDataCache.cachePopData
        } else {
            ""
        }
        callJsFunction(
            JSWebViewFunction.FUNC_RESUME, data!!
        )
        callLocalJsFunction(
            JSNotificationAction.jsNeedReloadData,
            ""
        )
    }

    private fun invokeNewWebViewActivity(url: String, pushData: String) {
        WebViewActivity.invoke(context, url, pushData = pushData)
    }
}

interface WebViewTitleBarController {
    fun getCloseButton(): View?
    fun getRightMenuText(): TextView?
    fun getTitle(): TextView?
    fun getRightMenuImage(): ImageView?
    fun onProgressChanged(newProgress: Int)
}