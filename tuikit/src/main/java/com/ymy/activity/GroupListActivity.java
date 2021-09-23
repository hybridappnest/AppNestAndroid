package com.ymy.activity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.ymy.utils.Constants;
import com.ymy.utils.DensityUtil;
import com.google.gson.Gson;
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
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.qcloud.tim.uikit.config.TUIKitConfigs;
import com.tencent.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.qcloud.tim.uikit.modules.chat.base.OfflineMessageBean;
import com.tencent.qcloud.tim.uikit.modules.chat.base.OfflineMessageContainerBean;
import com.tencent.qcloud.tim.uikit.modules.contact.ContactItemBean;
import com.tencent.qcloud.tim.uikit.modules.contact.ContactListView;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.tencent.qcloud.tim.uikit.utils.ImageUtil;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.base.RootActivity;
import com.ymy.core.utils.StringUtils;

import java.io.File;
import java.util.List;

import static com.tencent.qcloud.tim.uikit.modules.chat.base.ChatManagerKit.mCurrentProvider;

public class GroupListActivity extends RootActivity {

    private static final String TAG = GroupListActivity.class.getSimpleName();

    private TitleBarLayout mTitleBar;
    private ContactListView mListView;
    public static void invoke(Context context){
        mDataSource = null;
        context.startActivity(new Intent(context,GroupListActivity.class));
    }

