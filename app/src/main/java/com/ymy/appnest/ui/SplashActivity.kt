package com.ymy.appnest.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.ymy.core.base.BaseActivity
import com.ymy.appnest.viewmodel.AppConfigViewModel
import com.ymy.appnest.BuildConfig
import com.ymy.appnest.databinding.ActivitySplashBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class SplashActivity : BaseActivity(true) {
    private var intent_action = ""
    private var intent_data: Bundle? = null
    private val mAppConfigViewModel: AppConfigViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setCutoutMode()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setCutoutMode() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        val lp = window.attributes
        lp.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = lp
    }

    override fun getIntentExtra() {
    }

    private val mBinding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }


    override fun getBindingView() = mBinding.root


    override fun initView() {
        mBinding.tvVersion.text = "Version: ${BuildConfig.VERSION_NAME}"
        mBinding.tvCountdown.setOnClickListener {
            jumpToMain()
        }
    }

    var hasGetAppConfig = false
    override fun initData() {
        mAppConfigViewModel.mUiData.observe(this) {
            it?.run {
                if (appConfig) {
                    hasGetAppConfig = true
                    if(countDownFinish){
                        jumpToMain()
                    }
                }
            }
        }
        mAppConfigViewModel.initAppConfig()
    }

    override fun onResume() {
        super.onResume()
        runOneSecond()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun runOneSecond() {
        doCountDownTask()
//        if (YmyUserManager.isLogin()) {
//            doCountDownTask()
//        } else {
//            jumpInLoginActivity()
//        }
    }

    @SuppressLint("SetTextI18n")
    private fun doCountDownTask() {
        lifecycleScope.launch {
            flowOf(3, 2, 1)
                .onEach { time ->
                    mBinding.tvCountdown.text = "$time 秒钟"
                    delay(1000)
                }.collect { time ->
                    if (time == 1) {
                        countDownFinish = true
                        jumpToMain()
                    }
                }
        }
    }

    private fun jumpInLoginActivity() {
//        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
//        finish()
    }

    private var hasJump = false
    private var countDownFinish = false

    private fun jumpToMain() {
        if (!hasGetAppConfig) {
            return
        }
        if (!hasJump) {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java).apply {
                intent_data?.let { putExtras(it) }
            })
            finish()
            hasJump = true
        }
    }

}