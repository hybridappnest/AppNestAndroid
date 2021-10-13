package com.ymy.core.router

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.launcher.ARouter

/**
 * Created on 2021/10/9 10:27.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object ARouterHelper {
    /**
     * 根据路径获取Fragment，如果找不到则返回emptyFragment
     * @param path String
     * @param placeHolderFragment Fragment
     * @return Fragment
     */
    fun getFragmentByPathWithPlaceHolder(path: String, placeHolderFragment: Fragment): Fragment {
        val navigation = ARouter.getInstance().build(path).navigation()
        return if (navigation != null) {
            navigation as Fragment
        } else {
            placeHolderFragment
        }
    }
}