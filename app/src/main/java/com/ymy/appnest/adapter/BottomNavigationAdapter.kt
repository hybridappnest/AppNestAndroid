package com.ymy.appnest.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ymy.core.glide.ImageLoader
import com.ymy.appnest.R
import com.ymy.appnest.beans.HomeBottomNavigation

/**
 * Created on 1/16/21 09:10.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
open class BottomNavigationAdapter(
    layoutResId: Int = R.layout.item_bottom_navigation
) :
    BaseQuickAdapter<HomeBottomNavigation, BaseViewHolder>(layoutResId) {

    @SuppressLint("SetTextI18n")
    override fun convert(
        holder: BaseViewHolder,
        item: HomeBottomNavigation
    ) {
        val ivIcon = holder.getView<AppCompatImageView>(R.id.iv_icon)
        val unreadText = holder.getView<TextView>(R.id.tv_unread)
        val tvTitle = holder.getView<TextView>(R.id.tv_title)
        item.apply {
            val colorNormal = Color.parseColor(textColorRes)
            val colorSelected = Color.parseColor(textColorResSelected)
            if (iconUrl.isNotEmpty()) {
                ImageLoader.loadWithPlaceHolder(
                    iconUrl,
                    ivIcon,
                    R.mipmap.icon_daily_normal
                )
                if (isSelected) {
                    ivIcon.setColorFilter(colorSelected)
                    tvTitle.setTextColor(colorSelected)
                } else {
                    ivIcon.setColorFilter(colorNormal)
                    tvTitle.setTextColor(colorNormal)
                }
            } else {
                if (isSelected) {
                    ivIcon.setImageResource(imageResSelected)
                    tvTitle.setTextColor(colorSelected)
                } else {
                    ivIcon.setImageResource(imageRes)
                    tvTitle.setTextColor(colorNormal)
                }
            }
            setUnread(unreadText, unRead)
            tvTitle.text = item.name
        }
    }

    private fun setUnread(unreadText: TextView, unRead: Int) {
        if (unRead > 0) {
            unreadText.visibility = View.VISIBLE
            if (unRead > 99) {
                unreadText.text = "99+"
            } else {
                unreadText.text = unRead.toString()
            }
        } else {
            unreadText.visibility = View.GONE
        }
    }

}