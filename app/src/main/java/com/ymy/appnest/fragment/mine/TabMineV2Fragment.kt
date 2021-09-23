package com.ymy.appnest.fragment.mine

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ymy.core.BuildConfig
import com.ymy.core.base.BaseFragment
import com.ymy.core.base.Refresher
import com.ymy.core.glide.ImageLoader
import com.ymy.core.permission.DBXPermissionUtils
import com.ymy.core.permission.requestPermission
import com.ymy.core.user.YmyUserManager
import com.ymy.appnest.ui.setting.activity.MineActivity
import com.ymy.appnest.ui.setting.activity.SettingAboutV2Activity
import com.ymy.appnest.ui.setting.activity.SettingActivity
import com.ymy.appnest.R
import com.ymy.appnest.beans.MineItemBean
import com.ymy.appnest.beans.MineListAction
import com.ymy.appnest.databinding.FragmentTabMineV2Binding
import com.ymy.appnest.fragment.mine.adapter.MineItemAdapter
import com.ymy.appnest.qrcode.HWQRCodeScannerActivity
import com.ymy.appnest.ui.ACTION
import com.ymy.appnest.ui.MainActivity
import com.ymy.appnest.web.WebUrlConstant
import com.ymy.appnest.web.custom.ui.WebViewActivity


/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class TabMineV2Fragment : BaseFragment(), Refresher {
    private val mineItemAdapter = MineItemAdapter()
    override fun onRefresh() {

    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        mineItemAdapter.setList(dataList)
        ImageLoader.loadWithPlaceHolder(
            YmyUserManager.user.avatarUrl,
            mBinding.headerLayout.ivHeader,
            R.mipmap.icon_default_header
        )
        val nickname = if (YmyUserManager.user.nickname.isEmpty()) {
            ""
        } else {
            "(${YmyUserManager.user.nickname})"
        }
        mBinding.headerLayout.tvMineTabNickname.text = "${YmyUserManager.user.realName}${nickname}"
        if (BuildConfig.DEBUG) {
            mBinding.headerLayout.ivHeader.setOnClickListener {
//                activity?.let { it1 -> TestLocationActivity.invoke(it1) }
            }
        }
    }

    override fun getLayoutResId() = R.layout.fragment_tab_mine_v2

    private val dataList = arrayListOf<MineItemBean>()

    private fun initListData() {
        dataList.add(
            MineItemBean()
        )
        dataList.add(
            MineItemBean(
                "设置中心",
                R.mipmap.icon_default_header,
                action = MineListAction.settingCenter
            )
        )
        dataList.add(
            MineItemBean(
                "扫一扫",
                R.mipmap.icon_default_header,
                action = MineListAction.QRCodeScanner
            )
        )
        dataList.add(
            MineItemBean(
                "帮助中心",
                R.mipmap.icon_default_header,
                action = MineListAction.web,
                url = WebUrlConstant.helperCenterUrl
            )
        )
        dataList.add(
            MineItemBean(
                "问题反馈",
                R.mipmap.icon_default_header,
                action = MineListAction.web,
                url = WebUrlConstant.settingFeedBack
            )
        )
        dataList.add(
            MineItemBean(
                "关于鼎保信",
                R.mipmap.icon_default_header,
                action = MineListAction.about
            )
        )
    }

    val mBinding: FragmentTabMineV2Binding by lazy {
        FragmentTabMineV2Binding.bind(contentView)
    }

    override fun initView() {
        initListData()
        mineItemAdapter.run {
            setList(dataList)
            setOnItemClickListener { adapter, _, position ->
                val mineItem = adapter.data[position] as MineItemBean
                jumpByItem(mineItem)
            }
        }
        mBinding.rcMineList.run {
            adapter = mineItemAdapter
            layoutManager = LinearLayoutManager(context)
        }
        mBinding.headerLayout.llMineHeaderUserinfo.setOnClickListener {
            context?.let { it1 -> MineActivity.invoke(it1) }
        }
        mBinding.headerLayout.btnItem1.setOnClickListener {
            startActivity(Intent(activity, MainActivity::class.java).apply {
                putExtra(ACTION, MainActivity.ACTION_CHANGE_HOME_TAB)
                putExtra(MainActivity.ACTION_CHANGE_HOME_TAB_INDEX, 1)
            })
        }
    }


    private fun jumpByItem(mine: MineItemBean) {
        when (mine.action) {
            MineListAction.settingCenter -> context?.let {
                SettingActivity.invoke(it)
            }
            MineListAction.QRCodeScanner -> context?.let {
                requestPermission(
                    requireContext(),
                    "你好:\n" +
                            "     该功能需要访问您的相册及拍照、录制视频，鉴于您禁用相关权限，请手动设置开启权限:\n" +
                            "1、【相机】\n",
                    arrayOf(
                        DBXPermissionUtils.CAMERA,
                    ),
                    actionGranted = {
                        HWQRCodeScannerActivity.invoke(it)
                    },
                    actionDenied = {
                        toast("无权限无法使用该功能", false)
                    })
            }
            MineListAction.web -> {
                openUrl(mine.url, mine.name)
            }
            MineListAction.about -> {
                activity?.let { SettingAboutV2Activity.invoke(it) }
            }
        }
    }

    private fun openUrl(url: String, title: String = "", params: Bundle = Bundle()) {
        val addUrlParams = WebUrlConstant.addUrlParams(url)
        context?.let {
            WebViewActivity.invoke(
                it,
                addUrlParams,
                title = title
            )
        }
    }

    override fun initData() {
    }

}