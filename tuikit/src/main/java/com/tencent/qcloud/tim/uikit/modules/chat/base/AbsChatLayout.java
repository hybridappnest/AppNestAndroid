package com.tencent.qcloud.tim.uikit.modules.chat.base;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.ymy.activity.SelectSessionActivity;
import com.ymy.helper.ImHelper;
import com.ymy.utils.CommenDialog;
import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMImageElem;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMTextElem;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.imsdk.v2.V2TIMVideoElem;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.AudioPlayer;
import com.tencent.qcloud.tim.uikit.modules.chat.C2CChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.interfaces.IChatLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.interfaces.IChatProvider;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.input.InputLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout;
import com.tencent.qcloud.tim.uikit.modules.chat.layout.message.MessageListAdapter;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageType;
import com.tencent.qcloud.tim.uikit.modules.message.MessageUpload;
import com.tencent.qcloud.tim.uikit.utils.BackgroundTasks;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.user.YmyUserManager;
import com.ymy.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


public abstract class AbsChatLayout extends ChatLayoutUI implements IChatLayout {

    public MessageListAdapter mAdapter;
    List<MessageUpload> messageUploadList = new ArrayList<>();
    CommenDialog dialog;
    private AnimationDrawable mVolumeAnim;
    private Runnable mTypingRunnable = null;
    private ChatProvider.TypingListener mTypingListener = new ChatProvider.TypingListener() {
        @Override
        public void onTyping() {
            final String oldTitle = getTitleBar().getMiddleTitle().getText().toString();
            getTitleBar().getMiddleTitle().setText(R.string.typing);
            if (mTypingRunnable == null) {
                mTypingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        getTitleBar().getMiddleTitle().setText(oldTitle);
                    }
                };
            }
            getTitleBar().getMiddleTitle().removeCallbacks(mTypingRunnable);
            getTitleBar().getMiddleTitle().postDelayed(mTypingRunnable, 3000);
        }
    };

    public AbsChatLayout(Context context) {
        super(context);
    }

    public AbsChatLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsChatLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initListener() {
        getInputLayout().setAbsChatLayout(this);
        getMessageLayout().setPopActionClickListener(new MessageLayout.OnPopActionClickListener() {
            @Override
            public void onCopyClick(int position, MessageInfo msg) {
                ClipboardManager clipboard =
                        (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null || msg == null) {
                    return;
                }
                if (msg.getMsgType() == MessageInfo.MSG_TYPE_TEXT) {
                    V2TIMTextElem textElem = msg.getTimMessage().getTextElem();
                    ClipData clip = ClipData.newPlainText("message", textElem.getText());
                    clipboard.setPrimaryClip(clip);
                }
            }

            @Override
            public void onRepeatClick(int position, MessageInfo msg) {//转发
                List<MessageInfo> mDataSource = new ArrayList<>();
                msg.setShowSelect(false);
                mDataSource.add(msg);
                SelectSessionActivity.invoke(getContext(), mDataSource);
            }

            @Override
            public void onCopyMoreClick(int position, MessageInfo msg) {//多选
                mAdapter.uppDataState(true);
                more_select.setVisibility(View.VISIBLE);
                jiaojie_layout.setVisibility(GONE);
                getInputLayout().setVisibility(View.GONE);
            }

            @Override
            public void onSendMessageClick(MessageInfo msg, boolean retry) {
                sendMessage(msg, retry);
            }

            @Override
            public void onDeleteMessageClick(int position, MessageInfo msg) {
                deleteMessage(position, msg);
            }

            @Override
            public void onRevokeMessageClick(int position, MessageInfo msg) {
                revokeMessage(position, msg);
            }

            @Override
            public void onReportClick(int position, MessageInfo msg) {

            }
        });
        getMessageLayout().setLoadMoreMessageHandler(new MessageLayout.OnLoadMoreHandler() {
            @Override
            public void loadMore() {
                loadMessages();
            }
        });
        getMessageLayout().setEmptySpaceClickListener(new MessageLayout.OnEmptySpaceClickListener() {
            @Override
            public void onClick() {
                getInputLayout().hideSoftInput();
            }
        });

        /**
         * 设置消息列表空白处点击处理
         */
        getMessageLayout().addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    View child = rv.findChildViewUnder(e.getX(), e.getY());
                    if (child == null) {
                        getInputLayout().hideSoftInput();
                    } else if (child instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) child;
                        final int count = group.getChildCount();
                        float x = e.getRawX();
                        float y = e.getRawY();
                        View touchChild = null;
                        for (int i = count - 1; i >= 0; i--) {
                            final View innerChild = group.getChildAt(i);
                            int position[] = new int[2];
                            innerChild.getLocationOnScreen(position);
                            if (x >= position[0]
                                    && x <= position[0] + innerChild.getMeasuredWidth()
                                    && y >= position[1]
                                    && y <= position[1] + innerChild.getMeasuredHeight()) {
                                touchChild = innerChild;
                                break;
                            }
                        }
                        if (touchChild == null) {
                            getInputLayout().hideSoftInput();
                        }
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        getInputLayout().setChatInputHandler(new InputLayout.ChatInputHandler() {
            @Override
            public void onInputAreaClick() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        scrollToEnd();
                    }
                });
            }

            @Override
            public void onRecordStatusChanged(int status) {
                switch (status) {
                    case RECORD_START:
                        startRecording();
                        break;
                    case RECORD_STOP:
                        stopRecording();
                        break;
                    case RECORD_CANCEL:
                        cancelRecording();
                        break;
                    case RECORD_TOO_SHORT:
                    case RECORD_FAILED:
                        stopAbnormally(status);
                        break;
                    default:
                        break;
                }
            }

            private void startRecording() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        AudioPlayer.getInstance().stopPlay();
                        mRecordingGroup.setVisibility(View.VISIBLE);
                        mRecordingIcon.setImageResource(R.drawable.recording_volume);
                        mVolumeAnim = (AnimationDrawable) mRecordingIcon.getDrawable();
                        mVolumeAnim.start();
                        mRecordingTips.setTextColor(Color.WHITE);
                        mRecordingTips.setText("手指上滑，取消发送");
                    }
                });
            }

            private void stopRecording() {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mVolumeAnim.stop();
                        mRecordingGroup.setVisibility(View.GONE);
                    }
                }, 500);
            }

            private void cancelRecording() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mRecordingIcon.setImageResource(R.drawable.ic_volume_dialog_cancel);
                        mRecordingTips.setText("松开手指，取消发送");
                    }
                });
            }

            private void stopAbnormally(final int status) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mVolumeAnim.stop();
                        mRecordingIcon.setImageResource(R.drawable.ic_volume_dialog_length_short);
                        mRecordingTips.setTextColor(Color.WHITE);
                        if (status == RECORD_TOO_SHORT) {
                            mRecordingTips.setText("说话时间太短");
                        } else {
                            mRecordingTips.setText("录音失败");
                        }
                    }
                });
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRecordingGroup.setVisibility(View.GONE);
                    }
                }, 1000);
            }
        });
    }

    private void delete() {
        for (int i = 1; i < mAdapter.getItemCount(); i++) {
            MessageInfo msg = mAdapter.getItem(i);
            if (msg != null && msg.isSelect()) {
                deleteMessage(i - 1, msg);
                delete();
                return;
            }
        }
    }

    /**
     * 发送消息
     *
     * @param list
     * @param position
     */
    public void sendUploadMessage(final List<MessageInfo> list, final int position) {
        if (position >= list.size()) {
            hideJiaoJieState();
            // TODO: 2020/9/26 真实组织数据传参
            String str = "";
            ImHelper.getDBXSendReq().showHandover(messageUploadList);
            return;
        }
        MessageInfo messageInfo = list.get(position);
        final MessageUpload messageUpload = new MessageUpload();
        messageUpload.setFromAccount(messageInfo.getFromUser());
        messageUpload.setMsgTimeStamp(messageInfo.getMsgTime());
        if (messageInfo.getMsgType() == MessageInfo.MSG_TYPE_TEXT) {//文本
            V2TIMMessage timMessage = messageInfo.getTimMessage();
            final V2TIMTextElem imageEle = timMessage.getTextElem();
            messageUpload.setMsgType(MessageType.TEXT);
            MessageUpload.TextContent textContent = messageUpload.getTextContent();
            String text = imageEle.getText();
            textContent.setText(text);
            messageUpload.setTextContent(textContent);
        } else if (messageInfo.getMsgType() == MessageInfo.MSG_TYPE_IMAGE) {//图片
            messageUpload.setMsgType(MessageType.IMAGE);
            MessageUpload.ImageContent imageContent = messageUpload.getImageContent();
            V2TIMMessage timMessage = messageInfo.getTimMessage();
            final V2TIMImageElem imageEle = timMessage.getImageElem();
            final List<V2TIMImageElem.V2TIMImage> imgs = imageEle.getImageList();
            for (int i = 0; i < imgs.size(); i++) {
                final V2TIMImageElem.V2TIMImage img = imgs.get(i);
                if (img.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_ORIGIN) {
                    imageContent.setUrl(img.getUrl());
                    imageContent.setWidth(img.getWidth());
                    imageContent.setHeight(img.getHeight());
                }
                if (img.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_THUMB) {
                    imageContent.setThumbUrl(img.getUrl());
                    imageContent.setThumbWidth(img.getWidth());
                    imageContent.setThumbHeight(img.getHeight());
                }
            }
            messageUpload.setImageContent(imageContent);
        } else if (messageInfo.getMsgType() == MessageInfo.MSG_TYPE_VIDEO) {//视频
            messageUpload.setMsgType(MessageType.VIDEO);
            final MessageUpload.VideoContent videoContent = messageUpload.getVideoContent();
            V2TIMMessage timMessage = messageInfo.getTimMessage();
            final V2TIMVideoElem videoElem = timMessage.getVideoElem();
            videoContent.setWidth(videoElem.getSnapshotWidth());
            videoContent.setHeight(videoElem.getSnapshotHeight());
            videoElem.getSnapshotUrl(new V2TIMValueCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    videoContent.setSnapshotPath(s);
                    videoElem.getVideoUrl(new V2TIMValueCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            videoContent.setUrl(s);
                            messageUpload.setVideoContent(videoContent);
                            messageUploadList.add(messageUpload);
                            sendUploadMessage(list, position + 1);
                        }

                        @Override
                        public void onError(int i, String s) {
                            sendUploadMessage(list, position + 1);
                        }
                    });
                }

                @Override
                public void onError(int i, String s) {

                }
            });

            return;
        } else if (messageInfo.getMsgType() == MessageInfo.MSG_TYPE_AUDIO_CUSTOM) {
            messageUpload.setMsgType(MessageType.VOICE);
            MessageUpload.VoiceContent voiceContent = messageUpload.getVoiceContent();
            V2TIMCustomElem customElem = messageInfo.getTimMessage().getCustomElem();
            String data = new String(customElem.getData());
            MessageCustom messageCustom = null;
            try {
                Gson gson = new Gson();
                messageCustom = gson.fromJson(data, MessageCustom.class);
                int duration = messageCustom.getDuration();
                if (duration == 0) {
                    duration = 1;
                }
                voiceContent.setUrl(messageCustom.getRemoteUrl());
                voiceContent.setDiscernResult(messageCustom.getDiscernResult());
                voiceContent.setDuration(duration);
            } catch (Exception e) {
                e.printStackTrace();
            }
            messageUpload.setVoiceContent(voiceContent);
        } else if (messageInfo.getMsgType() == MessageInfo.MSG_TYPE_CUSTOM_BAOJING) {
            messageUpload.setMsgType(MessageType.CUSTOM);
            MessageUpload.CustomContent customContent = messageUpload.getCustomContent();
            V2TIMCustomElem customElem = messageInfo.getTimMessage().getCustomElem();
            String data = new String(customElem.getData());
            customContent.setData(data);
            messageUpload.setCustomContent(customContent);
        }
        messageUploadList.add(messageUpload);
        sendUploadMessage(list, position + 1);
    }

    public void showDelDialog() {
        if (dialog == null) {
            dialog = new CommenDialog(getContext(), "确认删除？", -1, new CommenDialog.DialogCallBack() {
                @Override
                public void callBack(Object obj) {
                    delete();
                }
            });
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void scrollToEnd() {
        getMessageLayout().scrollToEnd();
    }

    public void setDataProvider(IChatProvider provider) {
        if (provider != null) {
            ((ChatProvider) provider).setTypingListener(mTypingListener);
        }
        if (mAdapter != null) {
            boolean state = false;
            if (more_select.getVisibility() == View.VISIBLE) {
                state = true;
            }
            mAdapter.setDataSource(provider, state);
        }
    }

    public void loadChatMessages(final MessageInfo lastMessage) {
        getChatManager().loadChatMessages(lastMessage, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                if (lastMessage == null && data != null) {
                    setDataProvider((ChatProvider) data);
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //                ToastUtil.toastLongMessage(errMsg);
                if (lastMessage == null) {
                    setDataProvider(null);
                }
            }
        });
    }

    protected void deleteMessage(int position, MessageInfo msg) {
        getChatManager().deleteMessage(position, msg);
    }

    protected void revokeMessage(int position, MessageInfo msg) {
        getChatManager().revokeMessage(position, msg);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        exitChat();
    }

    @Override
    public void exitChat() {
        getTitleBar().getMiddleTitle().removeCallbacks(mTypingRunnable);
        AudioPlayer.getInstance().stopRecord();
        AudioPlayer.getInstance().stopPlay();
        if (getChatManager() != null) {
            getChatManager().destroyChat();
        }
    }

    @Override
    public void initDefault() {
        getTitleBar().getLeftGroup().setVisibility(View.VISIBLE);
        getTitleBar().setOnLeftClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (more_select.getVisibility() == View.VISIBLE) {
                    hideJiaoJieState();
                    return;
                }
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).finish();
                }
            }
        });
        getInputLayout().setMessageHandler(new InputLayout.MessageHandler() {
            @Override
            public void sendMessage(MessageInfo msg) {
                if (!ImHelper.currentGroupIsExist) {
                    ToastUtil.toastShortErrorMessage("您已经被移出群组");
                    return;
                }
                ChatInfo chatInfo = GroupChatManagerKit.getInstance().getCurrentChatInfo();
                if (chatInfo == null) {
                    chatInfo = C2CChatManagerKit.getInstance().getCurrentChatInfo();
                }
                if (chatInfo != null && !StringUtils.isEmpty(chatInfo.getChatName())) {
                    msg.setGroupNameCard(chatInfo.getChatName());
                }
                try {
                    msg.setGroup(chatInfo != null && chatInfo.getType() == V2TIMConversation.V2TIM_GROUP);
                    msg.setSendUserName(YmyUserManager.INSTANCE.getUser().getNickname());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                AbsChatLayout.this.sendMessage(msg, false);
            }
        });
        im_delLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDelDialog();
            }
        });
        next_layout.setOnClickListener(new OnClickListener() {//交接下一步
            @Override
            public void onClick(View v) {
                List<MessageInfo> temp = new ArrayList<>();
                List<MessageInfo> list = mAdapter.getmDataSource();
                for (MessageInfo messageInfo : list) {
                    if (messageInfo.isSelect()) {
                        temp.add(messageInfo);
                    }
                }
                messageUploadList.clear();
                sendUploadMessage(temp, 0);
            }
        });
        im_shareLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<MessageInfo> mDataSource = new ArrayList<>();
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    MessageInfo msg = mAdapter.getItem(i);
                    if (msg != null && msg.isSelect()) {
                        msg.setShowSelect(true);
                        mDataSource.add(msg);
                    }
                }
                if (mDataSource.size() == 0) {
                    ToastUtil.toastShortMessage("请选择转发的内容");
                    return;
                }
                SelectSessionActivity.invoke(getContext(), mDataSource);
            }
        });
        getInputLayout().clearCustomActionList();
        if (getMessageLayout().getAdapter() == null) {
            mAdapter = new MessageListAdapter();
            mAdapter.setSelectView(selectNum);
            getMessageLayout().setAdapter(mAdapter);
        }
        initListener();
    }

    @Override
    public void loadMessages() {
        loadChatMessages(mAdapter.getItemCount() > 0 ? mAdapter.getItem(1) : null);
    }

    @Override
    public void sendMessage(MessageInfo msg, boolean retry) {
        getChatManager().sendMessage(msg, retry, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scrollToEnd();
                    }
                });
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //                ToastUtil.toastLongMessage(errMsg);
            }
        });
    }

    @Override
    public void setParentLayout(Object parentContainer) {

    }

    public abstract ChatManagerKit getChatManager();

    /**
     * 展示交接布局
     */
    public void showJiaoJieState() {
        more_select.setVisibility(View.GONE);
        getInputLayout().setVisibility(View.GONE);
        jiaojie_layout.setVisibility(View.VISIBLE);
        mAdapter.uppDataState(true);
    }

    /**
     * 隐藏交接布局
     */
    public void hideJiaoJieState() {
        more_select.setVisibility(View.VISIBLE);
        getInputLayout().setVisibility(View.VISIBLE);
        jiaojie_layout.setVisibility(View.GONE);
        mAdapter.uppDataState(false);
    }


    public void clearSelectedStatus() {
        if (mAdapter != null) {
            mAdapter.uppDataState(false);
            more_select.setVisibility(View.GONE);
            jiaojie_layout.setVisibility(GONE);
            getInputLayout().setVisibility(View.VISIBLE);
        }
    }
}
