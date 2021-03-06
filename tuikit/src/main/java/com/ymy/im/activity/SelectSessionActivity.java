package com.ymy.im.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.google.gson.Gson;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.jeremyliao.liveeventbus.core.LiveEvent;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMImageElem;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMOfflinePushInfo;
import com.tencent.imsdk.v2.V2TIMSendCallback;
import com.tencent.imsdk.v2.V2TIMTextElem;
import com.tencent.imsdk.v2.V2TIMVideoElem;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.config.TUIKitConfigs;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.chat.base.OfflineMessageBean;
import com.tencent.qcloud.tim.uikit.modules.chat.base.OfflineMessageContainerBean;
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.tencent.qcloud.tim.uikit.utils.ImageUtil;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.base.RootActivity;
import com.ymy.core.utils.StringUtils;
import com.ymy.core.utils.ToastUtils;
import com.ymy.core.view.DBXLoadingView;
import com.ymy.im.fragment.SessionFragment;
import com.ymy.im.helper.ImHelper;
import com.ymy.im.signature.Menu;
import com.ymy.im.utils.CommenDialog;

import java.io.File;
import java.util.List;

import static com.tencent.qcloud.tim.uikit.modules.chat.base.ChatManagerKit.mCurrentProvider;

/**
 * ????????????????????????
 *
 * @author hanxueqiang
 */
public class SelectSessionActivity extends RootActivity implements OnClickListener {

