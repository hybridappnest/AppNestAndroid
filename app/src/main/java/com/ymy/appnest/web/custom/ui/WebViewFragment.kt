package com.ymy.appnest.web.custom.ui

import android.view.View
import com.just.agentweb.AgentWeb
import com.ymy.core.base.BaseFragment
import com.ymy.core.base.Refresher
import com.ymy.core.notchtools.NotchTools
import com.ymy.core.umeng.UmengUtils
import com.ymy.appnest.web.custom.H5WebView
import com.ymy.appnest.web.custom.WebViewTitleBarController
import com.ymy.appnest.R
import com.ymy.appnest.databinding.FragmentWebviewBinding
import com.ymy.appnest.ui.MainActivity

/**
 * Created on 1/28/21 08:29.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
const val BUNDLE = "Bundle"
const val TITLE = "Title"
const val URL = "Url"
const val SHOW_TITLE_BAR = "ShowTitleBar"
const val SHOW_RIGHT_MENU = "showRightMenu"
const val PUSH_DATA = "push_data"

class WebViewFragment : BaseFragment(true), WebViewTitleBarController, Refresher {
    var url = ""
    var title = ""

    companion object {
        fun newInstance(
            url: String,
            title: String = "",
            showTitleBar: Boolean = true,
            showRightMenu: Boolean = false,
            showBackIcon: Boolean = true,
            addTopPadding: Boolean = false,
            showRefreshBar: Boolean = false,
            pushData: String = "",
        ): WebViewFragment {
            val fragment = WebViewFragment()
            val args = WebViewFragmentArgs
                .Builder()
                .apply {
                    this.title = title
                    this.url = url
                    this.showTitleBar = showTitleBar
                    this.showRightMenu = showRightMenu
                    this.showBackIcon = showBackIcon
                    this.addTopPadding = addTopPadding
                    this.showRefreshBar = showRefreshBar
                    this.pushData = pushData
                }
                .build()
                .toBundle()
            fragment.arguments = args
            return fragment
        }
    }

    val mBinding: FragmentWebviewBinding by lazy {
        FragmentWebviewBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View? = mBinding.root

    override fun getCloseButton() = mBinding.titleBar.btnClose


    override fun getRightMenuText() = mBinding.titleBar.tvBtnRight


    override fun getTitle() = mBinding.titleBar.tvTitlebarTitle


    override fun getRightMenuImage() = mBinding.titleBar.ivBtnRight

    override fun onProgressChanged(newProgress: Int) {
        if (showRefreshBar) {
            if (newProgress == 100) {
                //隐藏进度条
                mBinding.swipeContainer.isRefreshing = false
            } else {
                if (!mBinding.swipeContainer.isRefreshing)
                    mBinding.swipeContainer.isRefreshing = true
            }
        }
    }

    private lateinit var mWebView: AgentWeb
    private val mH5WebView: H5WebView by lazy {
        H5WebView(requireActivity(), false, this)
    }

    /**
     * 初始化视图
     */
    override fun initView() {
        if (requireActivity() is MainActivity) {
        }
    }

    fun goBack(): Boolean {
        return if (::mWebView.isInitialized) {
            if (mH5WebView.webError) {
                false
            } else {
                mWebView.back()
            }
        } else {
            false
        }
    }

    var showRefreshBar = false
    var pushData = ""
    override fun initData() {
        arguments?.run {
            pushData = WebViewFragmentArgs.fromBundle(this).pushData
            val showTitleBar = WebViewFragmentArgs.fromBundle(this).showTitleBar
            val title = WebViewFragmentArgs.fromBundle(this).title
            url = WebViewFragmentArgs.fromBundle(this).url
            if (url.isEmpty()) {
                toast("传入参数异常，网页已关闭")
                requireActivity().finish()
            }
            if (showTitleBar) {
                val showBackIcon = WebViewFragmentArgs.fromBundle(this).showBackIcon
                val addTopPadding = WebViewFragmentArgs.fromBundle(this).addTopPadding
                if (addTopPadding) {
                    requireActivity().also {
                        val statusHeight =
                            NotchTools.getFullScreenTools().getStatusHeight(it.window)
                        mBinding.titleBar.titleBar.setPadding(0, statusHeight, 0, 0)
                    }
                }
                mBinding.titleBar.titleBar.visibility = View.VISIBLE
                mBinding.titleBar.btnClose.visibility = View.GONE
                if (showBackIcon) {
                    mBinding.titleBar.btnBack.visibility = View.VISIBLE
                } else {
                    mBinding.titleBar.btnBack.visibility = View.GONE
                }
                mBinding.titleBar.btnBack.setOnClickListener {
                    activity?.onBackPressed()
                }
                mBinding.titleBar.btnClose.setOnClickListener {
                    activity?.finish()
                }
                mBinding.titleBar.tvTitlebarTitle.text = title
            } else {
                mBinding.titleBar.titleBar.visibility = View.GONE
            }
            showRefreshBar = WebViewFragmentArgs.fromBundle(this).showRefreshBar
            mBinding.swipeContainer.isEnabled = showRefreshBar
        }
        mBinding.swipeContainer.run {
            setColorSchemeResources(R.color.colorPrimary)
            setOnRefreshListener {
                onRefresh()
            }
            setOnChildScrollUpCallback { _, _ ->
                // 原生安卓使用getScrollY()来判断在Y轴的距离
                mWebView.webCreator.webView.scrollY > 0
            }
        }
        uploadEvent(url)
        mWebView = mH5WebView.initWebView(mBinding.container, url, pushData)
    }

    private fun uploadEvent(url: String) {
        context?.let {
            UmengUtils.uploadEvent(it, "WebViewUrl", mutableMapOf<String, String>().apply {
                this["url"] = url
            })
        }
    }

    override fun onRefresh() {
        if (isAdded) {
            mH5WebView.refreshUrl()
        }
    }

}