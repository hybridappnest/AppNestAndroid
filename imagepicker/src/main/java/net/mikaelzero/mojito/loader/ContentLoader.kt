package net.mikaelzero.mojito.loader

import android.content.Context
import android.graphics.RectF
import android.view.View
import net.mikaelzero.mojito.interfaces.OnMojitoViewCallback

/**
 * @Author: MikaelZero
 * @CreateDate: 2020/6/10 9:54 AM
 * @Description:
 */
interface ContentLoader {
    val displayRect: RectF
    fun init(context: Context, originUrl:String,targetUrl: String?,onMojitoViewCallback: OnMojitoViewCallback?)
    fun providerView(): View
    fun providerRealView(): View
    fun dispatchTouchEvent(isDrag: Boolean, isActionUp: Boolean, isDown: Boolean, isHorizontal: Boolean): Boolean
    fun dragging(width: Int, height: Int, ratio: Float)
    fun beginBackToMin(isResetSize: Boolean)
    fun backToNormal()
    fun loadAnimFinish()
    fun needReBuildSize(): Boolean
    fun useTransitionApi(): Boolean
    fun isLongImage(width: Int, height: Int): Boolean
    fun onTapCallback(onTapCallback: OnTapCallback)
    fun onLongTapCallback(onLongTapCallback: OnLongTapCallback)
    fun pageChange(isHidden: Boolean)
}