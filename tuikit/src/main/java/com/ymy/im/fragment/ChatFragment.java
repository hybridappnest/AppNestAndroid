package com.ymy.im.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.ymy.im.activity.GroupMemberListActivity;
import com.ymy.im.activity.SelectSessionActivity;
import com.ymy.im.function.Func;
import com.ymy.im.helper.ChatLayoutHelper;
import com.ymy.im.helper.ImHelper;
import com.ymy.im.helper.type.EventType;
import com.ymy.im.utils.Constants;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.BaseFragment;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.AudioPlayer;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.ChatLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.input.FunctionBtnData;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.tencent.qcloud.tim.uikit.modules.group.info.GroupInfoProvider;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.ymy.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;



public class ChatFragment extends BaseFragment implements ImHelper.UIListener {

    public static ChatInfo mChatInfo;

    public static ArrayList<String> noNoticeLayoutEventType;

    static {
        noNoticeLayoutEventType = new ArrayList<String>();
        noNoticeLayoutEventType.add(EventType.TYPE_NORMAL);
        noNoticeLayoutEventType.add(EventType.TYPE_SJPX);
        noNoticeLayoutEventType.add(EventType.TYPE_ZKJJ);
    }

    public String mEventType = EventType.TYPE_NORMAL;
    private View mBaseView;
    private ChatLayout mChatLayout;
    private TitleBarLayout mTitleBar;
    private HashMap<String, ArrayList<FunctionBtnData>> mFunctionBarCache = new HashMap<>();
    private Boolean needRefreshListData = false;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LiveEventBus.get(SelectSessionActivity.ChatRefreshNotice.class).observe(this,
                new Observer<SelectSessionActivity.ChatRefreshNotice>() {
                    @Override
                    public void onChanged(SelectSessionActivity.ChatRefreshNotice chatRefreshNotice) {
                        String chatId = chatRefreshNotice.getChatId();
                        if (!chatId.isEmpty() && chatId.equals(mChatInfo.getId())) {
                            needRefreshListData = true;
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.chat_fragment, container, false);

        return mBaseView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        mChatInfo = (ChatInfo) bundle.getSerializable(Constants.CHAT_INFO);
        if (!StringUtils.isEmpty(GroupMemberListActivity.name)) {
            String editStr = mChatLayout.getInputLayout().getInputText().getText().toString();
            String text = editStr + GroupMemberListActivity.name;
            mChatLayout.getInputLayout().getInputText().setText(text);
            mChatLayout.getInputLayout().getInputText().setSelection(text.length());
            GroupMemberListActivity.name = "";
        }
        if (mChatInfo == null) {
            return;
        }
        initView(false);
        //        if (mChatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
        //            initView(false);
        //        } else {
        //            checkGroupInfoChange();
        //        }
        if (mChatLayout != null) {
            mChatLayout.clearSelectedStatus();
        }
        if (needRefreshListData) {
            new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChatLayout.loadChatMessages();
                    needRefreshListData = false;
                }
            }, 1000);
        }
    }

    /**
     * 强制刷新界面及数据状态
     *
     * @param force
     */
    private void initView(final boolean force) {
        if (!force && mChatLayout != null) {
            return;
        }
        //从布局文件中获取聊天面板组件
        mChatLayout = mBaseView.findViewById(R.id.chat_layout);

        //单聊组件的默认UI和交互初始化
        //        mChatLayout.setChat_im_state_left("已接警");
        //        mChatLayout.setChat_im_state_right("[6级]");

        /*
         * 需要聊天的基本信息
         */
        mChatLayout.setChatInfo(mChatInfo);
        mChatLayout.initDefault();
        if (mChatInfo.getType() != V2TIMConversation.V2TIM_C2C) {
            final GroupInfo groupInfo = new GroupInfo();
            groupInfo.setId(mChatInfo.getId());
            groupInfo.setChatName(mChatInfo.getChatName());
            GroupInfoProvider.getGroupCustom(groupInfo, new IUIKitCallBack() {
                @Override
                public void onSuccess(Object data) {
                    //                    initEventId(groupInfo);
                    checkYJYAIM(groupInfo.getId());
                    intInputStyle(force);
                    ImHelper.setGroupName(groupInfo.getChatName());
                    //                    if (noNoticeLayoutEventType.contains(mEventType)) {
                    //                        mChatLayout.setAlarmTitleInfoGone();
                    //                    }
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    intInputStyle(force);
                }
            });
        }
        //获取单聊面板的标题栏
        mTitleBar = mChatLayout.getTitleBar();

        //单聊面板标记栏返回按钮点击事件，这里需要开发者自行控制
        //        mTitleBar.setOnLeftClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                getActivity().finish();
        //            }
        //        });
        if (mChatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
            mChatLayout.setAlarmTitleInfoGone();
            ImHelper.eventId = "";
            ImHelper.event_type = "";
            mTitleBar.setRightHide();
            mTitleBar.getRightIcon().setVisibility(View.GONE);
            intInputStyle(force);
            String id = mChatInfo.getId();
            if (id.startsWith(Func.FUNC_PREFIX)) {
                ImHelper.setUIListener(this);
                ArrayList<FunctionBtnData> functionBtnData = mFunctionBarCache.get(id);
                if (functionBtnData == null) {
                    if (ImHelper.getDBXSendReq() != null) {
                        ImHelper.getDBXSendReq().getBottomFunctionBarData(id);
                    }
                } else {
                    refreshBottomFunctionBar(functionBtnData);
                }
            }
        }
        mChatLayout.getMessageLayout().setOnItemClickListener(new MessageLayout.OnItemClickListener() {
            @Override
            public void onMessageLongClick(View view, int position, MessageInfo messageInfo) {
                //因为adapter中第一条为加载条目，位置需减1
                mChatLayout.getMessageLayout().showItemPopMenu(position - 1, messageInfo, view);
            }

            @Override
            public void onUserIconClick(View view, int position, MessageInfo messageInfo) {
                if (null == messageInfo) {
                    return;
                }
                ChatInfo info = new ChatInfo();
                info.setId(messageInfo.getFromUser());
            }
        });

    }

    private void checkYJYAIM(String id) {
        if (ImHelper.getDBXSendReq().checkYJYAIMLayout(id)) {
            mChatLayout.setYJYATitleInfo();
        }
    }

    private void intInputStyle(boolean force) {
        if (!force && mChatLayout.getInputLayout().getInputText() != null) {
            //输入内容为空时初始化
            if (StringUtils.isEmpty(mChatLayout.getInputLayout().getInputText().getText().toString())) {
                initChatLayoutHelper();
            }
        } else {
            initChatLayoutHelper();
        }
    }

    /**
     * 初始化输入栏功能区
     */
    private void initChatLayoutHelper() {
        ChatLayoutHelper helper = new ChatLayoutHelper(getActivity());
        helper.customizeChatLayout(mChatLayout, ImHelper.event_type);
    }

    @Override
    public void onPause() {
        super.onPause();
        AudioPlayer.getInstance().stopPlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatLayout != null) {
            mChatLayout.exitChat();
        }
        mFunctionBarCache.clear();
    }

    private void initEventId(GroupInfo groupInfo) {
        mEventType = groupInfo.getEvent_group_type();
        String mCompanyId = groupInfo.getCompany_id();
        if (mEventType != null && !mEventType.isEmpty()) {
            String id = groupInfo.getId();
            ImHelper.setUIListener(this);
            ImHelper.setEventInfo(mEventType, id, mCompanyId);
        } else {
            mEventType = EventType.TYPE_NORMAL;
            ImHelper.setEventInfo(EventType.TYPE_NORMAL, groupInfo.getId(), mCompanyId);
        }
    }

    /**
     * 检查群类型是否有变化有变化是刷新布局及状态
     */
    private void checkGroupInfoChange() {
        final GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(mChatInfo.getId());
        groupInfo.setChatName(mChatInfo.getChatName());
        GroupInfoProvider.getGroupCustom(groupInfo, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                String eventType = groupInfo.getEvent_group_type();
                if (StringUtils.isNotEmpty(eventType) && !mEventType.equals(eventType)) {
                    initView(true);
                } else {
                    initView(false);
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                initView(true);
            }
        });
    }

    @Override
    public void setEventTitleData(String eventType, int status, int level, String yaylStartTime,
                                  long yaylTime) {
        if (!TextUtils.equals(eventType, mEventType)) {
            return;
        }
        if (mChatLayout == null) {
            if (mChatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
                mChatLayout.setAlarmTitleInfoGone();
            }
            return;
        }
        mChatLayout.setAlarmTitleInfo(eventType, status, level, yaylStartTime, yaylTime);
    }

    @Override
    public void refreshFunctionLayout() {
        initChatLayoutHelper();
    }

    @Override
    public void refreshBottomFunctionBar(ArrayList<FunctionBtnData> data) {
        mFunctionBarCache.put(mChatInfo.getId(), data);
        mChatLayout.getInputLayout().showFunctionBar(data);
    }

    public boolean onBackPressed() {
        if (mChatLayout.jiaojie_layout.getVisibility() == View.VISIBLE) {
            mChatLayout.hideJiaoJieState();
            return true;
        }
        return false;
    }
}
