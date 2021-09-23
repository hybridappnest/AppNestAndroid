package com.ymy.core.base

import android.os.Bundle
import android.view.View
import com.ymy.core.R


/**
 * Created on 2020/7/10 08:46.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
abstract class BaseActivity(private val userViewBinding: Boolean = false) : RootActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentExtra()
        if (userViewBinding) {
            setContentView(getBindingView())
        } else {
            setContentView(getLayoutResId())
        }
        initView()
        initData()
    }

    open fun getIntentExtra() {

    }

    open var stateBarColorId = R.color.app_blue

    open fun getLayoutResId(): Int {
        return 0
    }

    open fun getBindingView(): View? {
        return null
    }

    abstract fun initView()
    abstract fun initData()

}