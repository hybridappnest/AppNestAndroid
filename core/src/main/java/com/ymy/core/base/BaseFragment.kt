package com.ymy.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ymy.core.Ktx

/**
 * Created on 2020/7/10 08:46.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
abstract class BaseFragment(private val userViewBinding: Boolean = false) : RootFragment() {
    lateinit var contentView: View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        contentView = if (userViewBinding) {
            getBindingView() ?: View(Ktx.app)
        } else {
            inflater.inflate(getLayoutResId(), container, false)
        }
        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        initData()
        super.onViewCreated(view, savedInstanceState)
    }

    open fun getBindingView(): View? = null

    /**
     * content Layout id
     */
    open fun getLayoutResId(): Int = 0

    /**
     * 初始化视图
     */
    abstract fun initView()

    /**
     * 初始化数据
     */
    abstract fun initData()
}