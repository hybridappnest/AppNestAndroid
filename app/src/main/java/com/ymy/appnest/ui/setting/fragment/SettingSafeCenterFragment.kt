package com.ymy.appnest.ui.setting.fragment

import androidx.navigation.Navigation
import com.ymy.core.base.BaseFragment
import com.ymy.appnest.R
import com.ymy.appnest.databinding.FragmentSettingSafeCenterBinding

/**
 * Created on 2020/8/8 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class SettingSafeCenterFragment : BaseFragment(true) {

    val mBinding: FragmentSettingSafeCenterBinding by lazy {
        FragmentSettingSafeCenterBinding.inflate(layoutInflater)
    }

    override fun getBindingView() = mBinding.root

    override fun initView() {
        initTitleBar()
        mBinding.safeCenterChangePassword.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingSafeCenterFragment_to_changePasswordFragment)

        }
        mBinding.safeCenterChangePhone.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingSafeCenterFragment_to_changePhoneFragment)
        }
    }

    private fun initTitleBar() {
        mBinding.titleBar.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
        mBinding.titleBar.tvTitlebarTitle.text = "安全中心"
    }

    override fun initData() {
    }

}