package com.ymy.appnest.ui.setting.fragment

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.ymy.core.base.BaseFragment
import com.ymy.core.exts.hideSoftInput
import com.ymy.core.exts.showSoftInput
import com.ymy.core.user.YmyUserManager
import com.ymy.core.utils.RegexpUtils
import com.ymy.appnest.R
import com.ymy.appnest.appContext
import com.ymy.appnest.custom.FilerEnterSpaceTextWatcher
import com.ymy.appnest.databinding.FragmentChangePhoneBinding
import com.ymy.appnest.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Created on 2020/8/8 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class ChangePhoneFragment : BaseFragment(true) {


    val mBinding: FragmentChangePhoneBinding by lazy {
        FragmentChangePhoneBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View? = mBinding.root

    private var getVarCodeToken = ""

    override fun initView() {
        initTitleBar()
        context?.let {
            showSoftInput(it, mBinding.edNewPhone)
        }
        mBinding.container.setOnClickListener {
            context?.run {
                hideSoftInput(this, it)
            }
        }

        mBinding.edNewPhone.addTextChangedListener(
            afterTextChanged = {
                checkMobile(it.toString())
            }
        )
        mBinding.edNewPhone.addTextChangedListener(FilerEnterSpaceTextWatcher(mBinding.edNewPhone))
    }

    private fun checkMobile(str: String) {
        if (!startCountDown && RegexpUtils.isMobileNumber(str)) {
            mobile = str
            mBinding.tvRequestVarcode.setBackgroundResource(R.drawable.selector_login_btn_bg)
        } else {
            mBinding.tvRequestVarcode.setBackgroundResource(R.drawable.shape_login_button_bg_disable_notran)
        }
    }

    var mobile = ""
    private fun initTitleBar() {
        mBinding.titleBar.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
        mBinding.titleBar.tvTitlebarTitle.text = "更换手机号"
        mBinding.titleBar.btnRightLayout.visibility = View.VISIBLE
        mBinding.titleBar.tvBtnRight.visibility = View.VISIBLE
        mBinding.titleBar.tvBtnRight.text = "保存"
        mBinding.titleBar.tvBtnRight.setOnClickListener {
            checkMobileAndVarCode()
        }
        mBinding.tvRequestVarcode.setOnClickListener {
            requestSendVarCode()
        }
    }

    private fun checkMobileAndVarCode() {
        if (!RegexpUtils.isMobileNumber(mobile)) {
            toast("您输入的电话号码错误", false)
            return
        }
        val varCode = mBinding.edNewPhoneVarcode.text.toString()
        if (varCode.length < 6) {
            toast("您输入验证码错误", false)
            return
        }
        requestModifyMobile(mobile, varCode, getVarCodeToken)
    }

    override fun initData() {
        mBinding.tvOldMobile.text = YmyUserManager.user.phone
    }

    private val DEFAULT_COUNT_TIME = 60
    private var time = DEFAULT_COUNT_TIME
    private var startCountDown = false
    private val strGetVarCode = appContext.getString(R.string.str_login_get_var_code)
    private val strReGetVarCode = appContext.getString(R.string.str_login_re_get_var_code)
    private val repository: UserRepository by inject()

    /**
     * 请求发送验证码
     */
    private fun requestSendVarCode() {
        if (startCountDown) {
            toast("请您稍后再试", false)
            return
        }
        if (!RegexpUtils.isMobileNumber(mobile)) {
            toast("您输入的手机号错误", false)
            return
        }
        if (mobile == YmyUserManager.user.phone) {
            toast("您输入的新手机号与旧号码相同", false)
            return
        }
        requestSendVarCodePost(mobile)
    }

    private fun startCountDownGetVarCode() {
        lifecycleScope.launch(Dispatchers.Main) {
            startCountDown = true
            checkMobile(mBinding.edNewPhone.text.toString())
            while (time > 0) {
                mBinding.tvRequestVarcode.text = strReGetVarCode + "(${time})"
                delay(1000)
                time--
                if (time == 0) {
                    startCountDown = false
                    time = DEFAULT_COUNT_TIME
                    mBinding.tvRequestVarcode.text = strGetVarCode
                    checkMobile(mBinding.edNewPhone.text.toString())
                    break
                }
            }
        }
    }

    /**
     * 获取验证码
     * @param phone String
     */
    private fun requestSendVarCodePost(phone: String) {
//        lifecycleScope.launch(Dispatchers.Main) {
//            val job = async(Dispatchers.IO) {
//                repository.getSendVerificationCode(phone)
//            }
//            val result = job.await()
//            result.checkResult(
//                onSuccess = {
//                    getVarCodeToken = it.verifyToken
//                    toast("获取验证码成功")
//                    startCountDownGetVarCode()
//                },
//                onError = {
//                    if (it != null) {
//                        toast(it, false)
//                    }
//                })
//        }
    }

    /**
     * 修改手机号
     * @param phone String
     * @param code String
     */
    private fun requestModifyMobile(phone: String, code: String, getVarCodeToken: String) {
//        lifecycleScope.launch(Dispatchers.Main) {
//            val job = async(Dispatchers.IO) {
//                repository.modifyMobile(phone, code, getVarCodeToken)
//            }
//            val result = job.await()
//            result.checkResult(
//                onSuccess = {
//                    toast("手机号码修改成功")
//                    YmyUserManager.setUserPhone(phone)
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