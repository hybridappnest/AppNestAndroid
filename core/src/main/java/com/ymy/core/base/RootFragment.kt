package com.ymy.core.base

import androidx.fragment.app.Fragment
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
abstract class RootFragment : Fragment() {
    /**
     * fragment嵌套时，在父Fragment中设置为false，否则会统计异常
     */
    open var needMobClick = true

    val TAG = this::class.java.simpleName

    fun toast(msg: String, status: Boolean = true) {
        ToastUtils.showImageToast(context, msg, status)
    }

    override fun onDestroy() {
        lifecycleScope.cancel()
        super.onDestroy()
    }

    fun <T> findView(id: Int): T? {
        return view?.findViewById(id)
    }

    open fun onBackPressed(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        if (needMobClick) {
            MobclickAgent.onPageStart(this.TAG)
        }
    }

    override fun onPause() {
        super.onPause()
        if (needMobClick) {
            MobclickAgent.onPageEnd(this.TAG)
        }
    }
}