package com.ymy.appnest.ui.setting.activity

import android.content.Context
import android.content.Intent
import com.ymy.core.base.BaseActivity
import com.ymy.appnest.R


/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class SettingActivity : BaseActivity() {
    companion object {
        fun invoke(context: Context) {
            val intent = Intent(context, SettingActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getLayoutResId() = R.layout.activity_setting
    override fun initView() {
    }

    override fun initData() {
    }
}