    public static void invoke(Context context, List<MessageInfo> source){
        mDataSource = source;
        Intent intent = new Intent(context,GroupListActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_list_activity);

        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDataSource = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataSource();
    }
    public static List<MessageInfo> mDataSource;
    private void init() {
        mTitleBar = findViewById(R.id.group_list_titlebar);
        mTitleBar.setTitle(getResources().getString(R.string.group), TitleBarLayout.POSITION.MIDDLE);
        mTitleBar.setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        mTitleBar.setTitle(getResources().getString(R.string.add_group), TitleBarLayout.POSITION.RIGHT);
        mTitleBar.getRightIcon().setVisibility(View.GONE);
//        mTitleBar.setOnRightClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(TUIKit.getAppContext(), AddMoreActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("isGroup", true);
//                startActivity(intent);
//            }
//        });

        mListView = findViewById(R.id.group_list);
        mListView.setOnItemClickListener(new ContactListView.OnItemClickListener() {
            @Override
            public void onItemClick(int position, ContactItemBean contact) {
                if(DensityUtil.isFastClcick()){//防止快速点击
                    return;
                }
                if(mDataSource != null && mDataSource.size()>0){
                    sendMessage(contact,0);
                    return;
                }
                ChatInfo chatInfo = new ChatInfo();
                chatInfo.setType(V2TIMConversation.V2TIM_GROUP);
                String chatName = contact.getId();
                if (!TextUtils.isEmpty(contact.getRemark())) {
                    chatName = contact.getRemark();
                } else if (!TextUtils.isEmpty(contact.getNickname())) {
                    chatName = contact.getNickname();
                }
                chatInfo.setChatName(chatName);
                chatInfo.setId(contact.getId());
                chatInfo.setGroupType(contact.isGroup()?contact.getEvent_group_type():"");
                Intent intent = new Intent(GroupListActivity.this, ImChatActivity.class);
                intent.putExtra(Constants.CHAT_INFO, chatInfo);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
    public void sendMessage(final ContactItemBean contact, final int postion){
        MessageInfo messageInfo = mDataSource.get(postion);
        messageInfo = resetMessinfo(messageInfo);
        if(messageInfo == null){
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
        // OPPO必须设置ChannelID才可以收到推送消息，这个channelID需要和控制台一致
        v2TIMOfflinePushInfo.setAndroidOPPOChannelID("tuikit");
        V2TIMMessage v2TIMMessage = messageInfo.getTimMessage();
        final MessageInfo finalMessageInfo = messageInfo;
        String msgID =   V2TIMManager.getMessageManager().sendMessage(v2TIMMessage, isGroup ? null : userID, isGroup ? groupID : null,
                V2TIMMessage.V2TIM_PRIORITY_DEFAULT, false, v2TIMOfflinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onError(int code, String desc) {
                        TUIKitLog.e(TAG, "sendMessage   onError: " );
                        if(mCurrentProvider != null){
                            finalMessageInfo.setStatus(MessageInfo.MSG_STATUS_SEND_FAIL);
                            mCurrentProvider.updateMessageInfo(finalMessageInfo);
                        }
                        if(mDataSource != null && mDataSource.size()>(postion+1)){
                            sendMessage(contact,postion+1);
                        }else {
                            if(!zhuanfaState){
                                ToastUtil.toastShortMessage("转发失败");
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onSuccess(V2TIMMessage v2TIMMessage) {
                        TUIKitLog.e(TAG, "sendMessage   onSuccess: " );
                        if(mCurrentProvider != null){
                            finalMessageInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                            mCurrentProvider.updateMessageInfo(finalMessageInfo);
                        }
                        zhuanfaState = true;
                        if(mDataSource != null && mDataSource.size()>(postion+1)){
                            sendMessage(contact,postion+1);
                        }else {
                            ToastUtil.toastShortMessage("转发成功");
                            finish();
                        }
                    }
                });
        finalMessageInfo.setId(msgID);
    }
    boolean  zhuanfaState = false;
    private MessageInfo resetMessinfo(MessageInfo msgInfo){
        MessageInfo timMessage = resetTimMessage(msgInfo);
        if(timMessage == null){
            return  null;
        }
        timMessage.setFromUser(V2TIMManager.getInstance().getLoginUser());
        timMessage.setShowSelect(msgInfo.isShowSelect());
        timMessage.setMsgTime(timMessage.getTimMessage().getTimestamp());
        timMessage.setSelf(true);
        return  timMessage;
    }
    private MessageInfo resetTimMessage(MessageInfo msgInfo){
        V2TIMMessage timMessage = msgInfo.getTimMessage();
        int type = timMessage.getElemType();
        if (type == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
            V2TIMCustomElem customElem = timMessage.getCustomElem();
            String data = new String(customElem.getData());
            Gson gson = new Gson();
            MessageCustom messageCustom = null;
            try {
                messageCustom = gson.fromJson(data, MessageCustom.class);
                MessageCustom customAudioMessage = new MessageCustom();
                String voicePath = "";
                if(new File(messageCustom.getLocalPath()).exists()){
                    voicePath = messageCustom.getLocalPath();
                }else if(!TextUtils.isEmpty(messageCustom.getRemoteUrl())){
                    String url = messageCustom.getRemoteUrl();
                    final String path = TUIKitConstants.RECORD_DOWNLOAD_DIR +msgInfo.getFromUser()+"/";
                    final String name = url.substring(url.lastIndexOf("/")+1);
                    File file = new File(path+name);
                    if (file.exists()) {
                        voicePath = file.getAbsolutePath();
                    }
                }
                if(StringUtils.isEmpty(voicePath)){
                    return  null;
                }
                customAudioMessage.setDiscernResult(messageCustom.getDiscernResult());
                customAudioMessage.setDuration(messageCustom.getDuration());
                customAudioMessage.setLocalPath(voicePath);
                customAudioMessage.setRemoteUrl(messageCustom.getRemoteUrl());
                String data1 = gson.toJson(customAudioMessage);
                return   MessageInfoUtil.buildCustomAudioMessage(data1);
            } catch (Exception e) {
                TUIKitLog.e(TAG, "invalid json: " + data + ", exception:" + e);
            }
        } else {
            if (type == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT) {
                V2TIMTextElem txtEle = timMessage.getTextElem();
                return MessageInfoUtil.buildTextMessage(txtEle.getText());
            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE) {
                V2TIMImageElem imageEle = timMessage.getImageElem();
                List<V2TIMImageElem.V2TIMImage> imgs = imageEle.getImageList();
                if(!StringUtils.isEmpty(imageEle.getPath())){
                    return MessageInfoUtil.buildImageMessage1(imageEle.getPath(),false);
                }
                for (int i = 0; i < imgs.size(); i++) {
                    V2TIMImageElem.V2TIMImage img = imgs.get(i);
                    if (img.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_THUMB) {
                        final String path = TUIKitConstants.IMAGE_DOWNLOAD_DIR + img.getUUID();
                        File file = new File(path);
                        if (file.exists()) {
                            return MessageInfoUtil.buildImageMessage1(path,false);
                        }
                    }
                }
            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO) {
                V2TIMVideoElem videoEle = timMessage.getVideoElem();
                if (msgInfo.isSelf() && !TextUtils.isEmpty(videoEle.getSnapshotPath())) {
                    int size[] = ImageUtil.getImageSize(videoEle.getSnapshotPath());
                    return MessageInfoUtil.buildVideoMessage(videoEle.getSnapshotPath(),videoEle.getVideoPath(),size[0],size[1],videoEle.getDuration()*1000);
                } else {
                    final String videoPath = TUIKitConstants.VIDEO_DOWNLOAD_DIR + videoEle.getVideoUUID();
                    final String snapPath = TUIKitConstants.IMAGE_DOWNLOAD_DIR + videoEle.getSnapshotUUID();
                    //判断快照是否存在,不存在自动下载
                    if (!new File(snapPath).exists()) {
                        return  null;
                    }
                    return MessageInfoUtil.buildVideoMessage(snapPath,videoPath,(int) videoEle.getSnapshotWidth(),(int) videoEle.getSnapshotHeight(),videoEle.getDuration()*1000);
                }
            }
        }
        return  null;
    }
    public void loadDataSource() {
        mListView.loadDataSource(ContactListView.DataSource.GROUP_LIST);
    }

    @Override
    public void finish() {
        super.finish();
    }
    public static Uri getMediaUriFromPath(Context context, String path) {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(mediaUri,
                null,
                MediaStore.Images.Media.DISPLAY_NAME + "= ?",
                new String[] {path.substring(path.lastIndexOf("/") + 1)},
                null);

        Uri uri = null;
        if(cursor.moveToFirst()) {
            uri = ContentUris.withAppendedId(mediaUri,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
        }
        cursor.close();
        return uri;
    }
}
