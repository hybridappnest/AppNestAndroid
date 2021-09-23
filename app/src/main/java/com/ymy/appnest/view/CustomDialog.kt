package com.ymy.appnest.view

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatDialog
import com.ymy.appnest.R
import com.ymy.core.utils.ScreenUtils


/**
 * Created on 2020/7/20 18:34.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class CustomDialog(
    val activity: Activity,
    val layoutId: Int,
    val mIsDismissTouchOut: Boolean = false,
    val gravity: Int = Gravity.CENTER,
    val mAnimationResId: Int = R.style.bottom_animation,
    val scale: Float = 2 / 3F,
    val needFullHeight:Boolean = false
) : AppCompatDialog(activity, R.style.Custom_Dialog_Style) {
    lateinit var initView: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setGravity(gravity) // 设置自定义的dialog位置3
        if(mAnimationResId != 0){
            window?.setWindowAnimations(mAnimationResId) //添加自定义动画
        }
        setContentView(layoutId)

        val lp = window!!.attributes

        val screenWidth = ScreenUtils.getScreenWidth(activity)

        lp.width = (screenWidth * scale).toInt()
        if(needFullHeight){
            val screenHeight = ScreenUtils.getScreenHeight(activity)
            lp.height = screenHeight
        }

        window?.attributes = lp

        setCanceledOnTouchOutside(mIsDismissTouchOut)
        initView.invoke()
    }
}