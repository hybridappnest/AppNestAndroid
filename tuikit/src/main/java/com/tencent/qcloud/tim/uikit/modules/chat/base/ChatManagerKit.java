package com.tencent.qcloud.tim.uikit.modules.chat.base;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMFriendInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageReceipt;
import com.tencent.imsdk.v2.V2TIMOfflinePushInfo;
import com.tencent.imsdk.v2.V2TIMSendCallback;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.config.TUIKitConfigs;
import com.tencent.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.tencent.qcloud.tim.uikit.modules.message.MessageRevokedManager;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.user.UserInfoDB;
import com.ymy.core.user.YmyUserManager;
import com.ymy.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ChatManagerKit extends V2TIMAdvancedMsgListener implements MessageRevokedManager.MessageRevokeHandler {

    protected static final int MSG_PAGE_COUNT = 20;
    protected static final int REVOKE_TIME_OUT = 6223;
    private static final String TAG = ChatManagerKit.class.getSimpleName();
    public static ChatProvider mCurrentProvider;

    protected boolean mIsMore;
    private boolean mIsLoading;

    protected void init() {
        destroyChat();
        V2TIMManager.getMessageManager().addAdvancedMsgListener(this);
        MessageRevokedManager.getInstance().addHandler(this);
    }

    public void destroyChat() {
        mCurrentProvider = null;
    }

    public void onReadReport(List<V2TIMMessageReceipt> receiptList) {
        TUIKitLog.i(TAG, "onReadReport:" + receiptList.size());
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "onReadReport unSafetyCall");
            return;
        }
        if (receiptList.size() == 0) {
            return;
        }
        V2TIMMessageReceipt max = receiptList.get(0);
        for (V2TIMMessageReceipt msg : receiptList) {
            if (!TextUtils.equals(msg.getUserID(), getCurrentChatInfo().getId())) {
                continue;
            }
            if (max.getTimestamp() < msg.getTimestamp()) {
                max = msg;
            }
        }
        mCurrentProvider.updateReadMessage(max);
    }

    protected boolean safetyCall() {
        if (mCurrentProvider == null
                || getCurrentChatInfo() == null) {
            return false;
        }
        return true;
    }

    public abstract ChatInfo getCurrentChatInfo();

    public void setCurrentChatInfo(ChatInfo info) {
        if (info == null) {
            return;
        }
        mCurrentProvider = new ChatProvider();
        mIsMore = true;
        mIsLoading = false;
    }

    //    /**
    //     * 检测是否有弹窗内容
    //     *
    //     * @param timMessage
    //     */
    //    private void checkInfo(V2TIMMessage timMessage) {
    //        if (timMessage == null
    //                || timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_HAS_DELETED
    //                || timMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_NONE) {
    //            TUIKitLog.e(TAG, "ele2MessageInfo parameters error");
    //            return;
    //        }
    //        int type = timMessage.getElemType();
    //        if (type == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
    //            V2TIMCustomElem customElem = timMessage.getCustomElem();
    //            String data = new String(customElem.getData());
    //            if (data.equals(MessageCustom.BUSINESS_ID_GROUP_CREATE)) {
    //            } else {
    //                Gson gson = new Gson();
    //                MessageCustom messageCustom = null;
    //                try {
    //                    messageCustom = gson.fromJson(data, MessageCustom.class);
    //                    if (messageCustom != null) {
    //                        String mark = messageCustom.getMark();
    //                        if ("1".equals(mark)) {
    //                            ImHelper.getDBXSendReq().showAlarmDialog(mark, messageCustom
    //                            .getContent());
    //                        }
    //                        if ("2".equals(mark)) {
    //                            MessageCustom.Params params = messageCustom.getAction().params;
    //                            String groupId = messageCustom.getAction().params.getImGroupId();
    //                            YAYLAlarmInfo yaylAlarmInfo = new YAYLAlarmInfo(messageCustom
    //                            .getTitle(), params.getAddress(), params.getCreatedTime(),
    //                            groupId);
    //                            ImHelper.getDBXSendReq().showAlarmDialog(mark, yaylAlarmInfo);
    //                        }
    //                    }
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        }
    //        if(type == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT){
    //            V2TIMTextElem txtEle = timMessage.getTextElem();
    //            String text = txtEle.getText();
    //            if(text.contains("主机信号")){
    //                BeepHelper beepHelper = new BeepHelper(KtxManager.getCurrentActivity(), com
    //                .yuanmanyuan.core.R.raw.ping2);
    //                beepHelper.initVoice();
    //                beepHelper.playBeepSoundAndVibrate();
    //            }
    //        }
    //    }

    @Override
    public void onRecvNewMessage(V2TIMMessage msg) {
        TUIKitLog.i(TAG, "onRecvNewMessage msgID:" + msg.getMsgID());
        int elemType = msg.getElemType();
        if (elemType == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
            if (MessageInfoUtil.isTyping(msg.getCustomElem().getData())) {
                notifyTyping();
                return;
            } else if (MessageInfoUtil.isOnlineIgnoredDialing(msg.getCustomElem().getData())) {
                // 这类消息都是音视频通话邀请的在线消息，忽略
                TUIKitLog.i(TAG, "ignore online invitee message");
                return;
            }
        }
        onReceiveMessage(msg);
        //        checkInfo(msg);
    }

    private void notifyTyping() {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "notifyTyping unSafetyCall");
            return;
        }
        mCurrentProvider.notifyTyping();
    }

    public void notifyNewFriend(List<V2TIMFriendInfo> timFriendInfoList) {
        if (timFriendInfoList == null || timFriendInfoList.size() == 0) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("已和");
        for (V2TIMFriendInfo v2TIMFriendInfo : timFriendInfoList) {
            stringBuilder.append(v2TIMFriendInfo.getUserID()).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("成为好友");
        ToastUtil.toastLongMessage(stringBuilder.toString());
    }

    protected void onReceiveMessage(final V2TIMMessage msg) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "onReceiveMessage unSafetyCall");
            return;
        }
        addMessage(msg);
    }

    protected void addMessage(V2TIMMessage msg) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "addMessage unSafetyCall");
            return;
        }
        final List<MessageInfo> list = MessageInfoUtil.TIMMessage2MessageInfo(msg);
        if (list != null && list.size() != 0) {
            ChatInfo chatInfo = getCurrentChatInfo();
            boolean isGroupMessage = false;
            String groupID = null;
            String userID = null;
            if (!TextUtils.isEmpty(msg.getGroupID())) {
                // 群组消息
                if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C
                        || !chatInfo.getId().equals(msg.getGroupID())) {
                    return;
                }
                isGroupMessage = true;
                groupID = msg.getGroupID();
            } else if (!TextUtils.isEmpty(msg.getUserID())) {
                // C2C 消息
                if (chatInfo.getType() == V2TIMConversation.V2TIM_GROUP
                        || !chatInfo.getId().equals(msg.getUserID())) {
                    return;
                }
                userID = msg.getUserID();
            } else {
                return;
            }
            mCurrentProvider.addMessageInfoList(list);
            //防止一次收到多条系统消息，造成不必要的刷新数据请求
            //            boolean needRefreshGroupEventInfo = false;
            //            String changeGroupEventType = "";
            //            for (MessageInfo msgInfo : list) {
            //                msgInfo.setRead(true);
            //                addGroupMessage(msgInfo);
            //                if (isGroupMessage
            //                        && groupID.equals(ImHelper.groupId)
            //                        && DBXConstants.ADMINISTRATOR_USERNAME.equals(msgInfo
            //                        .getFromUser())) {
            //                    try {
            //                        V2TIMCustomElem customElem = msg.getCustomElem();
            //                        String data = new String(customElem.getData());
            //                        Gson gson = new Gson();
            //                        MessageCustom messageCustom = gson.fromJson(data,
            //                        MessageCustom.class);
            //                        String changeEventType = messageCustom.getAction()
            //                        .getChangeEventType();
            //                        if (StringUtils.isNotEmpty(changeEventType) && ImHelper
            //                        .EventTypeList.contains(changeEventType)) {
            //                            changeGroupEventType = changeEventType;
            //                            needRefreshGroupEventInfo = true;
            //                            continue;
            //                        }
            //                    } catch (Exception e) {
            //                        e.printStackTrace();
            //                    }
            //                    needRefreshGroupEventInfo = true;
            //                }
            //            }
            //            if (needRefreshGroupEventInfo) {
            //                if (StringUtils.isNotEmpty(changeGroupEventType) && ImHelper
            //                .EventTypeList.contains(changeGroupEventType)) {
            //                    ImHelper.changeGroupEventTypeAndGetInfo(changeGroupEventType);
            //                } else {
            //                    ImHelper.checkGroupEventTypeAndGetInfo();
            //                }
            //            }
            if (isGroupMessage) {
                V2TIMManager.getMessageManager().markGroupMessageAsRead(groupID,
                        new V2TIMCallback() {
                            @Override
                            public void onSuccess() {
                                TUIKitLog.i(TAG, "addMessage() markGroupMessageAsRead success");
                            }

                            @Override
                            public void onError(int code, String desc) {
                                TUIKitLog.e(TAG,
                                        "addMessage() markGroupMessageAsRead failed, code = " + code + "," +
                                                " desc = " + desc);
                            }
                        });
            } else {
                V2TIMManager.getMessageManager().markC2CMessageAsRead(userID, new V2TIMCallback() {
                    @Override
                    public void onSuccess() {
                        TUIKitLog.i(TAG, "addMessage() markC2CMessageAsRead success");
                    }

                    @Override
                    public void onError(int code, String desc) {
                        TUIKitLog.e(TAG,
                                "addMessage() markC2CMessageAsRead failed, code = " + code + ", " +
                                        "desc = " + desc);
                    }
                });
            }
        }
    }

    protected void addGroupMessage(MessageInfo msgInfo) {
        // GroupChatManagerKit会重写该方法
    }

    public void deleteMessage(int position, MessageInfo messageInfo) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "deleteMessage unSafetyCall");
            return;
        }
        if (messageInfo.remove()) {
            mCurrentProvider.remove(position);
        }
    }

    public void revokeMessage(final int position, final MessageInfo messageInfo) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "revokeMessage unSafetyCall");
            return;
        }
        V2TIMManager.getMessageManager().revokeMessage(messageInfo.getTimMessage(),
                new V2TIMCallback() {
                    @Override
                    public void onSuccess() {
                        if (!safetyCall()) {
                            TUIKitLog.w(TAG, "revokeMessage unSafetyCall");
                            return;
                        }
                        mCurrentProvider.updateMessageRevoked(messageInfo.getId());
                        ConversationManagerKit.getInstance().loadConversation(null);
                    }

                    @Override
                    public void onError(int code, String desc) {
                        if (code == REVOKE_TIME_OUT) {
                            ToastUtil.toastLongMessage("消息发送已超过2分钟");
                        } else {
                            ToastUtil.toastLongMessage("撤回失败:" + code + "=" + desc);
                        }
                    }
                });
    }

    public void loadChatMessages(MessageInfo lastMessage, final IUIKitCallBack callBack) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "loadLocalChatMessages unSafetyCall");
            return;
        }
        if (mIsLoading) {
            return;
        }
        mIsLoading = true;
        if (!mIsMore && lastMessage !=null) {
            mCurrentProvider.addMessageInfo(null);
            callBack.onSuccess(null);
            mIsLoading = false;
            return;
        }

        V2TIMMessage lastTIMMsg = null;
        if (lastMessage == null) {
            mCurrentProvider.clear();
        } else {
            lastTIMMsg = lastMessage.getTimMessage();
        }
        //        final int unread = (int) mCurrentConversation.getUnreadMessageNum();
        final ChatInfo chatInfo = getCurrentChatInfo();
        if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
            V2TIMManager.getMessageManager().getC2CHistoryMessageList(chatInfo.getId(),
                    MSG_PAGE_COUNT, lastTIMMsg, new V2TIMValueCallback<List<V2TIMMessage>>() {
                        @Override
                        public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                            processHistoryMsgs(v2TIMMessages, chatInfo, callBack);
                        }

                        @Override
                        public void onError(int code, String desc) {
                            mIsLoading = false;
                            callBack.onError(TAG, code, desc);
                            TUIKitLog.e(TAG,
                                    "loadChatMessages getC2CHistoryMessageList failed, code = " + code +
                                            ", desc = " + desc);
                        }
                    });
        } else {
            V2TIMManager.getMessageManager().getGroupHistoryMessageList(chatInfo.getId(),
                    MSG_PAGE_COUNT, lastTIMMsg, new V2TIMValueCallback<List<V2TIMMessage>>() {
                        @Override
                        public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                            processHistoryMsgs(v2TIMMessages, chatInfo, callBack);
                        }

                        @Override
                        public void onError(int code, String desc) {
                            mIsLoading = false;
                            callBack.onError(TAG, code, desc);
                            TUIKitLog.e(TAG, "loadChatMessages getGroupHistoryMessageList failed," +
                                    " code = "
                                    + code + ", desc = " + desc);
                        }
                    });
        }
    }

    private void processHistoryMsgs(List<V2TIMMessage> v2TIMMessages, ChatInfo chatInfo,
                                    IUIKitCallBack callBack) {
        mIsLoading = false;
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "getLocalMessage unSafetyCall");
            return;
        }
        if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
            V2TIMManager.getMessageManager().markC2CMessageAsRead(chatInfo.getId(),
                    new V2TIMCallback() {
                        @Override
                        public void onSuccess() {
                            TUIKitLog.d(TAG, "processHistoryMsgs setReadMessage success");
                        }

                        @Override
                        public void onError(int code, String desc) {
                            TUIKitLog.e(TAG,
                                    "processHistoryMsgs setReadMessage failed, code = " + code +
                                            ", desc " +
                                            "= " + desc);
                        }
                    });
        } else {
            V2TIMManager.getMessageManager().markGroupMessageAsRead(chatInfo.getId(),
                    new V2TIMCallback() {
                        @Override
                        public void onSuccess() {
                            TUIKitLog.d(TAG, "processHistoryMsgs markC2CMessageAsRead success");
                        }

                        @Override
                        public void onError(int code, String desc) {
                            TUIKitLog.e(TAG,
                                    "processHistoryMsgs markC2CMessageAsRead failed, code = " + code + "," +
                                            " desc = " + desc);
                        }
                    });
        }


        if (v2TIMMessages.size() < MSG_PAGE_COUNT) {
            mIsMore = false;
        }
        ArrayList<V2TIMMessage> messages = new ArrayList<>(v2TIMMessages);
        Collections.reverse(messages);

        List<MessageInfo> msgInfos = MessageInfoUtil.TIMMessages2MessageInfos(messages, isGroup());
        mCurrentProvider.addMessageList(msgInfos, true);
        for (int i = 0; i < msgInfos.size(); i++) {
            MessageInfo info = msgInfos.get(i);
            if (info.getStatus() == MessageInfo.MSG_STATUS_SENDING) {
                sendMessage(info, true, null);
            }
        }
        callBack.onSuccess(mCurrentProvider);
    }

    protected abstract boolean isGroup();

    public void sendMessage(final MessageInfo message, boolean retry,
                            final IUIKitCallBack callBack) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "sendMessage unSafetyCall");
            return;
        }
        if (message == null || message.getStatus() == MessageInfo.MSG_STATUS_SENDING) {
            return;
        }
        if (message.getSendUserName().isEmpty()) {
            UserInfoDB user = YmyUserManager.INSTANCE.getUser();
            message.setSendUserName(user.getNickname().isEmpty() ? user.getRealName() :
                    user.getNickname());
        }
        message.setSelf(true);
        message.setRead(true);
        assembleGroupMessage(message);

        //        OfflineMessageContainerBean containerBean = new OfflineMessageContainerBean();
        //        OfflineMessageBean entity = new OfflineMessageBean();
        //        entity.content = message.getExtra().toString();
        //        entity.sender = message.getFromUser();
        //        entity.nickname = StringUtils.isEmpty(message.getGroupNameCard()) ?
        //        TUIKitConfigs.getConfigs().getGeneralConfig().getUserNickname() : message
        //        .getGroupNameCard();
        //        entity.faceUrl = TUIKitConfigs.getConfigs().getGeneralConfig().getUserFaceUrl();
        //        containerBean.entity = entity;

        String userID = "";
        String groupID = "";
        boolean isGroup = false;
        if (getCurrentChatInfo().getType() == V2TIMConversation.V2TIM_GROUP) {
            groupID = getCurrentChatInfo().getId();
            isGroup = true;
            //            entity.chatType = V2TIMConversation.V2TIM_GROUP;
            //            entity.sender = groupID;
        } else {
            userID = getCurrentChatInfo().getId();
        }

        V2TIMOfflinePushInfo v2TIMOfflinePushInfo = new V2TIMOfflinePushInfo();
        v2TIMOfflinePushInfo.setAndroidOPPOChannelID("dbxIM");
        if (!message.isGroup()) {
            //            v2TIMOfflinePushInfo.setExt(new Gson().toJson(containerBean).getBytes());
            String userName = message.getSendUserName();
            v2TIMOfflinePushInfo.setTitle(userName);
            v2TIMOfflinePushInfo.setDesc(message.getExtra().toString());
        } else {
            // OPPO必须设置ChannelID才可以收到推送消息，这个channelID需要和控制台一致
            String title = StringUtils.isEmpty(message.getGroupNameCard()) ?
                    TUIKitConfigs.getConfigs().getGeneralConfig().getUserNickname() :
                    message.getGroupNameCard();
            TUIKitLog.v(TAG, "sendMessage title   =  :" + title);
            v2TIMOfflinePushInfo.setTitle(title);
            String userName = message.getSendUserName();
            if (!StringUtils.isEmpty(userName) && message.isGroup()) {
                v2TIMOfflinePushInfo.setDesc(userName + "：" + message.getExtra().toString());
            } else {
                v2TIMOfflinePushInfo.setDesc(message.getExtra().toString());
            }
        }
        V2TIMMessage v2TIMMessage = message.getTimMessage();
        String finalUserID = userID;
        String msgID = V2TIMManager.getMessageManager().sendMessage(v2TIMMessage, isGroup ? null
                        : userID, isGroup ? groupID : null,
                V2TIMMessage.V2TIM_PRIORITY_DEFAULT, false, v2TIMOfflinePushInfo,
                new V2TIMSendCallback<V2TIMMessage>() {
                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onSuccess(V2TIMMessage v2TIMMessage) {
//                        Logger.e("sendId:" + YmyUserManager.INSTANCE.getUser().getImId() + "\n" +
//                                "sendTime:" + StringUtils.toMD_HM(System.currentTimeMillis()) +
//                                "\n" +
//                                "receiver:" + finalUserID + "\n" +
//                                "v2TIMMessage:v2TIMMessage.getTextElem().getText()" + v2TIMMessage.getTextElem().getText() + "\n" +
//                                "v2TIMOfflinePushInfo:title " + v2TIMOfflinePushInfo.getTitle() + "\n" +
//                                "v2TIMOfflinePushInfo:desc " + v2TIMOfflinePushInfo.getDesc() + "\n"
//                        );
                        TUIKitLog.v(TAG, "sendMessage onSuccess:" + v2TIMMessage.getMsgID());
                        if (!safetyCall()) {
                            TUIKitLog.w(TAG, "sendMessage unSafetyCall");
                            return;
                        }
                        int type = v2TIMMessage.getElemType();
                        if (type == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {//
                            V2TIMCustomElem customElem = v2TIMMessage.getCustomElem();
                            String data = new String(customElem.getData());
                            Gson gson = new Gson();
                            MessageCustom messageCustom = null;
                            try {
                                messageCustom = gson.fromJson(data, MessageCustom.class);
                                if (!TextUtils.isEmpty(messageCustom.getLocalPath())) {//判断语音文件
                                    if (TextUtils.isEmpty(messageCustom.getRemoteUrl())) {
                                        message.setStatus(MessageInfo.MSG_STATUS_SEND_FAIL);
                                        mCurrentProvider.updateMessageInfo(message);
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (callBack != null) {
                            callBack.onSuccess(mCurrentProvider);
                        }
                        message.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                        mCurrentProvider.updateMessageInfo(message);
                    }

                    @Override
                    public void onError(int code, String desc) {
                        TUIKitLog.v(TAG, "sendMessage fail:" + code + "=" + desc);
                        if (!safetyCall()) {
                            TUIKitLog.w(TAG, "sendMessage unSafetyCall");
                            return;
                        }
                        if (callBack != null) {
                            callBack.onError(TAG, code, desc);
                        }
                        message.setStatus(MessageInfo.MSG_STATUS_SEND_FAIL);
                        mCurrentProvider.updateMessageInfo(message);
                    }
                });

        //消息先展示，通过状态来确认发送是否成功
        TUIKitLog.i(TAG, "sendMessage msgID:" + msgID);
        message.setId(msgID);
        if (message.getMsgType() < MessageInfo.MSG_TYPE_TIPS) {
            message.setStatus(MessageInfo.MSG_STATUS_SENDING);
            if (retry) {
                mCurrentProvider.resendMessageInfo(message);
            } else {
                mCurrentProvider.addMessageInfo(message);
            }
        }
    }

    protected void assembleGroupMessage(MessageInfo message) {
        // GroupChatManager会重写该方法
    }

    @Override
    public void handleInvoke(String msgID) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "handleInvoke unSafetyCall");
            return;
        }
        TUIKitLog.i(TAG, "handleInvoke msgID = " + msgID);
        mCurrentProvider.updateMessageRevoked(msgID);
    }
}
