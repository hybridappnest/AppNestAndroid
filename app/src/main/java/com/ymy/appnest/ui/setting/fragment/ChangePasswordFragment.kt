package com.ymy.appnest.ui.setting.fragment

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.ymy.core.base.BaseFragment
import com.ymy.core.exts.hideSoftInput
import com.ymy.core.exts.showSoftInput
import com.ymy.appnest.R
import com.ymy.appnest.custom.FilerEnterSpaceTextWatcher
import com.ymy.appnest.databinding.FragmentChangePasswordBinding

/**
 * Created on 2020/8/8 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class ChangePasswordFragment : BaseFragment(true) {

    val mBinding: FragmentChangePasswordBinding by lazy {
        FragmentChangePasswordBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View = mBinding.root

    override fun initView() {
        initTitleBar()
        context?.let { showSoftInput(it, mBinding.edOldPassword) }
        mBinding.container.setOnClickListener {
            context?.let { con -> hideSoftInput(con, it) }
        }

        mBinding.edOldPassword.inputType =
            EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
        mBinding.edNewPassword.inputType =
            EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
        mBinding.edNewPasswordConfirm.inputType =
            EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD

        mBinding.edOldPassword.setOnClickListener {
            context?.let { it1 -> showSoftInput(it1, it as EditText) }
        }
        mBinding.edNewPassword.setOnClickListener {
            context?.let { it1 -> showSoftInput(it1, it as EditText) }
        }
        mBinding.edNewPasswordConfirm.setOnClickListener {
            context?.let { it1 -> showSoftInput(it1, it as EditText) }
        }
        mBinding.toggleOldPasswordVisible.setOnClickListener {
            toggleEdPasswordVisible(mBinding.toggleOldPasswordVisible, mBinding.edOldPassword)
        }
        mBinding.toggleNewPasswordVisible.setOnClickListener {
            toggleEdPasswordVisible(mBinding.toggleNewPasswordVisible, mBinding.edNewPassword)
            toggleEdPasswordVisible(null, mBinding.edNewPasswordConfirm)
        }

        mBinding.edOldPassword.addTextChangedListener(FilerEnterSpaceTextWatcher(mBinding.edOldPassword))
        mBinding.edNewPassword.addTextChangedListener(FilerEnterSpaceTextWatcher(mBinding.edNewPassword))
        mBinding.edNewPasswordConfirm.addTextChangedListener(
            FilerEnterSpaceTextWatcher(
                mBinding.edNewPasswordConfirm
            )
        )
    }

    private fun toggleEdPasswordVisible(imageView: ImageView?, ed: EditText) {
        if (ed.inputType == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) {
            imageView?.setImageResource(R.mipmap.icon_password_visible)
            ed.inputType =
                EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            ed.setSelection(ed.text.length)
        } else {
            imageView?.setImageResource(R.mipmap.icon_password_invisible)
            ed.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
            ed.setSelection(ed.text.length)
        }
    }

    override fun initData() {
    }

    private fun initTitleBar() {
        mBinding.titleBar.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
        mBinding.titleBar.tvTitlebarTitle.text = "修改密码"
        mBinding.titleBar.btnRightLayout.visibility = View.VISIBLE
        mBinding.titleBar.tvBtnRight.visibility = View.VISIBLE
        mBinding.titleBar.tvBtnRight.text = "完成"
        mBinding.titleBar.tvBtnRight.setOnClickListener {
            context?.let { it1 -> hideSoftInput(it1, it) }
            checkInputInfo()
        }
    }

    private fun checkInputInfo() {
        val oldPassword = mBinding.edOldPassword.text.toString()
        val newPassword = mBinding.edNewPassword.text.toString()
        val newPasswordConfirm = mBinding.edNewPasswordConfirm.text.toString()
        if (oldPassword.isEmpty()) {
            toast("你没有输入旧密码", false)
            return
        }
        if (oldPassword.length < 6) {
            toast("您输入的旧密码格式不正确", false)
            return
        }
        if (newPassword.isEmpty()) {
            toast("你没有输入新密码", false)
            return
        }
        if (newPassword.length < 6) {
            toast("您输入的新密码格式不正确", false)
            return
        }
        if (newPasswordConfirm.isEmpty()) {
            toast("请你输入新确认密码", false)
            return
        }
        if (newPasswordConfirm != newPassword) {
            toast("您输入的确认密码与新密码不一致", false)
            return
        }
        if (newPasswordConfirm == oldPassword) {
            toast("新密码不能与旧密码相同", false)
            return
        }
        doChangePasswordRequest(oldPassword, newPassword)
    }

//    private val repository: UserRepository by inject()
    private fun doChangePasswordRequest(
        oldPassword: String,
        newPassword: String,
    ) {
//        lifecycleScope.launch {
//            val job = async(Dispatchers.IO) {
//                repository.modifyPassword(oldPassword, newPassword)
//            }
//            val result = job.await()
//            result.checkResult(
//                onSuccess = {
//                    toast("修改密码成功")
//                    activity?.onBackPressed()
//                },
//                onError = {
//                    if (it != null) {
//                        toast(it, false)
//                    }
//                })
//        }
    }
}