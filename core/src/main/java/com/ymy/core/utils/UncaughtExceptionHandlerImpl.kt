package com.ymy.core.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.orhanobut.logger.Logger
import com.ymy.core.BuildConfig
import com.ymy.core.lifecycle.KtxManager
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 异常处理类
 *
 *
 * Author: nanchen
 * Email: liushilin520@foxmail.com
 * Date: 2017-07-21  15:22
 */
@SuppressLint("StaticFieldLeak")
object UncaughtExceptionHandlerImpl : Thread.UncaughtExceptionHandler {
    val TAG = "CrashHandler"

    // 系统默认的 UncaughtException 处理类
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    // 程序的 Context 对象
    lateinit var mContext: Context

    // 用来存储设备信息和异常信息
    private val infos: MutableMap<String, String> =
        HashMap()

    // 用于格式化日期,作为日志文件名的一部分
    @SuppressLint("SimpleDateFormat")
    private val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")

    //是否是Debug模式
    private var mIsDebug = BuildConfig.DEBUG

    //是否重启APP
    private var mIsRestartApp // 默认需要重启
            = false

    //重启APP时间
    private var mRestartTime: Long = 0

    // 重启后跳转的Activity
    private var mRestartActivity: Class<*>? = null

    // Toast 显示文案
    private var mTips: String? = null

    /**
     * @param context         上下文
     * @param isDebug         是否是Debug模式
     * @param isRestartApp    是否支持重启APP
     * @param restartTime     延迟重启时间
     * @param restartActivity 重启后跳转的 Activity，我们建议是 SplashActivity
     */
    fun init(
        context: Context,
        isDebug: Boolean,
        isRestartApp: Boolean,
        restartTime: Long,
        restartActivity: Class<*>?
    ) {
        mIsRestartApp = isRestartApp
        mRestartTime = restartTime
        mRestartActivity = restartActivity
        init(context, isDebug)
    }

    fun init(context: Context, isDebug: Boolean) {
        mTips = "很抱歉，程序出现异常，即将退出..."
        mIsDebug = isDebug
        mContext = context
        // 获取系统默认的 UncaughtException 处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该 CrashHandler 为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当 UncaughtException 发生时会转入该函数来处理
     */
    @SuppressLint("WrongConstant")
    override fun uncaughtException(
        thread: Thread,
        ex: Throwable
    ) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                Logger.e(e, "error : ${e.message}")
            }
            if (mIsRestartApp) { // 如果需要重启
                val intent = Intent(mContext.applicationContext, mRestartActivity)
                val mAlarmManager =
                    mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                //重启应用，得使用PendingIntent
                val restartIntent = PendingIntent.getActivity(
                    mContext.applicationContext, 0, intent,
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //Android6.0以上，包含6.0
                    mAlarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC,
                        System.currentTimeMillis() + 1000,
                        restartIntent
                    ) //解决Android6.0省电机制带来的不准时问题
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //Android4.4到Android6.0之间，包含4.4
                    mAlarmManager.setExact(
                        AlarmManager.RTC,
                        System.currentTimeMillis() + 1000,
                        restartIntent
                    ) // 解决set()在api19上不准时问题
                } else {
                    mAlarmManager[AlarmManager.RTC, System.currentTimeMillis() + 1000] =
                        restartIntent
                }
            }
            // 结束应用
            KtxManager.finishAllActivity()
        }
    }

    /**
     * 自定义错误处理，收集错误信息，发送错误报告等操作均在此完成
     *
     * @param ex
     * @return true：如果处理了该异常信息；否则返回 false
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }
        Logger.e(ex, "error : ${ex.message}")

        // 使用 Toast 来显示异常信息
        object : Thread() {
            override fun run() {
                Looper.prepare()
                Toast.makeText(mContext, getTips(ex), Toast.LENGTH_LONG).show()
                Logger.e(ex, "程序出现异常 error : ${ex.message}")
                Looper.loop()
            }
        }.start()


        //  如果用户不赋予外部存储卡的写权限导致的崩溃，会造成循环崩溃
        if (mIsDebug) {
            // 收集设备参数信息
            collectDeviceInfo(mContext)
            // 保存日志文件
            saveCrashInfo2File(ex)
        }
        return true
    }

    private fun getTips(ex: Throwable): String? {
        if (ex is SecurityException) {
            mTips = if (ex.message!!.contains("android.permission.CAMERA")) {
                "请授予应用相机权限，程序出现异常，即将退出."
            } else if (ex.message!!.contains("android.permission.RECORD_AUDIO")) {
                "请授予应用麦克风权限，程序出现异常，即将退出。"
            } else if (ex.message!!.contains("android.permission.WRITE_EXTERNAL_STORAGE")) {
                "请授予应用存储权限，程序出现异常，即将退出。"
            } else if (ex.message!!.contains("android.permission.READ_PHONE_STATE")) {
                "请授予应用电话权限，程序出现异常，即将退出。"
            } else if (ex.message!!.contains("android.permission.ACCESS_COARSE_LOCATION") || ex.message!!.contains(
                    "android.permission.ACCESS_FINE_LOCATION"
                )
            ) {
                "请授予应用位置信息权，很抱歉，程序出现异常，即将退出。"
            } else {
                "很抱歉，程序出现异常，即将退出，请检查应用权限设置。"
            }
        }
        return mTips
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    fun collectDeviceInfo(ctx: Context) {
        try {
            val pm = ctx.packageManager
            val pi = pm.getPackageInfo(
                ctx.packageName,
                PackageManager.GET_ACTIVITIES
            )
            if (pi != null) {
                val versionName =
                    if (pi.versionName == null) "null" else pi.versionName
                val versionCode = pi.versionCode.toString() + ""
                infos["versionName"] = versionName
                infos["versionCode"] = versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(
                TAG,
                "an error occured when collect package info",
                e
            )
        }
        val fields =
            Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                infos[field.name] = field[null].toString()
                Log.d(
                    TAG,
                    field.name + " : " + field[null]
                )
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "an error occured when collect crash info",
                    e
                )
            }
        }
    }

    /**
     * 保存错误信息到文件中 *
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private fun saveCrashInfo2File(ex: Throwable): String? {
        val sb = StringBuffer()
        for ((key, value) in infos) {
            sb.append("$key=$value\n")
        }
        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result = writer.toString()
        sb.append(result)
        try {
            val timestamp = System.currentTimeMillis()
            val time = formatter.format(Date())
            val fileName = "crash-$time-$timestamp.log"
            if (Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
            ) {
                val path = "/sdcard/" + mContext.packageName + "/crash/"
                val dir = File(path)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val fos = FileOutputStream(path + fileName)
                fos.write(sb.toString().toByteArray())
                fos.close()
            }
            return fileName
        } catch (e: Exception) {
            Log.e(
                TAG,
                "an error occured while writing file...",
                e
            )
        }
        return null
    }


}