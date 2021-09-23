package com.ymy.appnest.ui.setting.fragment

import android.view.View
import androidx.navigation.Navigation
import com.ymy.core.base.BaseFragment
import com.ymy.core.glide.ImageLoader
import com.ymy.core.user.YmyUserManager
import com.ymy.appnest.R
import com.ymy.appnest.databinding.FragmentMineBinding

/**
 * Created on 2020/8/8 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class MineFragment : BaseFragment(true) {

    companion object {
        fun newInstance(): MineFragment {
            return MineFragment()
        }
    }

    val mBinding: FragmentMineBinding by lazy {
        FragmentMineBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View? = mBinding.root

    override fun initView() {
        initTitleBar()
        mBinding.llSetUserHeader.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_mineFragment_to_mineSetHeaderFragment)
        }
        mBinding.llSetUserNickname.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_mineFragment_to_mineSetNicknameFragment)
        }
        ImageLoader.loadWithPlaceHolder(
            YmyUserManager.user.avatarUrl,
            mBinding.ivUserHeader,
            R.mipmap.icon_default_header
        )
        YmyUserManager.user.run {
            mBinding.tvMineNickname.text = nickname
            mBinding.tvMineUserName.text = realName
            mBinding.tvMineSexual.text = when (gender) {
                1 -> "男"
                2 -> "女"
                else -> "未知"
            }
            mBinding.tvMineUserIdcardId.text = idCardNo
            mBinding.tvMinePhone.text = phone
        }
    }

    private fun initTitleBar() {
        mBinding.titleBar.btnBack.setOnClickListener {
            activity?.finish()
        }
        mBinding.titleBar.tvTitlebarTitle.text = "个人信息"
    }

    override fun initData() {
    }

}