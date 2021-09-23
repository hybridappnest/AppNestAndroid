package com.tencent.qcloud.tim.uikit.modules.chat.layout.message.holder;

import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ymy.down.listener.DownloadListener;
import com.ymy.down.utils.DownloadUtils;
import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.base.IUIKitCallBack;
import com.tencent.qcloud.tim.uikit.component.AudioPlayer;
import com.tencent.qcloud.tim.uikit.modules.chat.C2CChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatManagerKit;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatProvider;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.utils.ScreenUtil;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.ymy.core.utils.StringUtils;

import java.io.File;
import java.util.List;

public class MessageAudioDBXHolder extends MessageContentHolder {

    private static final int AUDIO_MIN_WIDTH = ScreenUtil.getPxByDp(60);
    private static final int AUDIO_MAX_WIDTH = ScreenUtil.getPxByDp(250);

    private static final int UNREAD = 0;
    private static final int READ = 1;

    private TextView audioTimeText;
    private ImageView audioPlayImage;
    private LinearLayout audioContentView;

    public MessageAudioDBXHolder(View itemView) {
        super(itemView);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.message_adapter_content_audio;
    }

    @Override
    public void initVariableViews() {
        audioTimeText = rootView.findViewById(R.id.audio_time_tv);
        audioPlayImage = rootView.findViewById(R.id.audio_play_iv);
        audioContentView = rootView.findViewById(R.id.audio_content_ll);
    }

    @Override
    public void layoutVariableViews(final MessageInfo msg, final int position) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        if (msg.isSelf()) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.rightMargin = 24;
            audioPlayImage.setImageResource(R.drawable.voice_msg_playing_im);
            audioPlayImage.setRotation(180f);
            audioContentView.removeView(audioPlayImage);
            audioContentView.addView(audioPlayImage);
            unreadAudioText.setVisibility(View.GONE);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.leftMargin = 24;
            // TODO 图标不对
            audioPlayImage.setImageResource(R.drawable.voice_msg_playing_im);
            audioContentView.removeView(audioPlayImage);
            audioContentView.addView(audioPlayImage, 0);
            if (msg.getCustomInt() == UNREAD) {
                LinearLayout.LayoutParams unreadParams = (LinearLayout.LayoutParams) isReadText.getLayoutParams();
                unreadParams.gravity = Gravity.CENTER_VERTICAL;
                unreadParams.leftMargin = 10;
                unreadAudioText.setVisibility(View.VISIBLE);
                unreadAudioText.setLayoutParams(unreadParams);
            } else {
                unreadAudioText.setVisibility(View.GONE);
            }
        }
        audioContentView.setLayoutParams(params);
        V2TIMCustomElem customElem = msg.getTimMessage().getCustomElem();
        String data = new String(customElem.getData());
        MessageCustom messageCustom = null;
        try {
            Gson gson = new Gson();
            messageCustom = gson.fromJson(data, MessageCustom.class);


            initVoiceText(msg.isSelf(),messageCustom.getDiscernResult());
            int duration = messageCustom.getDuration();
            if (duration == 0) {
                duration = 1;
            }
            if(msg.isSelf() && StringUtils.isEmpty(messageCustom.getRemoteUrl())){
                setSendFail();
            }
            if (TextUtils.isEmpty(msg.getDataPath())) {
                getSound(msg, messageCustom.getRemoteUrl());
            }
            ViewGroup.LayoutParams audioParams = msgContentFrame.getLayoutParams();
            audioParams.width = AUDIO_MIN_WIDTH + ScreenUtil.getPxByDp(duration * 6);
            if (audioParams.width > AUDIO_MAX_WIDTH) {
                audioParams.width = AUDIO_MAX_WIDTH;
            }
            msgContentFrame.setLayoutParams(audioParams);
            audioTimeText.setText(duration + "''");
            msgContentFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AudioPlayer.getInstance().isPlaying()) {
                        AudioPlayer.getInstance().stopPlay();
                        return;
                    }
                    if (TextUtils.isEmpty(msg.getDataPath())) {
//                        ToastUtil.toastLongMessage("语音文件还未下载完成");
                        return;
                    }
                    audioPlayImage.setImageResource(R.drawable.play_voice_message);
                    if (msg.isSelf()) {
                        audioPlayImage.setRotation(180f);
                    }
                    final AnimationDrawable animationDrawable = (AnimationDrawable) audioPlayImage.getDrawable();
                    animationDrawable.start();
                    msg.setCustomInt(READ);
                    unreadAudioText.setVisibility(View.GONE);
                    AudioPlayer.getInstance().startPlay(msg.getDataPath(), new AudioPlayer.Callback() {
                        @Override
                        public void onCompletion(Boolean success) {
                            audioPlayImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    animationDrawable.stop();
                                    audioPlayImage.setImageResource(R.drawable.voice_msg_playing_im);
                                    if (msg.isSelf()) {
                                        audioPlayImage.setRotation(180f);
                                    }
                                    checkUnReadAudio(msg);
                                }
                            });

                        }
                    });
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void checkUnReadAudio(MessageInfo msg){
        List<MessageInfo> messageInfos = mAdapter.getmDataSource();
        int index = 0;
        for(int i = 0;i<messageInfos.size();i++){
            MessageInfo messageInfo = messageInfos.get(i);
            if(messageInfo.getId().equals(msg.getId())){
                index = i;
            }
        }
        index = index +1;
        for(int i = index;i<messageInfos.size();i++){
            MessageInfo messageInfo = messageInfos.get(i);
            if(messageInfo.getCustomInt() == UNREAD&&messageInfo.getExtra().equals("[语音消息]")&&!messageInfo.isSelf()){
                try {
                    View view1 = recyclerView.getLayoutManager().findViewByPosition(i+1);;//获取到对应Item的View
                    if(view1 != null &&recyclerView.getChildViewHolder(view1) instanceof MessageAudioDBXHolder){
                        MessageAudioDBXHolder myViewHolder = (MessageAudioDBXHolder) recyclerView.getChildViewHolder(view1);//获取到对应Item的ViewHolder
                        mAdapter.getItem(i).setCustomInt(READ);
                        myViewHolder.autoClick();//调用ViewHolder中的方法
                        return;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }
    public void autoClick(){
        msgContentFrame.performClick();
    }
    private void getSound(final MessageInfo msgInfo,String url) {
        final String path = TUIKitConstants.RECORD_DOWNLOAD_DIR +msgInfo.getFromUser()+"/";
        final String name = url.substring(url.lastIndexOf("/")+1);
        File file = new File(path+name);
        if (!file.exists()) {
            DownloadUtils downloadUtils = new DownloadUtils(TUIKit.getAppContext());
            downloadUtils.downloadFile(url, path,name, new DownloadListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(final int currentLength) {
                }

                @Override
                public void onFinish(String localPath) {
                    msgInfo.setDataPath(path+name);
                }

                @Override
                public void onFailure(final String errorInfo) {
                }
            });
        } else {
            msgInfo.setDataPath(path+name);
        }
    }

}
