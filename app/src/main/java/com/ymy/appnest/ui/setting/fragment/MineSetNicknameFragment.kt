package com.ymy.appnest.ui.setting.fragment

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.ymy.core.base.BaseFragment
import com.ymy.core.base.getColorCompat
import com.ymy.core.exts.hideSoftInput
import com.ymy.core.exts.showSoftInput
import com.ymy.core.user.YmyUserManager
import com.ymy.core.view.DBXLoadingView
import com.ymy.appnest.R
import com.ymy.appnest.databinding.FragmentSetNicknameBinding
import com.ymy.appnest.ui.setting.viewmodel.UserModifyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created on 2020/8/8 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class MineSetNicknameFragment : BaseFragment(true), TextWatcher {

    private val userModifyViewModel: UserModifyViewModel by viewModel()


    val mBinding: FragmentSetNicknameBinding by lazy {
        FragmentSetNicknameBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View? = mBinding.root

    lateinit var loadingView: DBXLoadingView
    override fun initView() {
        initTitleBar()
        mBinding.edNickname.addTextChangedListener(this)
        mBinding.edNickname.setText(YmyUserManager.user.nickname)
        context?.let { showSoftInput(it, mBinding.edNickname) }
        mBinding.container.setOnClickListener {
            context?.let { con -> hideSoftInput(con, mBinding.edNickname) }
        }
        loadingView = DBXLoadingView(mBinding.container)
        mBinding.btnClear.setOnClickListener {
            mBinding.edNickname.setText("")
        }
    }

    private fun initTitleBar() {
        mBinding.titleBar.btnBack.setOnClickListener {
            back()
        }
        mBinding.titleBar.tvTitlebarTitle.text = "修改昵称"
        mBinding.titleBar.btnRightLayout.visibility = View.VISIBLE
        mBinding.titleBar.tvBtnRight.visibility = View.VISIBLE
        mBinding.titleBar.tvBtnRight.text = "完成"
        mBinding.titleBar.tvBtnRight.setOnClickListener {
            doChangeUserNickname()
        }
    }

    private fun back() {
        view?.let { Navigation.findNavController(it).popBackStack() }
    }

    private fun doChangeUserNickname() {
        val newNickname = mBinding.edNickname.text.toString().trim()
        if (newNickname.toCharArray().size > 18) {
            toast("您输入的昵称超过规定长度")
            return
        }
        if (newNickname.isEmpty()) {
            toast("昵称不能为空")
            return
        }
        changeNickname(newNickname)
    }

    private fun changeNickname(nickName: String) {
        userModifyViewModel.netUiLiveData.observe(this, Observer {
            it?.run {
                if (isLoading) {
                    loadingView.show()
                } else {
                    loadingView.hide()
                }
                isSuccess?.run {
                    toast("昵称修改成功")
                    YmyUserManager.setUserNickname(nickName)
                    back()
                }
                isError?.run {
                    toast(this)
                }
            }
        })
        userModifyViewModel.modifyUserInfo(nickName)
    }


    override fun initData() {
    }

    override fun afterTextChanged(s: Editable?) {
        setNicknameMaxLength(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.toString().contains(" ")) {
            val str: List<String> = s.toString().split(" ")
            var str1 = ""
            for (i in str.indices) {
                str1 += str[i]
            }
            mBinding.edNickname.setText(str1)
            mBinding.edNickname.setSelection(start)
        }
    }

    private fun setNicknameMaxLength(s: String) {
        val size = s.toCharArray().size
        if (size <= 18) {
            mBinding.tvNicknameMaxLength.setTextColor(getColorCompat(R.color.gray888888))
        } else {
            mBinding.tvNicknameMaxLength.setTextColor(getColorCompat(R.color.red))
        }
        mBinding.tvNicknameMaxLength.text = "长度$size/18字节"
    }
}