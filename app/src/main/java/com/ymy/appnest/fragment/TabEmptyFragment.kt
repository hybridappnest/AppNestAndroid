package com.ymy.appnest.fragment

import android.os.Bundle
import android.view.View
import com.ymy.appnest.R
import com.ymy.appnest.databinding.FragmentTabEmptyBinding
import com.ymy.core.base.BaseFragment
import com.ymy.core.base.getColorCompat
import com.ymy.core.notchtools.NotchTools


/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class TabEmptyFragment : BaseFragment(true) {

    companion object {
        fun newInstance(
            title: String = "",
            backgroundRes: Int,
            backgroundColor: Int,
            textColor: Int,
            showTitleBar: Boolean = true,
        ): TabEmptyFragment {
            val fragment = TabEmptyFragment()
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putInt("backgroundRes", backgroundRes)
            bundle.putInt("backgroundColor", backgroundColor)
            bundle.putInt("textColor", textColor)
            bundle.putBoolean("showTitleBar", showTitleBar)
            fragment.arguments = bundle
            return fragment
        }
    }


    val mBinding: FragmentTabEmptyBinding by lazy {
        FragmentTabEmptyBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View? = mBinding.root

    override fun initView() {
        activity?.also {
            val statusHeight = NotchTools.getFullScreenTools().getStatusHeight(it.window)
            mBinding.titleBar.titleBar.setPadding(0, statusHeight, 0, 0)
        }
        mBinding.titleBar.btnBack.visibility = View.GONE
        arguments?.run {
            mBinding.titleBar.tvTitlebarTitle.text = this.getString("title")
            val textColor = this.getInt("textColor", getColorCompat(R.color.white))
            val backgroundRes = this.getInt("backgroundRes", 0)
            if (backgroundRes != 0) {
                mBinding.container.setBackgroundResource(backgroundRes)
                mBinding.tvContent.setTextColor(textColor)
            }
            val backgroundColor = this.getInt("backgroundColor", 0)
            if (backgroundColor != 0) {
                mBinding.container.setBackgroundColor(backgroundColor)
                mBinding.tvContent.setTextColor(textColor)
            }
            val showTitleBar = this.getBoolean("showTitleBar", true)
            if (!showTitleBar) {
                mBinding.titleBar.titleBar.visibility = View.GONE
            }
        }
    }

    override fun initData() {
    }

}