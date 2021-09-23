package com.tencent.qcloud.tim.uikit.modules.conversation.holder;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationIconView;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.utils.DateTimeUtil;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.ymy.core.user.YmyUserManager;
import com.ymy.core.utils.StringUtils;

import java.util.Date;

public class ConversationCommonHolder extends ConversationBaseHolder {

    public static final String topLabel = "置顶";
    public ConversationIconView conversationIconView;
    protected View leftItemLayout;
    protected TextView titleText;
    protected TextView messageText;
    protected TextView timelineText;
    protected TextView unreadText;
    protected TextView tvMarket;
    protected View ivImportant;
    int colorBlue;
    int colorRed;

    public ConversationCommonHolder(View itemView) {
        super(itemView);
        leftItemLayout = rootView.findViewById(R.id.item_left);
        conversationIconView = rootView.findViewById(R.id.conversation_icon);
        titleText = rootView.findViewById(R.id.conversation_title);
        tvMarket = rootView.findViewById(R.id.tv_market);
        messageText = rootView.findViewById(R.id.conversation_last_msg);
        timelineText = rootView.findViewById(R.id.conversation_time);
        unreadText = rootView.findViewById(R.id.conversation_unread);
        ivImportant = rootView.findViewById(R.id.iv_important);
        colorBlue = rootView.getContext().getResources().getColor(R.color.im_item_market_color);
        colorRed = rootView.getContext().getResources().getColor(R.color.redDF2024);
    }

    @Override
    public void layoutViews(ConversationInfo conversation, int position) {
        MessageInfo lastMsg = conversation.getLastMessage();
        if (lastMsg != null && lastMsg.getStatus() == MessageInfo.MSG_STATUS_REVOKE) {
            if (lastMsg.isSelf()) {
                lastMsg.setExtra("您撤回了一条消息");
            } else if (lastMsg.isGroup()) {
                String message = TUIKitConstants.covert2HTMLString(
                        TextUtils.isEmpty(lastMsg.getGroupNameCard())
                                ? lastMsg.getFromUser()
                                : lastMsg.getGroupNameCard());
                lastMsg.setExtra(message + "撤回了一条消息");
            } else {
                lastMsg.setExtra("对方撤回了一条消息");
            }
        }

        //        if (conversation.isTop()) {
        //            leftItemLayout.setBackgroundColor(rootView.getResources().getColor(R.color
        //            .conversation_top_color));
        //        } else {
        //            leftItemLayout.setBackgroundColor(Color.WHITE);
        //        }
        String userCom = YmyUserManager.INSTANCE.getUser().getCompanyName();
        if (!StringUtils.isEmpty(conversation.getCompany_name()) && !conversation.getCompany_name().equals(userCom)) {
            titleText.setText(conversation.getTitle() + "@" + conversation.getCompany_name());
        } else {
            titleText.setText(conversation.getTitle());
        }
        messageText.setText("");
        timelineText.setText("");
        if (lastMsg != null) {
            if (lastMsg.getExtra() != null) {
                if (lastMsg.getExtra() != null && lastMsg.getExtra() instanceof MessageCustom) {
                    MessageCustom custom = (MessageCustom) lastMsg.getExtra();
                    messageText.setText("系统消息：" + custom.getDesc());
                } else {
                    messageText.setText(Html.fromHtml(lastMsg.getExtra().toString()));
                }
                //                messageText.setTextColor(rootView.getResources().getColor(R
                //                .color.list_bottom_text_bg));
            }
            timelineText.setText(DateTimeUtil.getTimeFormatText(new Date(lastMsg.getMsgTime() * 1000)));
        }

        if (conversation.getUnRead() > 0) {
            unreadText.setVisibility(View.VISIBLE);
            if (conversation.getUnRead() > 99) {
                unreadText.setText("99+");
            } else {
                unreadText.setText("" + conversation.getUnRead());
            }
        } else {
            unreadText.setVisibility(View.GONE);
        }

        conversationIconView.setRadius(mAdapter.getItemAvatarRadius());
        if (mAdapter.getItemDateTextSize() != 0) {
            timelineText.setTextSize(mAdapter.getItemDateTextSize());
        }
        if (mAdapter.getItemBottomTextSize() != 0) {
            messageText.setTextSize(mAdapter.getItemBottomTextSize());
        }
        if (mAdapter.getItemTopTextSize() != 0) {
            titleText.setTextSize(mAdapter.getItemTopTextSize());
        }
        if (!mAdapter.hasItemUnreadDot()) {
            unreadText.setVisibility(View.GONE);
        }

        if (conversation.getIconUrlList() != null) {
            conversationIconView.setConversation(conversation);
        }
        ivImportant.setVisibility(TextUtils.equals(conversation.getIs_key_part(), "1") ?
                View.VISIBLE : View.INVISIBLE);

        String label = conversation.getLable();
        if (label.isEmpty()) {
            tvMarket.setVisibility(View.GONE);
        } else {
            if (topLabel.equals(label)) {
                tvMarket.setTextColor(colorRed);
                tvMarket.setBackgroundResource(R.drawable.shape_im_list_item_market_red_bg);
            } else {
                tvMarket.setTextColor(colorBlue);
                tvMarket.setBackgroundResource(R.drawable.shape_im_list_item_market_bg);
            }
            tvMarket.setText(label);
            tvMarket.setVisibility(View.VISIBLE);
        }
        // 由子类设置指定消息类型的views
        layoutVariableViews(conversation, position);
    }

    public void layoutVariableViews(ConversationInfo conversationInfo, int position) {

    }
}
