package com.ymy.appnest.web

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.jeremyliao.liveeventbus.LiveEventBus
import com.mobile.auth.gatewayauth.AuthRegisterXmlConfig
import com.mobile.auth.gatewayauth.AuthUIConfig
import com.mobile.auth.gatewayauth.PhoneNumberAuthHelper
import com.mobile.auth.gatewayauth.TokenResultListener
import com.mobile.auth.gatewayauth.model.TokenRet
import com.mobile.auth.gatewayauth.ui.AbstractPnsViewDelegate
import com.orhanobut.logger.Logger
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.ymy.appnest.BuildConfig
import com.ymy.appnest.R
import com.ymy.appnest.appContext
import com.ymy.appnest.wx.WXCons
import com.ymy.appnest.wx.WXLiveData
import com.ymy.appnest.wx.WXLoginManager
import com.ymy.core.base.RootActivity
import com.ymy.core.base.getColorCompat
import com.ymy.core.exts.fromJson
import com.ymy.core.ok3.GsonUtils
import com.ymy.core.utils.DensityUtil
import com.ymy.core.utils.ScreenUtils
import com.ymy.web.custom.JSCallBack
import com.ymy.web.custom.JSNotificationAction.jsSocialLoginCB
import java.io.Serializable


/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:透明背景
 */
class JSLoginBridgeActivity : RootActivity() {

