package com.ymy.web.custom

/**
 * Created on 1/28/21 14:02.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
const val DEFAULT_JS_SUFFIX = "_Notification"
/**
 * 注册到页面中的方法
 */
val jsBridgeName = "ymyJSBridge"

/**
 * js调用本地的事件
 */
const val js_fun_setTitle = "setTitle"
const val js_fun_setOptionMenu = "setOptionMenu"
const val js_fun_postNotification = "postNotification"
const val js_fun_popWindow = "popWindow"
const val js_fun_pushWindow = "pushWindow"
const val js_fun_init = "init"

/**
 * 页面js方法
 */
object JSWebViewFunction {
    const val FUNC_RESUME = "resume"
    const val FUNC_INIT = "init"
    const val FUNC_SETSTARTUPPARAMS = "setStartupParams"
    const val FUNC_SETENVS = "setEnvs"
    const val FUNC_OPTIONMENU = "optionMenu"
}

/**
 * js调用通知的事件
 */
object JSNotificationAction {
    const val CALLBACK_SUFFIX = "Callback"

    /**
     * 展示大图
     */
    const val jsshowGallery = "showGallery"

    /**
     * 弹出toast
     */
    const val jsShowToast = "showToast"


    /**
     * 获取用户数据，收到和回写一致
     */
    const val jsFetchUserInfo = "fetchUserInfo"

    /**
     * 获取用户数据，收到和回写一致
     */
    const val jsSignature = "signature"

    /**
     * 获取用户权限
     */
    const val jscheckPermission = "checkPermission"

    /**
     * js操作页面自动锁屏
     */
    const val jsAutolockScreen = "autolockScreen"
    /**
     * js事件标记当前页面为返回时的目标页面（应用场景：当有一个页面需要打开多个webView后续页面，处理完成后回到目标页面时使用）
     */
    const val jspopTo = "popTo"

    /**
     * js标记当前页面需要在onResume是刷新
     */
    const val jsneedRefreshOnResume = "needRefreshOnResume"

    /**
     * 刷新页面数据用
     */
    const val jsNeedReloadData = "needReloadData"
    /**
     * 扫码
     */
    const val jsScan = "scan"

}
