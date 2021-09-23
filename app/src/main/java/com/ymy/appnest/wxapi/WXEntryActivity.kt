package com.ymy.appnest.wxapi

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.jeremyliao.liveeventbus.LiveEventBus
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.ymy.appnest.wx.WXCons
import com.ymy.appnest.wx.WXLiveData
import com.ymy.appnest.wx.WXLoginManager
import com.ymy.core.base.RootActivity

/**
 * Created on 3/8/21 17:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class WXEntryActivity : RootActivity(), IWXAPIEventHandler {

    private var mIWXAPI: IWXAPI? = null
    private val mProgressDialog by lazy {
        ProgressDialog(this).run {
            setProgressStyle(ProgressDialog.STYLE_SPINNER) //转盘
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setTitle("提示")
            setMessage("登录中，请稍后")
            this
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setCutoutMode()
        }
        mIWXAPI = WXAPIFactory.createWXAPI(this, WXCons.WX_APP_ID, false).apply {
            handleIntent(intent!!, this@WXEntryActivity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setCutoutMode() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val lp = window.attributes
        lp.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = lp
    }


    override fun onReq(req: BaseReq?) {
    }

    override fun onResp(resp: BaseResp?) {
        when (resp!!.errCode) {
            BaseResp.ErrCode.ERR_OK -> { //获取 access_token
                if (resp is SendAuth.Resp) {
                    if (resp.state == WXLoginManager.wx_login_status) {
                        LiveEventBus.get(WXLiveData::class.java)
                            .post(WXLiveData(WXLiveData.status_success, resp.code))
                        WXLoginManager.wx_login_status = ""
                        finish()
                        mProgressDialog.dismiss()
                    }
                }
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                LiveEventBus.get(WXLiveData::class.java)
                    .post(WXLiveData(WXLiveData.status_fail))
                finish() //用户拒绝授权
                mProgressDialog.dismiss()
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                LiveEventBus.get(WXLiveData::class.java)
                    .post(WXLiveData(WXLiveData.status_cancel))
                finish()  //用户取消
                mProgressDialog.dismiss()
            }
        }
    }
}