    companion object {
        const val ACTION = "ACTION"
        const val ACTION_ANNOUNCE = "ANNOUNCE"
        const val PARAMS = "PARAMS"
        private const val CALLBACK_SUFFIX = "Callback"

        var mJsCallBack: JSCallBack? = null

        fun invokeNew(
            context: Context,
            jsCallBack: JSCallBack,
            params: String = "",
            announce: Int = 0,
        ) {
            mJsCallBack = jsCallBack
            val bundle = Bundle()
            bundle.putString(PARAMS, params)
            bundle.putInt(ACTION_ANNOUNCE, announce)
            val intent = Intent(context, JSLoginBridgeActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentExtra()
    }

    private var mActionAnnounce = 0
    private var mParams = ""
    private var loginChannel: String = ""
    private fun getIntentExtra() {
        intent.extras?.run {
            mActionAnnounce = getInt(ACTION_ANNOUNCE, 0)
            mParams = getString(PARAMS, "")
            val mLoginAction: LoginAction = GsonUtils.mGson.fromJson(mParams)
            loginChannel = mLoginAction.channel
            when (loginChannel) {
                LoginAction.loginChannel_Phone -> {
                    initLoginByPhoneNumberAuth()
                    mAlicomAuthHelper.getLoginToken(this@JSLoginBridgeActivity, 10 * 60 * 1000)
                }
                LoginAction.loginChannel_WX -> {
                    registerToWX()
                    checkWX()
                }
                else -> {
                    toast("传入登录方式暂不支持")
                    finish()
                }
            }
        }
        if (loginChannel.isEmpty()) {
            toast("传入参数异常")
            finish()
        }
    }

    private lateinit var mIWXAPI: IWXAPI

    private fun registerToWX() {
        mIWXAPI = WXAPIFactory.createWXAPI(appContext, WXCons.WX_APP_ID, false).apply {
            registerApp(WXCons.WX_APP_ID)
        }
        LiveEventBus.get(WXLiveData::class.java).observe(this) {
            it?.run {
                when (status) {
                    WXLiveData.status_cancel -> {
                        callWebFunction(
                            jsSocialLoginCB,
                            GsonUtils.mGson.toJson(
                                LoginActionCallback(
                                    channel = loginChannel,
                                    "",
                                    LoginActionCallback.status_cancel
                                )
                            )
                        )
                    }
                    WXLiveData.status_fail -> {
                        callWebFunction(
                            jsSocialLoginCB,
                            GsonUtils.mGson.toJson(
                                LoginActionCallback(
                                    channel = loginChannel,
                                    "",
                                    LoginActionCallback.status_fail
                                )
                            )
                        )
                    }
                    WXLiveData.status_success -> {
                        callWebFunction(
                            jsSocialLoginCB,
                            GsonUtils.mGson.toJson(
                                LoginActionCallback(
                                    channel = loginChannel,
                                    code,
                                    LoginActionCallback.status_success
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private fun checkWX() {
        if (mIWXAPI.isWXAppInstalled) {
            getWxLoginToken()
        } else {
            toast("您的设备未安装微信客户端！", false)
        }
    }

    private fun getWxLoginToken() {
        mIWXAPI.sendReq(SendAuth.Req().apply {
            scope = "snsapi_userinfo"
            state = "wechat_sdk_dbx_${System.currentTimeMillis()}"
            WXLoginManager.wx_login_status = state
        })
    }

    var cantLoginByPhoneAuth = false
    private val mTokenResultListener = object : TokenResultListener {
        override fun onTokenSuccess(ret: String?) {
            cantLoginByPhoneAuth = true
            if (ret != null) {
                Logger.e(ret)
            }
            var tokenRet: TokenRet? = null
            try {
                tokenRet = JSON.parseObject<TokenRet>(ret, TokenRet::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Logger.e("onTokenSuccess tokenRet.code:${tokenRet?.code} ${tokenRet?.token} ")
            tokenRet?.run {
                when (code) {
                    "600000" -> {
                        val token = token
                        callWebFunction(
                            jsSocialLoginCB,
                            GsonUtils.mGson.toJson(
                                LoginActionCallback(
                                    channel = loginChannel,
                                    token,
                                    LoginActionCallback.status_success
                                )
                            )
                        )
                        Logger.e("token : $token")
                        mAlicomAuthHelper.quitLoginPage()
                    }
                    else -> {
                        callWebFunction(
                            jsSocialLoginCB,
                            GsonUtils.mGson.toJson(
                                LoginActionCallback(
                                    channel = loginChannel,
                                    "",
                                    LoginActionCallback.status_fail
                                )
                            )
                        )
                    }
                }
            }
        }

        override fun onTokenFailed(ret: String?) {
            cantLoginByPhoneAuth = false
            if (ret != null) {
                Logger.e(ret)
            }
            var tokenRet: TokenRet? = null
            try {
                tokenRet = JSON.parseObject<TokenRet>(ret, TokenRet::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Logger.e("onTokenFailed tokenRet.code:${tokenRet?.code}")
            tokenRet?.run {
                when (code) {
                    "80800" -> {
                        toast("当前网络环境为wifi环境，请您切换为移动网络", false)
                    }
                    "600008" -> {
                        toast("蜂窝网络未开启，请您打开蜂窝网络", false)
                    }
                    "600007" -> {
                        toast("未检测到sim卡，请您切换账号登录", false)
                    }
                    else -> {
                        toast(msg, false)
                    }
                }
                mAlicomAuthHelper.quitLoginPage()
            }
            callWebFunction(
                jsSocialLoginCB,
                GsonUtils.mGson.toJson(
                    LoginActionCallback(
                        channel = loginChannel,
                        "",
                        LoginActionCallback.status_fail
                    )
                )
            )
        }
    }


    lateinit var mAlicomAuthHelper: PhoneNumberAuthHelper
    var mScreenWidthDp: Int = 0
    var mScreenHeightDp: Int = 0
    var token: String = ""
    private val authSDKID = BuildConfig.authSDKID

    private fun initLoginByPhoneNumberAuth() {
        mAlicomAuthHelper = PhoneNumberAuthHelper.getInstance(
            this,
            mTokenResultListener
        ).apply {
            setAuthSDKInfo(authSDKID)
        }
        configLoginTokenPortDialog()
    }

    private fun initDynamicView(): TextView {
        return TextView(applicationContext).apply {
            val mLayoutParams2 =
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    DensityUtil.dip2pxX(50f)
                )
            mLayoutParams2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
            mLayoutParams2.setMargins(0, DensityUtil.dip2pxX(450f), 0, 0)
            text = "-----  自定义view  -----"
            setTextColor(-0x666667)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.0f)
            layoutParams = mLayoutParams2
        }
    }

    private fun updateScreenSize() {
        mScreenHeightDp =
            DensityUtil.px2dip((ScreenUtils.getScreenHeight(this)).toFloat())
        mScreenWidthDp =
            DensityUtil.px2dip((ScreenUtils.getScreenWidth(this)).toFloat())
    }

    private fun configLoginTokenPortDialog() {
        Logger.e("configLoginTokenPortDialog2")
        initDynamicView()
        mAlicomAuthHelper.removeAuthRegisterXmlConfig()
        mAlicomAuthHelper.removeAuthRegisterViewConfig()
        var authPageOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        if (Build.VERSION.SDK_INT == 26) {
            authPageOrientation = ActivityInfo.SCREEN_ORIENTATION_BEHIND
        }
        updateScreenSize()
        val dialogWidth = (mScreenWidthDp * 0.8f).toInt()
        val dialogHeight = (mScreenHeightDp * 0.65f).toInt()
        mAlicomAuthHelper.addAuthRegisterXmlConfig(
            AuthRegisterXmlConfig.Builder()
                .setLayout(
                    R.layout.custom_port_dialog_action_bar,
                    object : AbstractPnsViewDelegate() {
                        override fun onViewCreated(view: View) {
                            findViewById(R.id.btn_close).setOnClickListener { mAlicomAuthHelper.quitLoginPage() }
                        }
                    })
                .build()
        )
        val logBtnOffset = dialogHeight / 2
        mAlicomAuthHelper.setAuthUIConfig(
            AuthUIConfig.Builder()
                .setAppPrivacyColor(Color.GRAY, Color.parseColor("#002E00"))
                .setPrivacyState(false)
                .setCheckboxHidden(true)
                .setNavHidden(true)
                .setNavColor(getColorCompat(R.color.blue0B297E))
                .setStatusBarColor(getColorCompat(R.color.blue0B297E))
                .setWebNavColor(getColorCompat(R.color.blue0B297E))
                .setAuthPageActIn("bottom_enter", "bottom_exit")
                .setAuthPageActOut("bottom_enter", "bottom_exit")
                .setVendorPrivacyPrefix("《")
                .setVendorPrivacySuffix("》")
                .setLogoImgPath("icon_setting_about_logo")
                .setLogoWidth(65)
                .setLogoHeight(84)
                .setLogoOffsetY(20)
                .setPrivacyState(true)
                .setLogBtnWidth(dialogWidth - 30)
                .setLogBtnMarginLeftAndRight(15)
                .setLogBtnBackgroundPath("selector_login_btn_bg")
                .setLogBtnHeight(54)
                .setNavReturnHidden(true)
                .setLogBtnOffsetY(logBtnOffset)
                .setLogBtnText("本机号码一键登录")
                .setSloganText("为了您的账号安全，请先绑定手机号")
                .setSloganOffsetY(logBtnOffset - 100)
                .setSloganTextSize(11)
                .setNumFieldOffsetY(logBtnOffset - 50)
                .setSwitchOffsetY(logBtnOffset + 74)
                .setSwitchAccTextSize(11)
                .setPageBackgroundPath("shape_dialog_white_bg")
                .setCheckboxHidden(false)
                .setNumberSize(17)
                .setLogBtnTextSize(16)
                .setDialogWidth(dialogWidth)
                .setDialogHeight(dialogHeight)
                .setDialogBottom(false)
                .setScreenOrientation(authPageOrientation)
                .create()
        )
    }

    private fun callWebFunction(functionName: String, params: Any) {
        mJsCallBack?.run {
            sendResult(functionName, params, mActionAnnounce)
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        finish()
    }
}

data class LoginAction(
    val channel: String = ""
) : Serializable {
    companion object {
        const val loginChannel_Phone = "phone"
        const val loginChannel_WX = "wechat"
    }
}

data class LoginActionCallback(
    val channel: String = "",
    val token: String = "",
    val status: Int = status_fail
) : Serializable {
    companion object {
        const val status_fail = 0
        const val status_cancel = 1
        const val status_success = 2
    }
}