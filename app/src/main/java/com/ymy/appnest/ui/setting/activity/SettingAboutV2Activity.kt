package com.ymy.appnest.ui.setting.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ymy.core.BuildConfig
import com.ymy.core.base.BaseActivity
import com.ymy.appnest.R
import com.ymy.appnest.databinding.ActivitySettingAboutV2Binding
import com.ymy.web.WebUrlConstant
import com.ymy.web.custom.ui.WebViewActivity


/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class SettingAboutV2Activity : BaseActivity(true) {
    companion object {
        fun invoke(context: Context) {
            val intent = Intent(context, SettingAboutV2Activity::class.java)
            context.startActivity(intent)
        }
    }


    val mBinding: ActivitySettingAboutV2Binding by lazy {
        ActivitySettingAboutV2Binding.inflate(layoutInflater)
    }

    override fun getBindingView(): View? = mBinding.root


    override fun initView() {
        mBinding.titleBar.btnBack.setOnClickListener {
            onBackPressed()
        }
        mBinding.titleBar.tvTitlebarTitle.text = "关于鼎保信"
        mBinding.aboutUserAgreements.setOnClickListener {
            openUrl(WebUrlConstant.userAgreementUrl, getString(R.string.str_user_agreements))
        }
        mBinding.aboutPrivacyPolicy.setOnClickListener {
            openUrl(WebUrlConstant.privacyPolicyUrl, getString(R.string.str_privacy_agreements))
        }
        mBinding.aboutDisclaimer.setOnClickListener {
            openUrl(WebUrlConstant.disclaimerUrl, getString(R.string.str_disclaimer))
        }
        mBinding.licenses.setOnClickListener {
            openUrl(WebUrlConstant.LicensesUrl, "版权声明")
        }
    }

    private fun openUrl(url: String, title: String = "", params: Bundle = Bundle()) {
        val addUrlParams = WebUrlConstant.addUrlParams(url)
        WebViewActivity.invoke(
            this,
            addUrlParams,
            title
        )
    }


    @SuppressLint("SetTextI18n")
    override fun initData() {
        mBinding.tvAppVersion.text = "版本 ${BuildConfig.VERSION_NAME}"
    }
}