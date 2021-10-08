package com.tencent.qcloud.tim.uikit.modules.chat.layout.message.holder;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.cloud.qcloudasrsdk.common.QCloudAudioFormat;
import com.tencent.cloud.qcloudasrsdk.common.QCloudAudioFrequence;
import com.tencent.cloud.qcloudasrsdk.common.QCloudSourceType;
import com.tencent.cloud.qcloudasrsdk.models.QCloudOneSentenceRecognitionParams;
import com.tencent.cloud.qcloudasrsdk.recognizer.QCloudOneSentenceRecognizer;
import com.tencent.cloud.qcloudasrsdk.recognizer.QCloudOneSentenceRecognizerListener;
import com.ymy.im.signature.GenerateTestUserSig;
import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.liteav.model.DiscernResult;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.component.AudioPlayer;
import com.tencent.qcloud.tim.uikit.component.gatherimage.UserIconView;
import com.tencent.qcloud.tim.uikit.config.TUIKitConfigs;
import com.tencent.qcloud.tim.uikit.modules.chat.C2CChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.utils.NetWorkUtils;
import com.ymy.core.lifecycle.KtxManager;
import com.ymy.core.upload.OSSManager;
import com.ymy.core.upload.UploadUtils;
import com.ymy.core.upload.UploadViewModel;
import com.ymy.core.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class MessageContentHolder extends MessageEmptyHolder {

    public UserIconView leftUserIcon;
    public UserIconView rightUserIcon;
    public TextView usernameText;
    public LinearLayout msgContentLinear;
    public ProgressBar sendingProgress;
    public ImageView statusImage;
    public TextView isReadText;
    public TextView unreadAudioText;
    private TextView msgBodyAudioTv;
    private QCloudOneSentenceRecognizer recognizer;

    public MessageContentHolder(View itemView) {
        super(itemView);
        rootView = itemView;

        leftUserIcon = itemView.findViewById(R.id.left_user_icon_view);
        rightUserIcon = itemView.findViewById(R.id.right_user_icon_view);
        usernameText = itemView.findViewById(R.id.user_name_tv);
        msgContentLinear = itemView.findViewById(R.id.msg_content_ll);
        statusImage = itemView.findViewById(R.id.message_status_iv);
        sendingProgress = itemView.findViewById(R.id.message_sending_pb);
        isReadText = itemView.findViewById(R.id.is_read_tv);
        unreadAudioText = itemView.findViewById(R.id.audio_unread);
        msgBodyAudioTv = itemView.findViewById(R.id.msg_body_audio_tv);
        if (msgBodyAudioTv != null) {
            msgBodyAudioTv.setVisibility(View.GONE);
        }
    }

    public void initVoiceText(boolean self, String text) {
        if (msgBodyAudioTv != null) {
            msgBodyAudioTv.setVisibility(View.VISIBLE);
            msgBodyAudioTv.setText(text);
            if (self) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) msgBodyAudioTv.getLayoutParams();
                layoutParams.gravity =Gravity.END;
                msgBodyAudioTv.setLayoutParams(layoutParams);
                msgBodyAudioTv.setBackgroundResource(R.drawable.bg_message_blue);
            } else {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) msgBodyAudioTv.getLayoutParams();
                layoutParams.gravity =Gravity.START;
                msgBodyAudioTv.setLayoutParams(layoutParams);
                msgBodyAudioTv.setBackgroundResource(R.drawable.bg_message_white);
            }
        }
    }

    private void setVariableLayoutGravity(boolean isSelf) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) msgContentLinear.getLayoutParams();
        layoutParams.gravity = isSelf ? Gravity.END : Gravity.START;
        msgContentLinear.setLayoutParams(layoutParams);
    }

    @Override
    public void layoutViews(final MessageInfo msg, final int position) {
        super.layoutViews(msg, position);

        //// 头像设置
        if (msg.isSelf()) {
            leftUserIcon.setVisibility(View.GONE);
            rightUserIcon.setVisibility(View.VISIBLE);
            setVariableLayoutGravity(true);
        } else {
            leftUserIcon.setVisibility(View.VISIBLE);
            rightUserIcon.setVisibility(View.GONE);
            setVariableLayoutGravity(false);
        }
        if (properties.getAvatar() != 0) {
            leftUserIcon.setDefaultImageResId(properties.getAvatar());
            rightUserIcon.setDefaultImageResId(properties.getAvatar());
        } else {
            leftUserIcon.setDefaultImageResId(R.drawable.default_head);
            rightUserIcon.setDefaultImageResId(R.drawable.default_head);
        }
        if (properties.getAvatarRadius() != 0) {
            leftUserIcon.setRadius(properties.getAvatarRadius());
            rightUserIcon.setRadius(properties.getAvatarRadius());
        } else {
            leftUserIcon.setRadius(5);
            rightUserIcon.setRadius(5);
        }
        if (properties.getAvatarSize() != null && properties.getAvatarSize().length == 2) {
            ViewGroup.LayoutParams params = leftUserIcon.getLayoutParams();
            params.width = properties.getAvatarSize()[0];
            params.height = properties.getAvatarSize()[1];
            leftUserIcon.setLayoutParams(params);

            params = rightUserIcon.getLayoutParams();
            params.width = properties.getAvatarSize()[0];
            params.height = properties.getAvatarSize()[1];
            rightUserIcon.setLayoutParams(params);
        }
        leftUserIcon.invokeInformation(msg);
        rightUserIcon.invokeInformation(msg);

        //// 用户昵称设置
        if (msg.isSelf()) { // 默认不显示自己的昵称
            if (properties.getRightNameVisibility() == 0) {
                usernameText.setVisibility(View.GONE);
            } else {
                usernameText.setVisibility(properties.getRightNameVisibility());
            }
        } else {
            if (properties.getLeftNameVisibility() == 0) {
                if (msg.isGroup()) { // 群聊默认显示对方的昵称
                    usernameText.setVisibility(View.VISIBLE);
                } else { // 单聊默认不显示对方昵称
                    usernameText.setVisibility(View.GONE);
                }
            } else {
                usernameText.setVisibility(properties.getLeftNameVisibility());
            }
        }
        if (properties.getNameFontColor() != 0) {
            usernameText.setTextColor(properties.getNameFontColor());
        }
        if (properties.getNameFontSize() != 0) {
            usernameText.setTextSize(properties.getNameFontSize());
        }
        // 聊天界面设置头像和昵称
        V2TIMMessage timMessage = msg.getTimMessage();
        if (!TextUtils.isEmpty(timMessage.getNameCard())) {
            usernameText.setText(timMessage.getNameCard().trim());
        } else if (!TextUtils.isEmpty(timMessage.getFriendRemark())) {
            usernameText.setText(timMessage.getFriendRemark().trim());
        } else if (!TextUtils.isEmpty(timMessage.getNickName())) {
            usernameText.setText(timMessage.getNickName().trim());
        } else {
            usernameText.setText(timMessage.getSender().trim());
        }

        if (!TextUtils.isEmpty(timMessage.getFaceUrl())) {
            List<Object> urllist = new ArrayList<>();
            urllist.add(timMessage.getFaceUrl());
            if (msg.isSelf()) {
                rightUserIcon.setIconUrls(urllist);
            } else {
                leftUserIcon.setIconUrls(urllist);
            }
        }

        if (msg.isSelf()) {
            if (msg.getStatus() == MessageInfo.MSG_STATUS_SEND_FAIL
                    || msg.getStatus() == MessageInfo.MSG_STATUS_SEND_SUCCESS
                    || msg.isPeerRead()) {
                sendingProgress.setVisibility(View.GONE);
            } else {
                sendingProgress.setVisibility(View.VISIBLE);
            }
        } else {
            sendingProgress.setVisibility(View.GONE);
        }

        //// 聊天气泡设置
        if (msg.isSelf()) {
            if (properties.getRightBubble() != null && properties.getRightBubble().getConstantState() != null) {
                msgContentFrame.setBackground(properties.getRightBubble().getConstantState().newDrawable());
            } else {
                msgContentFrame.setBackgroundResource(R.drawable.chat_bubble_myself);
            }
        } else {
            if (properties.getLeftBubble() != null && properties.getLeftBubble().getConstantState() != null) {
                msgContentFrame.setBackground(properties.getLeftBubble().getConstantState().newDrawable());
                msgContentFrame.setLayoutParams(msgContentFrame.getLayoutParams());
            } else {
                msgContentFrame.setBackgroundResource(R.drawable.bg_message_white);
            }
            if (msg.getMsgType() == MessageInfo.MSG_TYPE_AUDIO_LEFT || msg.getMsgType() == MessageInfo.MSG_TYPE_AUDIO || msg.getMsgType() == MessageInfo.MSG_TYPE_AUDIO_CUSTOM) {
                msgContentFrame.setBackgroundResource(R.drawable.bg_message_white);
            }
        }

        //// 聊天气泡的点击事件处理
        if (onItemClickListener != null) {
            msgContentFrame.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onMessageLongClick(v, position, msg);
                    return true;
                }
            });
            leftUserIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onUserIconClick(view, position, msg);
                }
            });
            rightUserIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onUserIconClick(view, position, msg);
                }
            });
        }

        //// 发送状态的设置
        if (msg.getStatus() == MessageInfo.MSG_STATUS_SEND_FAIL) {
            statusImage.setVisibility(View.VISIBLE);
            msgContentFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onMessageLongClick(msgContentFrame, position, msg);
                    }
                }
            });
        } else {
            msgContentFrame.setOnClickListener(null);
            statusImage.setVisibility(View.GONE);
        }
        statusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatInfo mCurrentChatInfo = GroupChatManagerKit.getInstance().getCurrentChatInfo();
                if (msg.getMsgType() == MessageInfo.MSG_TYPE_AUDIO_CUSTOM) {//重新上传，语音需要特殊处理
                    discernVoice(msg);
                    return;
                }
                if (mCurrentChatInfo == null || StringUtils.isEmpty(mCurrentChatInfo.getId())) {
                    C2CChatManagerKit.getInstance().sendMessage(msg, true, null);
                } else {
                    GroupChatManagerKit.getInstance().sendMessage(msg, true, null);
                }
            }
        });
        //// 左右边的消息需要调整一下内容的位置
        if (msg.isSelf()) {
            msgContentLinear.removeView(msgContentFrame);
            msgContentLinear.addView(msgContentFrame);
        } else {
            msgContentLinear.removeView(msgContentFrame);
            msgContentLinear.addView(msgContentFrame, 0);
        }

        msgContentLinear.setVisibility(View.VISIBLE);

        //// 对方已读标识的设置
        if (TUIKitConfigs.getConfigs().getGeneralConfig().isShowRead()) {
            if (msg.isSelf()) {
                if (msg.isGroup()) {
                    isReadText.setVisibility(View.GONE);
                } else {
                    isReadText.setVisibility(View.GONE);
//                    isReadText.setVisibility(View.VISIBLE);
//                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) isReadText.getLayoutParams();
//                    params.gravity = Gravity.CENTER_VERTICAL;
//                    isReadText.setLayoutParams(params);
//                    if (msg.isPeerRead()) {
//                        isReadText.setText(R.string.has_read);
//                    } else {
//                        isReadText.setText(R.string.unread);
//                    }
                }
            } else {
                isReadText.setVisibility(View.GONE);
            }
        }

        //// 音频已读
        unreadAudioText.setVisibility(View.GONE);

        //// 由子类设置指定消息类型的views
        layoutVariableViews(msg, position);
    }

    public void setBg() {
        msgContentFrame.setBackgroundResource(R.drawable.chat_other_bg);
    }

    public void setSendFail() {
        sendingProgress.setVisibility(View.GONE);
        statusImage.setVisibility(View.VISIBLE);
        isReadText.setVisibility(View.GONE);
    }

    /**
     * 开始识别
     */
    private void discernVoice(final MessageInfo info) {
        V2TIMCustomElem customElem = info.getTimMessage().getCustomElem();
        String data = new String(customElem.getData());
        Gson gson = new Gson();
        MessageCustom messageCustom = null;
        try {
            messageCustom = gson.fromJson(data, MessageCustom.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageCustom == null) {
            return;
        }
        if (!NetWorkUtils.IsNetWorkEnable(KtxManager.getCurrentActivity())) {
            uploadAudio(info, "");
            return;
        }
        InputStream is = null;
        try {
            if (recognizer == null) {
                recognizer = new QCloudOneSentenceRecognizer((AppCompatActivity) KtxManager.getCurrentActivity(), GenerateTestUserSig.apppId, GenerateTestUserSig.secretId, GenerateTestUserSig.secretKey);
                recognizer.setCallback(new QCloudOneSentenceRecognizerListener() {
                    @Override
                    public void didStartRecord() {

                    }

                    @Override
                    public void didStopRecord() {

                    }

                    @Override
                    public void recognizeResult(QCloudOneSentenceRecognizer qCloudOneSentenceRecognizer, String s, Exception e) {
                        Log.e("recognizeResult", "thread id:" + Thread.currentThread().getId() + " name:" + Thread.currentThread().getName());
                        try {
                            DiscernResult messageCustom = new Gson().fromJson(s, DiscernResult.class);
                            uploadAudio(info, messageCustom.getResponse().getResult());
                            return;
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        // 语音发送异常
                    }
                });
            }
            File f = new File(messageCustom.getLocalPath());
            is = new FileInputStream(f);
            int length = is.available();
            byte[] audioData = new byte[length];
            is.read(audioData);
            QCloudOneSentenceRecognitionParams params = (QCloudOneSentenceRecognitionParams) QCloudOneSentenceRecognitionParams.defaultRequestParams();
            params.setFilterDirty(0);// 0 ：默认状态 不过滤脏话 1：过滤脏话
            params.setFilterModal(0);// 0 ：默认状态 不过滤语气词  1：过滤部分语气词 2:严格过滤
            params.setFilterPunc(0); // 0 ：默认状态 不过滤句末的句号 1：滤句末的句号
            params.setConvertNumMode(1);//1：默认状态 根据场景智能转换为阿拉伯数字；0：全部转为中文数字。
//                    params.setHotwordId(""); // 热词id。用于调用对应的热词表，如果在调用语音识别服务时，不进行单独的热词id设置，自动生效默认热词；如果进行了单独的热词id设置，那么将生效单独设置的热词id。
            params.setData(audioData);
            params.setVoiceFormat(QCloudAudioFormat.QCloudAudioFormatMp3);
            params.setSourceType(QCloudSourceType.QCloudSourceTypeData);
            params.setEngSerViceType(QCloudAudioFrequence.QCloudAudioFrequence16k.getFrequence());
            recognizer.recognize(params);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception msg" + e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 重新上传并发送
     */
    private void uploadAudio(final MessageInfo info, final String discernResult) {
        ArrayList<String> list = new ArrayList<>();
        MessageCustom messageCustom = null;
        try {
            V2TIMCustomElem customElem = info.getTimMessage().getCustomElem();
            String data = new String(customElem.getData());
            Gson gson = new Gson();
            messageCustom = gson.fromJson(data, MessageCustom.class);
            list.add(messageCustom.getLocalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (list.size() == 0 || messageCustom == null) {
            return;
        }
        Log.e("INPUT_INFO", "LOCAL_VOICE = " + AudioPlayer.getInstance().getPath());
        UploadViewModel mUploadViewModel = new UploadViewModel();
        final MessageCustom finalMessageCustom = messageCustom;
        mUploadViewModel.uploadFile(
                list,
                UploadUtils.getUploadFilePath(OSSManager.imFolder, "alarm"), new UploadViewModel.CallBack() {
                    @Override
                    public void onSuccess(@NotNull String ossTag, @NotNull ArrayList<String> resultList) {
                        //上传成功的urls
                        ArrayList<String> showSuccessList = resultList;
                        if (showSuccessList != null && showSuccessList.size() > 0) {
                            Log.e("INPUT_INFO", "duration = " + AudioPlayer.getInstance().getDuration());
                            Gson gson = new Gson();
                            MessageCustom customAudioMessage = new MessageCustom();
                            customAudioMessage.setDiscernResult(discernResult);
                            customAudioMessage.setDuration(finalMessageCustom.getDuration());
                            customAudioMessage.setLocalPath(finalMessageCustom.getLocalPath());
                            customAudioMessage.setRemoteUrl(showSuccessList.get(0));
                            String data = gson.toJson(customAudioMessage);
                            info.setTimMessage(V2TIMManager.getMessageManager().createCustomMessage(data.getBytes()));
                            if (info == null || !info.isGroup()) {
                                C2CChatManagerKit.getInstance().sendMessage(info, true, null);
                            } else {
                                GroupChatManagerKit.getInstance().sendMessage(info, true, null);
                            }
                        }
                    }

                    @Override
                    public void showLoading(boolean show) {
                        // TODO: 2020/8/27 true展示loading 或 false去掉loading
                    }

                    @Override
                    public void onError(@NotNull String errorMsg) {
                        MessageCustom customAudioMessage = new MessageCustom();
                        Gson gson = new Gson();
                        customAudioMessage.setDiscernResult(finalMessageCustom.getDiscernResult());
                        customAudioMessage.setDuration(finalMessageCustom.getDuration());
                        customAudioMessage.setLocalPath(finalMessageCustom.getLocalPath());
                        customAudioMessage.setRemoteUrl("");
                        String data = gson.toJson(customAudioMessage);
                        Log.e("INPUT_INFO", "MessageInfo = " + data);
                        info.setTimMessage(V2TIMManager.getMessageManager().createCustomMessage(data.getBytes()));
                        if (info == null || !info.isGroup()) {
                            C2CChatManagerKit.getInstance().sendMessage(info, true, null);
                        } else {
                            GroupChatManagerKit.getInstance().sendMessage(info, true, null);
                        }
                    }
                });
    }

    public abstract void layoutVariableViews(final MessageInfo msg, final int position);
}
