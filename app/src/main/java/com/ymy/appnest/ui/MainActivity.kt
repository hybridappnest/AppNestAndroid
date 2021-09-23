package com.ymy.appnest.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.tencent.smtt.export.external.TbsCoreSettings
import com.tencent.smtt.sdk.QbSdk
import com.ymy.core.base.BaseActivity
import com.ymy.core.utils.StatusBarTool
import com.ymy.appnest.R
import com.ymy.appnest.appContext
import com.ymy.appnest.databinding.ActivityMainBinding
import com.ymy.appnest.fragment.HomeTabFragment
import com.ymy.appnest.push.NotificationHelper
import java.util.HashMap

/**
 * Created on 2021/9/6 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class MainActivity : BaseActivity(true) {

    companion object {
        const val ACTION_TO_LOGIN = "needLogin"
        const val ACTION_TO_SWITCH = "toSwitch"
        const val ACTION_CHANGE_HOME_TAB = "action_change_home_tab"
        const val ACTION_JUMP_TO_IM = "action_jump_to_im"
        const val ACTION_CHANGE_HOME_TAB_INDEX = "action_change_home_tab_index"
        const val ACTION_CHANGE_HOME_TAB_SUB_INDEX = "action_change_home_tab_sub_index"

        var hasInitX5 = false
    }

    val mMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val mHomeTabFragment by lazy { HomeTabFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setCutoutMode()
        }
        StatusBarTool.setTranslucentStatus(this)
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

    override fun getBindingView() = mMainBinding.root

    override fun initView() {

        initFragment()
    }


    private fun initFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.home_container, mHomeTabFragment)
        transaction.commit()
    }


    override fun initData() {

        checkNotifySetting()

        if (!hasInitX5) {
            initX5()
            hasInitX5 = true
        }
    }

    /**
     * X5内核初始化放到这里，因为放到Application中初始化时有可能找不到so库
     */
    private fun initX5() {
        // 在调用TBS初始化、创建WebView之前进行如下配置
        val map = HashMap<String, Any>()
        map[TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER] = true
        map[TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE] = true
        QbSdk.initTbsSettings(map)
        val cb: QbSdk.PreInitCallback = object : QbSdk.PreInitCallback {
            override fun onCoreInitFinished() {
            }

            override fun onViewInitFinished(p0: Boolean) {
            }
        }
        QbSdk.initX5Environment(appContext, cb)
    }

    /**
     * 检查通知权限，用户展示没有通知权限的dialog
     */
    private fun checkNotifySetting() {
        val manager = NotificationManagerCompat.from(this)
        val isOpened = manager.areNotificationsEnabled()
        if (!isOpened) {
            NotificationHelper.showNotificationSettingDialog(this@MainActivity)
        } else {
            NotificationHelper.initNotificationHelper(this@MainActivity)
        }
    }

}