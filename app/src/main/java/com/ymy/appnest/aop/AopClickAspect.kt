package com.ymy.appnest.aop

import android.os.SystemClock
import android.view.View
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature

/**
 * Created on 2020/9/9 08:07.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

@Aspect
class AopClickAspect {


    /**
     * 定义切点，标记切点为所有被@Keep
    @AopOnclick注解的方法
     * 注意：这里com.ymy.appnest.aop.AopOnclick需要替换成
     * 你自己项目中AopOnclick这个类的全路径
     */
    @Pointcut("execution(@com.ymy.appnest.aop.AopOnclick * *(..))")
    fun methodAnnotated() {
    }

    /**
     * 定义一个切面方法，包裹切点方法
     */
    @Around("methodAnnotated()")
    @Throws(Throwable::class)
    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint) {
        // 取出方法的注解
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        if (!method.isAnnotationPresent(AopOnclick::class.java)) {
            return
        }
        val aopOnclick = method.getAnnotation(AopOnclick::class.java)
        // 判断是否快速点击
        if (!isFastDoubleClick(aopOnclick.value)) {
            // 不是快速点击，执行原方法
            joinPoint.proceed()
        }
    }

    private var mLastRunTime: Long = 0

    /**
     * 是否是快速点击
     *
     * @param intervalMillis  时间间期（毫秒）
     * @return  true:是，false:不是
     */
    private fun isFastDoubleClick(intervalMillis: Long): Boolean {
        val time = SystemClock.elapsedRealtime()
        val timeInterval = kotlin.math.abs(time - mLastRunTime)
        return if (timeInterval < intervalMillis) {
            true
        } else {
            mLastRunTime = time
            false
        }
    }

    /**
     * 定义一个切面方法，包裹切点方法
     */
    @Around("execution(* android.view.View.OnClickListener.onClick(..))")
    @Throws(Throwable::class)
    fun viewOnClickJoinPoint(joinPoint: ProceedingJoinPoint) {
        val view = joinPoint.args[0] as View
        if (!AopClickUtil.isViewFastDoubleClick(view,1000)) {
            // 不是快速点击，执行原方法
            joinPoint.proceed()
        }
    }

    @Around("execution(* com.chad.library.adapter.base.listener.OnItemClickListener.onItemClick(..))")
    @Throws(Throwable::class)
    fun onItemClickJoinPoint(joinPoint: ProceedingJoinPoint) {
        val view = joinPoint.args[0] as View
        if (!AopClickUtil.isViewFastDoubleClick(view,1000)) {
            // 不是快速点击，执行原方法
            joinPoint.proceed()
        }
    }

}