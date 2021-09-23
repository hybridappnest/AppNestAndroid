package com.tencent.qcloud.tim.uikit.modules.chat.layout.message.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.ymy.helper.ImHelper
import com.tencent.qcloud.tim.uikit.R
import com.tencent.qcloud.tim.uikit.TUIKit
import com.tencent.qcloud.tim.uikit.modules.message.Entry
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo
import com.tencent.qcloud.tim.uikit.modules.message.ViewData
import com.ymy.core.utils.StringUtils

/**
 * @author hanxueqiang
 * @date 2021-01-11 10:57:00
 */
class MessageFunctionHolder(itemView: View?) : MessageEmptyHolder(itemView) {
    private var mTitle: TextView? = null
    private var mTime: TextView? = null
    private var mSubTitle: TextView? = null
    private var mContent: LinearLayout? = null
    private var line: View? = null
    private var text1: View? = null
    private var arrow: View? = null
    override fun getVariableLayout(): Int {
        return R.layout.message_adapter_content_founction
    }

    override fun initVariableViews() {
        mTitle = rootView.findViewById(R.id.title)
        mTime = rootView.findViewById(R.id.time)
        mSubTitle = rootView.findViewById(R.id.sub_title)
        mContent = rootView.findViewById(R.id.content)
        mSubTitle = rootView.findViewById(R.id.sub_title)
        line = rootView.findViewById(R.id.line)
        text1 = rootView.findViewById(R.id.text1)
        arrow = rootView.findViewById(R.id.arrow)
    }

    override fun layoutViews(msg: MessageInfo, position: Int) {
        super.layoutViews(msg, position)
        if (msg.extraData != null && msg.extraData is ViewData) {
            val custom = msg.extraData as ViewData
            mTitle?.text = custom.title
            mTime?.text = StringUtils.toNYR_HMS(custom.time)
            if (custom.desc.isEmpty()) {
                mSubTitle?.visibility = View.GONE
            } else {
                mSubTitle?.run {
                    visibility = View.VISIBLE
                    text = custom.desc
                }
            }
            val entrys: List<Entry> = custom.entrys
            mContent?.removeAllViews()
            for (e in entrys) {
                val item = LayoutInflater.from(TUIKit.getAppContext())
                    .inflate(R.layout.item_content_founction, null) as ViewGroup
                val itemTitle = item.findViewById<View>(R.id.item_title) as TextView
                val itemContent = item.findViewById<View>(R.id.item_content) as TextView
                itemTitle.text = e.key + "："
                itemContent.text = e.value
                mContent?.addView(item)
            }
            msgContentFrame.setOnClickListener {
                val url = custom.url
                if (url.isNotEmpty()) {
                    ImHelper.getDBXSendReq().goToWebActivity(url)
                }
            }
            msgContentFrame.setOnLongClickListener {
                onItemClickListener.onMessageLongClick(it, position, msg)
                true
            }
            val bottomVisibility = if (custom.url.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            line?.visibility = bottomVisibility
            text1?.visibility = bottomVisibility
            arrow?.visibility = bottomVisibility
        }
    }
}