package com.ymy.appnest.web.custom.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Keep
import com.ymy.core.base.BaseActivity
import com.ymy.appnest.R
import java.net.URLDecoder

/**
 * Created on 2020/9/2 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:AgentWeb原生的webView支持原生的js互调方式
 */

class WebViewActivity : BaseActivity() {
    companion object {

        @Keep
        fun invoke(
            context: Context,
            url: String,
            title: String = "",
            showTitleBar: Boolean = true,
            showRightMenu: Boolean = false,
            pushData: String = "",
        ) {
            val intent = Intent(context, WebViewActivity::class.java)
            val bundle = Bundle()
            bundle.putString(TITLE, title)
            bundle.putString(URL, URLDecoder.decode(url,"UTF-8"))
            bundle.putBoolean(SHOW_TITLE_BAR, showTitleBar)
            bundle.putBoolean(SHOW_RIGHT_MENU, showRightMenu)
            bundle.putString(PUSH_DATA, pushData)
            intent.putExtra(BUNDLE, bundle)
            context.startActivity(intent)
        }
    }

    override fun getLayoutResId() = R.layout.activity_empty_content

    lateinit var newInstance: WebViewFragment

    var originalUrl = ""

    override fun initView() {
        intent.extras?.run {
            val bundle = getBundle(BUNDLE)
            bundle?.run {
                val title = getString(TITLE, "")
                originalUrl = getString(URL, "")
                val showTitleBar = getBoolean(SHOW_TITLE_BAR, true)
                val showRightMenu = getBoolean(SHOW_RIGHT_MENU, false)
                val pushData = getString(PUSH_DATA, "")
                newInstance = WebViewFragment.newInstance(originalUrl, title, showTitleBar, showRightMenu,pushData = pushData)
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.content,
                        newInstance
                    )
                    .commitAllowingStateLoss()
            }
        }
    }

    override fun onBackPressed() {
        if (::newInstance.isInitialized && newInstance.goBack()) {

        } else {
            super.onBackPressed()
        }
    }

    override fun initData() {}
}