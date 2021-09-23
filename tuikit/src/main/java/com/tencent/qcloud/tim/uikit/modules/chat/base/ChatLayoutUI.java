package com.tencent.qcloud.tim.uikit.modules.chat.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.NoticeEventLayout;
import com.tencent.qcloud.tim.uikit.component.NoticeLayout;
import com.tencent.qcloud.tim.uikit.component.NoticeV2Layout;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.interfaces.IChatLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.input.InputLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfoProvider;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.ymy.core.user.YmyUserManager;
import com.ymy.core.utils.StringUtils;

public abstract class ChatLayoutUI extends LinearLayout implements IChatLayout {

    protected NoticeLayout mGroupApplyLayout;
    protected NoticeEventLayout mNoticeEventLayout;
    protected NoticeV2Layout mNoticeYJYALayout;
    protected View mRecordingGroup;
    protected ImageView mRecordingIcon;
    protected TextView mRecordingTips;
    private TitleBarLayout mTitleBar;
    private MessageLayout mMessageLayout;
    private InputLayout mInputLayout;
    private NoticeLayout mNoticeLayout;
    private ChatInfo mChatInfo;
    public LinearLayout more_select;
    public LinearLayout jiaojie_layout;
    public LinearLayout   next_layout;
    public TextView selectNum;
    public LinearLayout im_shareLayout;
    public LinearLayout im_delLayout;

    public ChatLayoutUI(Context context) {
        super(context);
        initViews();
    }

    public ChatLayoutUI(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ChatLayoutUI(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.chat_layout, this);
        mTitleBar = findViewById(R.id.chat_title_bar);
        mMessageLayout = findViewById(R.id.chat_message_layout);
        mInputLayout = findViewById(R.id.chat_input_layout);
        mInputLayout.setChatLayout(this);
        mRecordingGroup = findViewById(R.id.voice_recording_view);
        mRecordingIcon = findViewById(R.id.recording_icon);
        mRecordingTips = findViewById(R.id.recording_tips);
        mGroupApplyLayout = findViewById(R.id.chat_group_apply_layout);
        mNoticeLayout = findViewById(R.id.chat_notice_layout);
        mNoticeEventLayout = findViewById(R.id.chat_notice_layout_alarm);
        mNoticeYJYALayout = findViewById(R.id.chat_notice_layout_yjya);
        more_select = findViewById(R.id.more_select);
        next_layout = findViewById(R.id.next_layout);
        selectNum = findViewById(R.id.selectNum);
        jiaojie_layout = findViewById(R.id.jiaojie_layout);
        im_shareLayout = findViewById(R.id.im_shareLayout);
        im_delLayout = findViewById(R.id.im_delLayout);

        init();
    }
    protected void init() {

    }

    @Override
    public InputLayout getInputLayout() {
        return mInputLayout;
    }

    @Override
    public MessageLayout getMessageLayout() {
        return mMessageLayout;
    }

    @Override
    public NoticeLayout getNoticeLayout() {
        return mNoticeLayout;
    }

    @Override
    public ChatInfo getChatInfo() {
        return mChatInfo;
    }

    @Override
    public void setChatInfo(final ChatInfo chatInfo) {
        mChatInfo = chatInfo;
        if (chatInfo == null) {
            return;
        }
        if (mChatInfo.getType() != V2TIMConversation.V2TIM_C2C) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setId(chatInfo.getId());
            GroupInfoProvider.getGroupCustom(groupInfo, new IUIKitCallBack() {
                @Override
                public void onSuccess(Object data) {
                    try {
                        GroupInfo groupInfo1 = (GroupInfo) data;
                        String  userCom = YmyUserManager.INSTANCE.getUser().getCompanyName();
                        if(!StringUtils.isEmpty(groupInfo1.getCompany_name())&&!groupInfo1.getCompany_name().equals(userCom)){
                            getTitleBar().setTitle(chatInfo.getChatName()+"@"+groupInfo1.getCompany_name(), TitleBarLayout.POSITION.MIDDLE);
                        }else {
                            getTitleBar().setTitle(chatInfo.getChatName(), TitleBarLayout.POSITION.MIDDLE);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {

                }
            });
        }else{
            getTitleBar().setTitle(chatInfo.getChatName(), TitleBarLayout.POSITION.MIDDLE);
        }

    }

    @Override
    public void exitChat() {

    }

    @Override
    public void initDefault() {

    }

    @Override
    public void loadMessages() {

    }

    @Override
    public void sendMessage(MessageInfo msg, boolean retry) {

    }

    @Override
    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    @Override
    public void setParentLayout(Object parent) {

    }
}
