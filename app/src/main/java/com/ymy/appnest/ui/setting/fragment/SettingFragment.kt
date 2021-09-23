package com.ymy.appnest.ui.setting.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.navigation.Navigation
import com.ymy.core.base.BaseFragment
import com.ymy.core.glide.ImageLoader
import com.ymy.core.utils.BrandUtil
import com.ymy.appnest.BuildConfig
import com.ymy.appnest.R
import com.ymy.appnest.databinding.FragmentSettingBinding
import com.ymy.appnest.web.WebUrlConstant
import com.ymy.appnest.web.custom.H5WebView
import com.ymy.appnest.web.custom.ui.WebViewFragmentArgs
import java.lang.ref.WeakReference

/**
 * Created on 2020/8/8 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class SettingFragment : BaseFragment(true) {

    val mBinding: FragmentSettingBinding by lazy {
        FragmentSettingBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View = mBinding.root

    override fun initView() {
        initTitleBar()
        initFunctionView()
        mBinding.settingSafeCenter.visibility = View.VISIBLE
        mBinding.settingSafeCenter.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingFragment_to_settingSafeCenterFragment)
        }
        mBinding.settingCleanCache.visibility = View.VISIBLE
        mBinding.settingCleanCache.setOnClickListener {
            cleanCache()
        }
        mBinding.settingPhoneNoticeSetting.visibility = View.VISIBLE
        mBinding.settingPhoneNoticeSetting.setOnClickListener {
            jumpToPhoneNoticeSettingPage(it)
        }
        mBinding.settingLogout.setOnClickListener {
            cleanCache(false)
            // TODO: 2021/9/10 登出逻辑
//            LoginAndLogoutManager.doLogout()
        }
    }

    private fun jumpToPhoneNoticeSettingPage(view: View) {
        val url = when {
            BrandUtil.isBrandXiaoMi() -> {
                WebUrlConstant.phoneNoticexiaomi
            }
            BrandUtil.isSanSung() -> {
                WebUrlConstant.phoneNoticesansung
            }
            BrandUtil.isBrandMeizu() -> {
                WebUrlConstant.phoneNoticemeizu
            }
            BrandUtil.isBrandOppo() -> {
                WebUrlConstant.phoneNoticeoppe
            }
            BrandUtil.isBrandVivo() -> {
                WebUrlConstant.phoneNoticevivo
            }
            else -> {
                WebUrlConstant.phoneNoticehuawei
            }
        }
        val args = WebViewFragmentArgs
            .Builder()
            .apply {
                title = "手机消息提醒设置"
                this.url = url
            }
            .build()
            .toBundle()
        Navigation.findNavController(view).navigate(
            R.id.action_settingFragment_to_webViewFragment,
            args
        )
    }

    private fun initFunctionView() {

    }

    private fun cleanCache(showToast: Boolean = true) {
        context?.let { context ->
            //清理图片缓存
            ImageLoader.clearCache(
                WeakReference(context)
            )
            //清理webView缓存
            clearWebViewCache(context)
            if (showToast) {
                toast("已为您清空应用缓存")
            }
        }
    }

    private fun clearWebViewCache(context: Context) {
        val initWebView = H5WebView(context).initWebView(mBinding.webviewRoot)
        initWebView.clearWebCache()
    }

    private fun initTitleBar() {
        mBinding.titleBar.btnBack.setOnClickListener {
            activity?.finish()
        }
        mBinding.titleBar.tvTitlebarTitle.text = "设置中心"
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        mBinding.tvAppVersion.text = "版本 ${BuildConfig.VERSION_NAME}"
    }

}