package com.ymy.appnest.aop

import android.os.SystemClock
import android.view.View
import com.orhanobut.logger.Logger
import org.aspectj.lang.ProceedingJoinPoint
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * Created on 2020/9/9 08:07.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object AopClickUtil {
    /**
     * 最近一次点击的时间
     */
    private var mViewLastClickTime: Long = 0
    private var mLastClickTime: Long = 0
    private var mLastViewWeakReference: WeakReference<View>? = null

    /**
     * 是否是快速点击
     *
     * @param intervalMillis  时间间期（毫秒）
     * @return  true:是，false:不是
     */
    fun isViewFastDoubleClick(view: View, intervalMillis: Long): Boolean {
        Logger.v("isViewFastDoubleClick in")
        val mLastView = mLastViewWeakReference?.get()
        return if (mLastView != null && mLastView != view) {
            mLastViewWeakReference = WeakReference(view)
            //不是同一个view，不判断双击
            Logger.v("isViewFastDoubleClick mLastView:$mLastView view:$view")
            false
        } else {
            val time = SystemClock.elapsedRealtime()
            val timeInterval = abs(time - mViewLastClickTime)
            //是同一个view，判断双击
            if (timeInterval < intervalMillis) {
//                ToastUtils.showToast(appContext, "请您不要频繁操作")
                Logger.v("isViewFastDoubleClick true")
                true
            } else {
                Logger.v("isViewFastDoubleClick false")
                mLastViewWeakReference = WeakReference(view)
                mViewLastClickTime = time
                false
            }
        }
    }

    /**
     * 是否是快速点击
     *
     * @param intervalMillis  时间间期（毫秒）
     * @return  true:是，false:不是
     */
    fun isFastDoubleClick(intervalMillis: Long, joinPoint: ProceedingJoinPoint): Boolean {
        val time = SystemClock.elapsedRealtime()
        val timeInterval = abs(time - mLastClickTime)
        return if (timeInterval < intervalMillis) {
            true
        } else {
            mLastClickTime = time
            false
        }
    }
}