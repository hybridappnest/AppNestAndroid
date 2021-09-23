package com.ymy.appnest.fragment.mine.adapter

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ymy.core.glide.ImageLoader
import com.ymy.appnest.BuildConfig
import com.ymy.appnest.R
import com.ymy.appnest.beans.MineItemBean
import com.ymy.appnest.beans.MineListAction

/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
open class MineItemAdapter(
    layoutResId: Int = R.layout.item_mine_list
) :
    BaseQuickAdapter<MineItemBean, BaseViewHolder>(layoutResId) {

    override fun convert(
        holder: BaseViewHolder,
        item: MineItemBean
    ) {
        val itemLayout = holder.getView<View>(R.id.item_layout)
        val flEmptyLine = holder.getView<View>(R.id.fl_empty_line)
        val tvSubText = holder.getView<TextView>(R.id.tv_sub_text)
        if (item.action != MineListAction.empty) {
            itemLayout.visibility = View.VISIBLE
            flEmptyLine.visibility = View.GONE
            item.apply {
                val ivIcon = holder.getView<AppCompatImageView>(R.id.iv_mine_item_icon)
                val tvTitle = holder.getView<TextView>(R.id.iv_mine_item_title)
                val tvRedDot = holder.getView<TextView>(R.id.iv_mine_item_dot)
                if (iconRes != -1) {
                    ivIcon.setImageResource(iconRes)
                } else if (iconUrl.isNotEmpty()) {
                    ImageLoader.loadWithPlaceHolder(
                        iconUrl,
                        ivIcon,
                        R.mipmap.icon_default_header
                    )
                }
                tvTitle.text = name
                tvRedDot.run {
                    if (item.redDot == 0) {
                        visibility = View.INVISIBLE
                    } else {
                        visibility = View.VISIBLE
                        text = item.redDot.toString()
                    }
                }
            }
            if (item.action == MineListAction.about) {
                tvSubText.text = "版本:${BuildConfig.VERSION_NAME}"
            }
        } else {
            itemLayout.visibility = View.GONE
            flEmptyLine.visibility = View.VISIBLE
        }
    }

}