    public static List<MessageInfo> mDataSource;
    CommenDialog dialog;
    boolean isSend = false;
    boolean zhuanfaState = false;
    private ConversationInfo temp;
    private TextView mBtnCancel;
    private RelativeLayout mTitle;
    private ImageView mRightIcon;
    private SessionFragment sessionFragment;
    private Menu mMenu;
    private DBXLoadingView loadingView;
    public SelectSessionActivity.SelectSessionCallBack mSelectSessionCallBack =
            new SelectSessionCallBack() {

                /**
                 * ????????????
                 *
                 * @param object
                 */
                @Override
                public void selectSession(Object object) {
                    if (object instanceof ConversationInfo) {
                        if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
                            sendMessageDialog((ConversationInfo) object);
                        } else {
                            temp = (ConversationInfo) object;
                        }
                    } else if (object instanceof ChatInfo) {
                        ChatInfo chatInfo = (ChatInfo) object;
                        boolean isGroup = chatInfo.getType() == V2TIMConversation.V2TIM_GROUP;
                        ConversationInfo conversationInfo = new ConversationInfo();
                        conversationInfo.setId(chatInfo.getId());
                        conversationInfo.setType(chatInfo.getType());
                        conversationInfo.setTitle(chatInfo.getChatName());
                        conversationInfo.setGroup(isGroup);
                        if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
                            sendMessageDialog(conversationInfo);
                        } else {
                            temp = conversationInfo;
                        }
                    } else {
                        throw new IllegalArgumentException("??????????????????");
                    }
                }
            };

    public static void invoke(Context context, List<MessageInfo> source) {
        mDataSource = source;
        Intent intent = new Intent(context, SelectSessionActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_session_activity);
        init();
    }

    private void init() {
        mBtnCancel = (TextView) findViewById(R.id.search_cancel);
        FrameLayout loadRoot = (FrameLayout) findViewById(R.id.loadingroot);
        initLoading(loadRoot);
        mBtnCancel.setOnClickListener(this);
        sessionFragment = new SessionFragment();
        sessionFragment.setSelectSessionCallBack(mSelectSessionCallBack);
        getSupportFragmentManager().beginTransaction().replace(R.id.empty_view, sessionFragment).commitAllowingStateLoss();

        mTitle = (RelativeLayout) findViewById(R.id.search_title);
        mMenu = new Menu(this, mTitle, Menu.MENU_TYPE_CONVERSATION);
        mRightIcon = (ImageView) findViewById(R.id.page_title_right_icon);
        mRightIcon.setOnClickListener(this);
        ImHelper.setSelectSessionCallBack(mSelectSessionCallBack);
    }

    private void initLoading(FrameLayout rootView) {
        loadingView = new DBXLoadingView(rootView);
        loadingView.setLoadingText("?????????");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (temp != null) {
            sendMessageDialog(temp);
        }
    }

    @Override
    protected void onDestroy() {
        mDataSource = null;
        sessionFragment.setSelectSessionCallBack(null);
        ImHelper.setSelectSessionCallBack(null);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.search_cancel) {
            finish();
        }
        if (id == R.id.page_title_right_icon) {
            if (mMenu.isShowing()) {
                mMenu.hide();
            } else {
                mMenu.show();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    public void sendMessageDialog(final ConversationInfo contact) {
        if (isSend) {
            ToastUtil.toastShortMessage("????????????????????????...");
            return;
        }
        if (dialog == null) {
            dialog = new CommenDialog(this, "???????????????", -1, new CommenDialog.DialogCallBack() {
                @Override
                public void callBack(Object obj) {
                    isSend = true;
                    loadingView.show();
                    sendMessage(contact, 0);
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    temp = null;
                }
            });

        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void sendMessage(final ConversationInfo contact, final int postion) {
        MessageInfo messageInfo = mDataSource.get(postion);
        messageInfo = resetMessinfo(messageInfo);
        if (messageInfo == null) {
            ToastUtil.toastShortMessage("?????????????????????????????????");
            isSend = false;
            finish();
            return;
        }
        messageInfo.setSelf(true);
        messageInfo.setRead(true);
        OfflineMessageContainerBean containerBean = new OfflineMessageContainerBean();
        OfflineMessageBean entity = new OfflineMessageBean();
        entity.content = messageInfo.getExtra().toString();
        entity.sender = messageInfo.getFromUser();
        entity.nickname = TUIKitConfigs.getConfigs().getGeneralConfig().getUserNickname();
        entity.faceUrl = TUIKitConfigs.getConfigs().getGeneralConfig().getUserFaceUrl();
        containerBean.entity = entity;

        String userID = "";
        String groupID = "";
        boolean isGroup = false;
        if (contact.isGroup()) {
            groupID = contact.getId();
            isGroup = true;
            messageInfo.setGroup(true);
            entity.chatType = V2TIMConversation.V2TIM_GROUP;
            entity.sender = groupID;
        } else {
            messageInfo.setGroup(false);
            userID = contact.getId();
        }
        V2TIMOfflinePushInfo v2TIMOfflinePushInfo = new V2TIMOfflinePushInfo();
        v2TIMOfflinePushInfo.setExt(new Gson().toJson(containerBean).getBytes());
        // OPPO????????????ChannelID????????????????????????????????????channelID????????????????????????
        v2TIMOfflinePushInfo.setDesc(messageInfo.getExtra().toString());
        v2TIMOfflinePushInfo.setAndroidOPPOChannelID("tuikit");

        V2TIMMessage v2TIMMessage = messageInfo.getTimMessage();
        final MessageInfo finalMessageInfo = messageInfo;
        String msgID = V2TIMManager.getMessageManager().sendMessage(v2TIMMessage, isGroup ? null
                        : userID, isGroup ? groupID : null,
                V2TIMMessage.V2TIM_PRIORITY_DEFAULT, false, v2TIMOfflinePushInfo,
                new V2TIMSendCallback<V2TIMMessage>() {
                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onSuccess(V2TIMMessage v2TIMMessage) {
                        TUIKitLog.e(ImHelper.TAG, "sendMessage   onSuccess: ");
                        if (mCurrentProvider != null) {
                            finalMessageInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                            mCurrentProvider.updateMessageInfo(finalMessageInfo);
                        }
                        zhuanfaState = true;
                        if (mDataSource != null && mDataSource.size() > (postion + 1)) {
                            sendMessage(contact, postion + 1);
                        } else {
                            loadingView.hide();
                            ToastUtils.showImageToast(SelectSessionActivity.this, "????????????", true);
                            new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendNoticeToRefreshChatList(contact.getId());
                                    finish();
                                }
                            },1000);
                        }
                    }

                    @Override
                    public void onError(int code, String desc) {
                        TUIKitLog.e(ImHelper.TAG, "sendMessage   onError: ");
                        if (mCurrentProvider != null) {
                            finalMessageInfo.setStatus(MessageInfo.MSG_STATUS_SEND_FAIL);
                            mCurrentProvider.updateMessageInfo(finalMessageInfo);
                        }
                        if (mDataSource != null && mDataSource.size() > (postion + 1)) {
                            sendMessage(contact, postion + 1);
                        } else {
                            if (!zhuanfaState) {
                                loadingView.hide();
                                ToastUtils.showImageToast(SelectSessionActivity.this, "????????????", true);
                            }
                            finish();
                        }
                    }
                });
        finalMessageInfo.setId(msgID);
    }

    private void sendNoticeToRefreshChatList(String id) {
        LiveEventBus.get(ChatRefreshNotice.class).post(new ChatRefreshNotice(id));
    }

    private MessageInfo resetMessinfo(MessageInfo msgInfo) {
        MessageInfo timMessage = resetTimMessage(msgInfo);
        if (timMessage == null) {
            return null;
        }
        timMessage.setFromUser(V2TIMManager.getInstance().getLoginUser());
        timMessage.setShowSelect(msgInfo.isShowSelect());
        timMessage.setMsgTime(timMessage.getTimMessage().getTimestamp());
        timMessage.setSelf(true);
        return timMessage;
    }

    private MessageInfo resetTimMessage(MessageInfo msgInfo) {
        V2TIMMessage timMessage = msgInfo.getTimMessage();
        int type = timMessage.getElemType();
        if (type == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
            V2TIMCustomElem customElem = timMessage.getCustomElem();
            String data = new String(customElem.getData());
            Gson gson = new Gson();
            MessageCustom messageCustom = null;
            try {
                messageCustom = gson.fromJson(data, MessageCustom.class);
                String msgViewType = messageCustom.getType();
                if (!TextUtils.isEmpty(msgViewType) && msgViewType.equals(MessageCustom.MessageViewType.FUNCTIONAL)) {
                    return MessageInfoUtil.buildCustomMessage(data);
                } else {
                    MessageCustom customAudioMessage = new MessageCustom();
                    String voicePath = "";
                    if (new File(messageCustom.getLocalPath()).exists()) {
                        voicePath = messageCustom.getLocalPath();
                    } else if (!TextUtils.isEmpty(messageCustom.getRemoteUrl())) {
                        String url = messageCustom.getRemoteUrl();
                        final String path =
                                TUIKitConstants.RECORD_DOWNLOAD_DIR + msgInfo.getFromUser() + "/";
                        final String name = url.substring(url.lastIndexOf("/") + 1);
                        File file = new File(path + name);
                        if (file.exists()) {
                            voicePath = file.getAbsolutePath();
                        }
                    }
                    if (StringUtils.isEmpty(voicePath)) {
                        return null;
                    }
                    customAudioMessage.setDiscernResult(messageCustom.getDiscernResult());
                    customAudioMessage.setDuration(messageCustom.getDuration());
                    customAudioMessage.setLocalPath(voicePath);
                    customAudioMessage.setRemoteUrl(messageCustom.getRemoteUrl());
                    String data1 = gson.toJson(customAudioMessage);
                    return MessageInfoUtil.buildCustomAudioMessage(data1);
                }
            } catch (Exception e) {
                TUIKitLog.e(ImHelper.TAG, "invalid json: " + data + ", exception:" + e);
            }
        } else {
            if (type == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT) {
                V2TIMTextElem txtEle = timMessage.getTextElem();
                return MessageInfoUtil.buildTextMessage(txtEle.getText());
            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE) {
                V2TIMImageElem imageEle = timMessage.getImageElem();
                List<V2TIMImageElem.V2TIMImage> imgs = imageEle.getImageList();
                if (!StringUtils.isEmpty(imageEle.getPath())) {
                    return MessageInfoUtil.buildImageMessage1(imageEle.getPath(), false);
                }
                for (int i = 0; i < imgs.size(); i++) {
                    V2TIMImageElem.V2TIMImage img = imgs.get(i);
                    if (img.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_THUMB) {
                        final String path = TUIKitConstants.IMAGE_DOWNLOAD_DIR + img.getUUID();
                        File file = new File(path);
                        if (file.exists()) {
                            return MessageInfoUtil.buildImageMessage1(path, false);
                        }
                    }
                }
            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO) {
                V2TIMVideoElem videoEle = timMessage.getVideoElem();
                if (msgInfo.isSelf() && !TextUtils.isEmpty(videoEle.getSnapshotPath())) {
                    int size[] = ImageUtil.getImageSize(videoEle.getSnapshotPath());
                    return MessageInfoUtil.buildVideoMessage(videoEle.getSnapshotPath(),
                            videoEle.getVideoPath(), size[0], size[1],
                            videoEle.getDuration() * 1000);
                } else {
                    final String videoPath =
                            TUIKitConstants.VIDEO_DOWNLOAD_DIR + videoEle.getVideoUUID();
                    final String snapPath =
                            TUIKitConstants.IMAGE_DOWNLOAD_DIR + videoEle.getSnapshotUUID();
                    //????????????????????????,?????????????????????
                    if (!new File(snapPath).exists()) {
                        return null;
                    }
                    return MessageInfoUtil.buildVideoMessage(snapPath, videoPath,
                            (int) videoEle.getSnapshotWidth(), (int) videoEle.getSnapshotHeight()
                            , videoEle.getDuration() * 1000);
                }
            }
        }
        return null;
    }

    public interface SelectSessionCallBack {
        /**
         * ????????????
         *
         * @param conversationInfo
         */
        void selectSession(Object conversationInfo);
    }

    public class ChatRefreshNotice implements LiveEvent {
        String chatId = "";

        public ChatRefreshNotice(String chatId) {
            this.chatId = chatId;
        }

        public String getChatId() {
            return chatId == null ? "" : chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }
    }

}
