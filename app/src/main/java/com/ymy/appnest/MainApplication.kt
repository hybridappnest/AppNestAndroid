package com.ymy.appnest

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import com.ymy.helper.ConfigHelper
import com.ymy.signature.GenerateTestUserSig
import com.lcw.library.imagepicker.ImagePicker
import com.tencent.qcloud.tim.uikit.TUIKit
import com.ymy.camera.CameraV2Activity
import com.ymy.appnest.di.appModule
import com.ymy.appnest.ui.SplashActivity
import com.ymy.camera.JCameraView
import com.ymy.core.base.IUIKitCallBack
import com.ymy.core.exception.GlobalThreadUncaughtExceptionHandler
import com.ymy.core.ok3.DBXRetrofitClient
import com.ymy.core.utils.TUIKitConstants
import com.ymy.core.utils.UncaughtExceptionHandlerImpl
import com.ymy.image.imagepicker.ImagePreManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Created on 2021/9/6 14:22.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
lateinit var appContext: Application
    private set

open class MainApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }


    override fun onCreate() {
        super.onCreate()
        checkAppProcess { init() }
    }

    /**
     * 保障只别初始化一次，因mpaas的原因，有多个进程，会被多次调用onCreate
     */
    private fun checkAppProcess(action: () -> Unit) {
        val processName = getProcessName(this)
        if (processName != null) {
            if (processName == BuildConfig.APPLICATION_ID) {
                action.invoke()
            }
        } else {
            action.invoke()
        }
    }

    private fun getProcessName(context: Context): String? {
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps =
            am.runningAppProcesses ?: return null
        for (proInfo in runningApps) {
            if (proInfo.pid == Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName
                }
            }
        }
        return null
    }


    private fun init() {
        appContext = applicationContext as Application
        DBXRetrofitClient.initClient(appContext, object : DBXRetrofitClient.HttpListener {
            override fun doLogout() {
            }
        })
        startKoin {
            AndroidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }

        ImagePreManager.init(this)

        GlobalThreadUncaughtExceptionHandler.setUp()

        UncaughtExceptionHandlerImpl.init(
            this,
            BuildConfig.DEBUG,
            false,
            0,
            SplashActivity::class.java
        )

        /**
         * TUIKit的初始化函数
         *
         * @param context  应用的上下文，一般为对应应用的ApplicationContext
         * @param sdkAppID 您在腾讯云注册应用时分配的sdkAppID
         * @param configs  TUIKit的相关配置项，一般使用默认即可，需特殊配置参考API文档
         */
        TUIKit.init(this, GenerateTestUserSig.SDKAPPID, ConfigHelper().getConfigs(this))

        initImagePicker()

        closeAndroidPDialog()

        TUIKitConstants.initPath()
    }

    private fun initImagePicker() {
        ImagePicker.getInstance().setCameraViewCallBack { _, type, callBack ->
            CameraV2Activity.invoke(this,
                if (type == ImagePicker.CameraView.type_all) {
                    JCameraView.BUTTON_STATE_BOTH
                } else {
                    JCameraView.BUTTON_STATE_ONLY_CAPTURE
                }, object : IUIKitCallBack {
                    override fun onSuccess(data: Any) {
                        if (data is String) {
                            callBack.onSuccess(ImagePicker.CameraViewCallBack.type_photo, data)
                        } else if (data is Intent) {
                            val videoPath = data.getStringExtra(TUIKitConstants.CAMERA_VIDEO_PATH)
                            callBack.onSuccess(ImagePicker.CameraViewCallBack.type_video, videoPath)
                        }
                    }

                    override fun onError(module: String, errCode: Int, errMsg: String) {
                        callBack.onError(module, errCode, errMsg)
                    }
                })
        }
    }

    /**
     * 解决androidP 第一次打开程序出现莫名弹窗
     * 弹窗内容“detected problems with api ”
     */
    fun closeAndroidPDialog() {
        try {
            val aClass = Class.forName("android.content.pm.PackageParser\$Package")
            val declaredConstructor = aClass.getDeclaredConstructor(String::class.java)
            declaredConstructor.setAccessible(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val cls = Class.forName("android.app.ActivityThread")
            val declaredMethod: Method = cls.getDeclaredMethod("currentActivityThread")
            declaredMethod.isAccessible = true
            val activityThread: Any = declaredMethod.invoke(null)
            val mHiddenApiWarningShown: Field = cls.getDeclaredField("mHiddenApiWarningShown")
            mHiddenApiWarningShown.isAccessible = true
            mHiddenApiWarningShown.setBoolean(activityThread, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
