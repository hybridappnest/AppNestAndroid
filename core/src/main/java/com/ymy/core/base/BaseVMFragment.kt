package com.ymy.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * Created on 2020/7/10 08:46.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
abstract class BaseVMFragment<VM : BaseViewModel>(useDataBinding: Boolean = true) : RootFragment() {
    private val _useBinding = useDataBinding
    protected lateinit var mBinding: ViewDataBinding
    protected lateinit var mViewModel: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (_useBinding) {
            mBinding = DataBindingUtil.inflate(inflater, getLayoutResId(), container, false)
            mBinding.root
        } else
            inflater.inflate(getLayoutResId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mViewModel = initVM()
        if (_useBinding) mBinding.lifecycleOwner = this
        initArguments()
        initView()
        initData()
        startObserve()
        super.onViewCreated(view, savedInstanceState)
    }
    open fun initArguments(){

    }

    abstract fun getLayoutResId(): Int
    abstract fun initVM(): VM
    abstract fun initView()
    abstract fun initData()
    abstract fun startObserve()
}