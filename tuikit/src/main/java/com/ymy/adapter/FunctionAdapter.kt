package com.ymy.adapter

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tencent.qcloud.tim.uikit.R
import com.ymy.core.bean.FunctionInfo
import com.ymy.core.glide.ImageLoader

/**
 * Created on 1/16/21 09:10.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
open class FunctionAdapter(
    layoutResId: Int = R.layout.item_function
) :
    BaseQuickAdapter<FunctionInfo, BaseViewHolder>(layoutResId) {

    @SuppressLint("SetTextI18n")
    override fun convert(
        holder: BaseViewHolder,
        item: FunctionInfo
    ) {
        item.apply {
            val ivIcon = holder.getView<AppCompatImageView>(R.id.iv_icon)
//            val unreadText = holder.getView<TextView>(R.id.conversation_unread)
//            if (conversationInfo != null) {
//                val unRead = conversationInfo!!.unRead
//                if (unRead > 0) {
//                    unreadText.visibility = View.VISIBLE
//                    if (unRead > 99) {
//                        unreadText.text = "99+"
//                    } else {
//                        unreadText.text = "" + unRead
//                    }
//                } else {
//                    unreadText.visibility = View.GONE
//                }
//            } else {
//                unreadText.visibility = View.GONE
//            }
//            if (avatarUrl.isNullOrEmpty() || !avatarUrl.startsWith("http")) {
//                val iconId = when (functionInfo.channel) {
//                    "alarm" -> {
//                        R.drawable.icon_mine_alarm
//                    }
//                    "emergency" -> {
//                        R.drawable.icon_mine_acting
//                    }
//                    "engine-log" -> {
//                        R.drawable.icon_mine_host_log
//                    }
//                    "inspect" -> {
//                        R.drawable.icon_mine_inspect
//                    }
//                    "patrol" -> {
//                        R.drawable.icon_mine_inspect
//                    }
//                    "work-order" -> {
//                        R.drawable.icon_mine_workorder
//                    }
//                    "trouble-shoot" -> {
//                        R.drawable.icon_mine_hidden_peril
//                    }
//                    "handover" -> {
//                        R.drawable.icon_mine_handover
//                    }
//                    "train" -> {
//                        R.drawable.icon_mine_traing
//                    }
//                    "fire-record" -> {
//                        R.drawable.icon_mine_host_log
//                    }
//                    "notice" -> {
//                        R.drawable.icon_mine_helpe_center
//                    }
//                    else -> {
//                        R.drawable.icon_mine_helpe_center
//                    }
//                }
//                ivIcon.setImageResource(iconId)
//            } else {
            ImageLoader.loadRound(
                avatarUrl,
                ivIcon,
                4,
                context.resources.getDrawable(R.drawable.group_common_list)
            )
//            }
            val tvTitle = holder.getView<TextView>(R.id.tv_title)
            tvTitle.text = title
        }
    }

}