package com.ymy.core.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.umeng.analytics.MobclickAgent
import com.ymy.core.utils.ToastUtils
import kotlinx.coroutines.cancel


/**
 * Created on 2020/7/10 08:46.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
abstract class RootActivity : AppCompatActivity() {
    val TAG = this::class.java.simpleName

    fun toast(msg: String, status: Boolean = true) {
        if (msg.isNotEmpty()) {
            ToastUtils.showImageToast(this, msg, status)
        }
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }

    override fun onDestroy() {
        lifecycleScope.cancel()
        super.onDestroy()
    }

}