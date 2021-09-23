package com.ymy.appnest.notification

import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.Ringtone
import android.os.Build
import android.os.Vibrator
import android.provider.Settings
import android.widget.TextView
import com.ymy.appnest.R
import com.ymy.appnest.appContext
import com.ymy.appnest.ui.ACTION
import com.ymy.appnest.ui.DATA
import com.ymy.appnest.ui.DATA_TYPE
import com.ymy.appnest.ui.MainActivity
import com.ymy.appnest.view.CustomDialog
import kotlin.properties.Delegates

/**
 * Created on 11/17/20 08:00.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object NotificationHelper {
    const val channelID = "dbxIM"
    var notificationManager: NotificationManager by Delegates.notNull()
    var mVibrator: Vibrator? = null
    var mRingtone: Ringtone? = null

    fun initNotificationHelper(context: Context) {
        createNotificationChanel(context, channelID)
        mVibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
//        mRingtone = RingtoneManager.getRingtone(
//            context,
//            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
//        )
    }

    fun onDestroy() {
//        mVibrator?.run {
//            cancel()
//        }
//        mRingtone?.run {
//            stop()
//        }
    }

    private fun createNotificationChanel(context: Context, chanelID: String) {
        notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        //创建通知渠道的代码只在第一次执行的时候才会创建，以后每次执行创建代码系统会检测到该通知渠道已经存在了，因此不会重复创建
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = ""
            val description = ""
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(chanelID, channelName, importance)
            channel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.description = description
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun showNotification(title: String, content: String, senderId: String?, msgId: Int, type: Int) {
        mVibrator?.vibrate(longArrayOf(0, 500, 500), -1)
//        mRingtone?.play()
        val activity = PendingIntent.getActivity(
            appContext,
            0,
            Intent(appContext, MainActivity::class.java).apply {
                putExtra(ACTION, MainActivity.ACTION_JUMP_TO_IM)
                putExtra(DATA, senderId)
                putExtra(DATA_TYPE, type)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = Notification.Builder(appContext).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setChannelId(channelID)
            }
            setContentIntent(
                activity
            )
            setVisibility(Notification.VISIBILITY_PUBLIC)
            //设置状态栏小图标
            setSmallIcon(R.mipmap.ic_launcher)
            setLargeIcon(
                BitmapFactory.decodeResource(
                    appContext.resources,
                    R.mipmap.ic_launcher
                )
            )
            //设置服务内容
            setContentTitle(title)
            setContentText(content)
            setAutoCancel(true)
            //设置通知时间
            setWhen(System.currentTimeMillis())
        }.build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showNotificationSettingDialog(activity: Activity) {
        CustomDialog(activity, R.layout.dialog_notification_setting, scale = 2 / 3F).apply {
            setCancelable(true)
            initView = {
                val tvDesc = findViewById<TextView>(R.id.tv_desc)
                findViewById<TextView>(R.id.tv_cancel)?.setOnClickListener {
                    dismiss()
                }
                findViewById<TextView>(R.id.tv_sure)?.setOnClickListener {
                    gotoNotificationSetting(activity)
                    dismiss()
                }
            }
        }.show()
    }

    private fun gotoNotificationSetting(activity: Activity) {
        val uid = activity.applicationInfo.uid
        val pkg = activity.applicationContext.packageName
        try {
            val intent = Intent().apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
                    putExtra(Settings.EXTRA_CHANNEL_ID, uid)
                }
                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                putExtra("app_package", pkg)
                putExtra("app_uid", uid)
            }
            activity.startActivityForResult(intent, 0)
        } catch (e: Exception) {
            activity.startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0)
        }
    }
}