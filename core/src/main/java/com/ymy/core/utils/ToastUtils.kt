package com.ymy.core.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.orhanobut.logger.Logger
import com.ymy.core.R


/**
 * Created on 2020/8/28 10:53.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object ToastUtils {
    //显示文本+图片的Toast
    @JvmStatic
    fun showImageToast(context: Context?, message: String?, success: Boolean = true) {
        if (message?.isEmpty() == true) {
            return
        }
        try {
            val toastview: View =
                LayoutInflater.from(context).inflate(R.layout.layout_custom_toast, null)
            toastview.findViewById<ImageView>(R.id.iv_status)?.run {
                setImageResource(if (success) R.drawable.icon_custom_toast_success else R.drawable.icon_custom_toast_fail)
            }
            val text = toastview.findViewById<TextView>(R.id.tv_toast_msg)
            text.text = message //要提示的文本
            val toast = Toast(context) //上下文
            toast.setGravity(Gravity.FILL, 0, 0) //位置居中
            toast.duration = Toast.LENGTH_SHORT //设置短暂提示
            toast.view = toastview //把定义好的View布局设置到Toast里面
            toast.show()
        } catch (e: Exception) {

        }
    }

    @JvmStatic
    fun showToast(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun showTestToast(context: Context?, message: String?) {
        if (false) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        if (message != null) {
            Logger.e(message)
        }
    }
}