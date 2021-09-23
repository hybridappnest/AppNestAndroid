package com.tencent.qcloud.tim.uikit.modules.chat.layout.message.holder;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageListAdapter;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.utils.DateTimeUtil;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;

import java.util.Date;

public abstract class MessageEmptyHolder extends MessageBaseHolder {

    public TextView chatTimeText;
    public TextView chat_time_tv_temp;
    public FrameLayout msgContentFrame;
    private LinearLayout im_msg_slect_layout;
    private TextView im_msg_slect;

    public MessageEmptyHolder(View itemView) {
        super(itemView);
        rootView = itemView;
        chat_time_tv_temp = itemView.findViewById(R.id.chat_time_tv_temp);
        chatTimeText = itemView.findViewById(R.id.chat_time_tv);
        msgContentFrame = itemView.findViewById(R.id.msg_content_fl);
        im_msg_slect_layout = itemView.findViewById(R.id.im_msg_slect_layout);
        im_msg_slect = itemView.findViewById(R.id.im_msg_slect);

        initVariableLayout();
    }

    public abstract int getVariableLayout();

    private void setVariableLayout(int resId) {
        if (msgContentFrame.getChildCount() == 0) {
            View.inflate(rootView.getContext(), resId, msgContentFrame);
        }
        initVariableViews();
    }

    private void initVariableLayout() {
        if (getVariableLayout() != 0) {
            setVariableLayout(getVariableLayout());
        }
    }

    public abstract void initVariableViews();

    @Override
    public void layoutViews(final MessageInfo msg, final int position) {

        //// 时间线设置
        if (properties.getChatTimeBubble() != null) {
            chatTimeText.setBackground(properties.getChatTimeBubble());
        }
        if (properties.getChatTimeFontColor() != 0) {
            chatTimeText.setTextColor(properties.getChatTimeFontColor());
        }
        if (properties.getChatTimeFontSize() != 0) {
            chatTimeText.setTextSize(properties.getChatTimeFontSize());
        }
        if (msg.isSelect()) {
            im_msg_slect.setBackgroundResource(R.drawable.check_box_selected);
        } else {
            im_msg_slect.setBackgroundResource(R.drawable.check_box_unselected);
        }
        im_msg_slect_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageInfo item = mAdapter.getItem(position);
                item.setSelect(!msg.isSelect());
                int num = mAdapter.getSelectNum();
                if (num > 20) {
                    ToastUtil.toastShortMessage("最多可选择20条消息");
                    item.setSelect(!msg.isSelect());
                    return;
                }
                mAdapter.setSelectNum(num);
                mAdapter.notifyDataSetChanged();
            }
        });
        if (MessageListAdapter.isShowSelect) {
            im_msg_slect_layout.setVisibility(View.VISIBLE);
        } else {
            im_msg_slect_layout.setVisibility(View.GONE);
        }
        if (position > 1) {
            MessageInfo last = mAdapter.getItem(position - 1);
            if (last != null) {
                if (msg.getMsgTime() - last.getMsgTime() >= 5 * 60) {
                    chatTimeText.setVisibility(View.VISIBLE);
                    chat_time_tv_temp.setVisibility(View.INVISIBLE);
                    chatTimeText.setText(DateTimeUtil.getTimeFormatText(new Date(msg.getMsgTime() * 1000)));
                } else {
                    chatTimeText.setVisibility(View.GONE);
                    chat_time_tv_temp.setVisibility(View.GONE);
                }
            }
        } else {
            chatTimeText.setVisibility(View.VISIBLE);
            chat_time_tv_temp.setVisibility(View.INVISIBLE);
            chatTimeText.setText(DateTimeUtil.getTimeFormatText(new Date(msg.getMsgTime() * 1000)));
        }
    }

}
