package com.ymy.appnest.ui.gallery.adapter

import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.util.getItemView
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * Created on 1/6/21 09:07.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
abstract class BaseGalleryItemProvider : BaseItemProvider<IGallerySourceModel>() {

    private val mScreenPoint = Point()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        //获取屏幕宽高
        val displayMetrics = context.resources.displayMetrics
        mScreenPoint.x = displayMetrics.widthPixels
        mScreenPoint.y = displayMetrics.heightPixels
        return getBaseVideoHolder(parent.getItemView(layoutId))
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is BaseVideoHolder) {
            holder.onViewAttachedToWindow()
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is BaseVideoHolder) {
            holder.onViewDetachedFromWindow()
        }
    }

    abstract fun getBaseVideoHolder(view: View): BaseVideoHolder


    abstract class BaseVideoHolder(itemView: View) : BaseViewHolder(itemView) {
        var data: IGallerySourceModel? = null
            set(value) {
                field = value
                initViewByData()
            }

        /**
         * 根据数据初始化视图
         */
        abstract fun initViewByData()

        open fun onViewAttachedToWindow() {

        }

        open fun onViewDetachedFromWindow() {

        }
    }
}