package com.tencent.qcloud.tim.uikit.base;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.ymy.down.listener.DownloadListener;
import com.ymy.down.utils.DownloadUtils;
import com.ymy.function.Func;
import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMDownloadCallback;
import com.tencent.imsdk.v2.V2TIMElem;
import com.tencent.imsdk.v2.V2TIMFaceElem;
import com.tencent.imsdk.v2.V2TIMFileElem;
import com.tencent.imsdk.v2.V2TIMGroupChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMGroupTipsElem;
import com.tencent.imsdk.v2.V2TIMImageElem;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMSoundElem;
import com.tencent.imsdk.v2.V2TIMTextElem;
import com.tencent.imsdk.v2.V2TIMVideoElem;
import com.tencent.liteav.model.CallModel;
import com.tencent.qcloud.tim.uikit.TUIKit;
import com.tencent.qcloud.tim.uikit.modules.message.MessageCustom;
import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.qcloud.tim.uikit.modules.message.ViewData;
import com.tencent.qcloud.tim.uikit.utils.DateTimeUtil;
import com.tencent.qcloud.tim.uikit.utils.FileUtil;
import com.tencent.qcloud.tim.uikit.utils.ImageUtil;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.ymy.core.utils.StringUtils;

import java.io.File;
import java.util.List;

import static com.tencent.qcloud.tim.uikit.modules.message.MessageInfoUtil.TIMElemType2MessageInfoType;
import static com.tencent.qcloud.tim.uikit.modules.message.MessageInfoUtil.isTyping;

/**
 * IM事件监听
 */

public abstract class IMEventListener {
    private final static String TAG = IMEventListener.class.getSimpleName();

    /**
     * 被踢下线时回调
     */
    public void onForceOffline() {
        TUIKitLog.v(TAG, "onForceOffline");
    }

    /**
     * 用户票据过期
     */
    public void onUserSigExpired() {
        TUIKitLog.v(TAG, "onUserSigExpired");
    }

    /**
     * 连接建立
     */
    public void onConnected() {
        TUIKitLog.v(TAG, "onConnected");
    }

    /**
     * 连接断开
     *
     * @param code 错误码
     * @param desc 错误描述
     */
    public void onDisconnected(int code, String desc) {
        TUIKitLog.v(TAG, "onDisconnected, code:" + code + "|desc:" + desc);
    }

    /**
     * WIFI需要验证
     *
     * @param name wifi名称
     */
    public void onWifiNeedAuth(String name) {
        TUIKitLog.v(TAG, "onWifiNeedAuth, wifi name:" + name);
    }

    /**
     * 部分会话刷新（包括多终端已读上报同步）
     *
     * @param conversations 需要刷新的会话列表
     */
    public void onRefreshConversation(List<V2TIMConversation> conversations) {
        TUIKitLog.v(TAG, "onRefreshConversation, size:" + (conversations != null ? conversations.size() : 0));
    }

    /**
     * 收到新消息回调
     */
    public void onNewMessage(V2TIMMessage timMessage) {
        TUIKitLog.v(TAG, "onNewMessage, msgID:" + (timMessage != null ? timMessage.getMsgID() : ""));
        if (timMessage == null
                || timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_HAS_DELETED
                || timMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_NONE) {
            TUIKitLog.e(TAG, "ele2MessageInfo parameters error");
            return;
        }
        final MessageInfo msgInfo = new MessageInfo();
        boolean isGroup = !TextUtils.isEmpty(timMessage.getGroupID());
        String sender = timMessage.getSender();
        msgInfo.setTimMessage(timMessage);
        msgInfo.setGroup(isGroup);
        msgInfo.setId(timMessage.getMsgID());
        msgInfo.setPeerRead(timMessage.isPeerRead());
        msgInfo.setFromUser(sender);
        if (isGroup) {
            if (!TextUtils.isEmpty(timMessage.getNameCard())) {
                msgInfo.setGroupNameCard(timMessage.getNameCard());
            }
        }
        msgInfo.setMsgTime(timMessage.getTimestamp());
        msgInfo.setSelf(sender.equals(V2TIMManager.getInstance().getLoginUser()));

        int type = timMessage.getElemType();
        if (type == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
            V2TIMCustomElem customElem = timMessage.getCustomElem();
            String data = new String(customElem.getData());
            if (isTyping(customElem.getData())) {
                // 忽略正在输入，它不能作为真正的消息展示
                return;
            }

            TUIKitLog.i(TAG, "custom data:" + data);
            String content = "[自定义消息]";
            msgInfo.setMsgType(MessageInfo.MSG_TYPE_CUSTOM);
            msgInfo.setExtra(content);
            Gson gson = new Gson();
            MessageCustom messageCustom = null;
            try {
                messageCustom = gson.fromJson(data, MessageCustom.class);
                String msgViewType = messageCustom.getType();
                if (!TextUtils.isEmpty(msgViewType) && msgViewType.equals(MessageCustom.MessageViewType.FUNCTIONAL)) {
                    if (timMessage.getSender().startsWith(Func.FUNC_PREFIX)) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_FUNCTION_CUSTOM);
                    } else {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_NORMAl_FUNCTION_CUSTOM);
                    }
                    ViewData viewData = gson.fromJson(data, ViewData.class);
                    msgInfo.setExtraData(viewData);
                    msgInfo.setExtra(viewData.getTitle());
                }
                if (!TextUtils.isEmpty(messageCustom.businessID) && messageCustom.businessID.equals(MessageCustom.BUSINESS_ID_GROUP_CREATE)) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_CREATE);
                    String message = TUIKitConstants.covert2HTMLString(messageCustom.opUser) + messageCustom.content;
                    msgInfo.setExtra(message);
                } else if (!TextUtils.isEmpty(messageCustom.event_type)) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_CUSTOM_BAOJING);
                    msgInfo.setExtra(messageCustom);
                } else if (!TextUtils.isEmpty(messageCustom.getRemoteUrl())) {
                    msgInfo.setMsgType(MessageInfo.MSG_TYPE_AUDIO_CUSTOM);
                    if (msgInfo.isSelf()) {
                        msgInfo.setDataPath(messageCustom.getLocalPath());
                    } else {
                        String url = messageCustom.getRemoteUrl();
                        final String path = TUIKitConstants.RECORD_DOWNLOAD_DIR + sender + "/";
                        final String name = url.substring(url.lastIndexOf("/") + 1);
                        File file = new File(path + name);
                        if (!file.exists()) {
                            DownloadUtils downloadUtils = new DownloadUtils(TUIKit.getAppContext());
                            downloadUtils.downloadFile(messageCustom.getRemoteUrl(), path, name, new DownloadListener() {
                                @Override
                                public void onStart() {
                                    Log.e(TAG, "onStart: ");
                                }

                                @Override
                                public void onProgress(final int currentLength) {
                                    Log.e(TAG, "onLoading: " + currentLength);
                                }

                                @Override
                                public void onFinish(String localPath) {
                                    Log.e(TAG, "onFinish: " + localPath);
                                    msgInfo.setDataPath(path + name);
                                }

                                @Override
                                public void onFailure(final String errorInfo) {
                                    Log.e(TAG, "onFailure: " + errorInfo);
                                }
                            });
                        } else {
                            msgInfo.setDataPath(path + name);
                        }
                    }
                    msgInfo.setExtra("[语音消息]");
                } else {
                    CallModel callModel = CallModel.convert2VideoCallData(timMessage);
                    if (callModel != null) {
                        String senderShowName = timMessage.getSender();
                        if (!TextUtils.isEmpty(timMessage.getNameCard())) {
                            senderShowName = timMessage.getNameCard();
                        } else if (!TextUtils.isEmpty(timMessage.getFriendRemark())) {
                            senderShowName = timMessage.getFriendRemark();
                        } else if (!TextUtils.isEmpty(timMessage.getNickName())) {
                            senderShowName = timMessage.getNickName();
                        }
                        switch (callModel.action) {
                            case CallModel.VIDEO_CALL_ACTION_DIALING:
                                content = isGroup ? ("\"" + senderShowName + "\"" + "发起群通话") : ("发起通话");
                                break;
                            case CallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL:
                                content = isGroup ? "取消群通话" : "取消通话";
                                break;
                            case CallModel.VIDEO_CALL_ACTION_LINE_BUSY:
                                content = isGroup ? ("\"" + senderShowName + "\"" + "忙线") : "对方忙线";
                                break;
                            case CallModel.VIDEO_CALL_ACTION_REJECT:
                                content = isGroup ? ("\"" + senderShowName + "\"" + "拒绝群通话") : "拒绝通话";
                                break;
                            case CallModel.VIDEO_CALL_ACTION_SPONSOR_TIMEOUT:
                                if (isGroup && callModel.invitedList != null && callModel.invitedList.size() == 1
                                        && callModel.invitedList.get(0).equals(timMessage.getSender())) {
                                    content = "\"" + senderShowName + "\"" + "无应答";
                                } else {
                                    StringBuilder inviteeShowStringBuilder = new StringBuilder();
                                    if (callModel.invitedList != null && callModel.invitedList.size() > 0) {
                                        for (String invitee : callModel.invitedList) {
                                            inviteeShowStringBuilder.append(invitee).append("、");
                                        }
                                        if (inviteeShowStringBuilder.length() > 0) {
                                            inviteeShowStringBuilder.delete(inviteeShowStringBuilder.length() - 1, inviteeShowStringBuilder.length());
                                        }
                                    }
                                    content = isGroup ? ("\"" + inviteeShowStringBuilder.toString() + "\"" + "无应答") : "无应答";
                                }
                                break;
                            case CallModel.VIDEO_CALL_ACTION_ACCEPT:
                                content = isGroup ? ("\"" + senderShowName + "\"" + "已接听") : "已接听";
                                break;
                            case CallModel.VIDEO_CALL_ACTION_HANGUP:
                                content = isGroup ? "结束群通话" : "结束通话，通话时长：" + DateTimeUtil.formatSecondsTo00(callModel.duration);
                                break;
                            default:
                                content = "不能识别的通话指令";
                                break;
                        }
                        if (isGroup) {
                            msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_AV_CALL_NOTICE);
                        }
                        msgInfo.setExtra(content);
                    }
                }
            } catch (Exception e) {
                TUIKitLog.e(TAG, "invalid json: " + data + ", exception:" + e);
            }
        } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_GROUP_TIPS) {
            V2TIMGroupTipsElem groupTipElem = timMessage.getGroupTipsElem();
            int tipsType = groupTipElem.getType();
            String user = "";
            if (groupTipElem.getMemberList().size() > 0) {
                List<V2TIMGroupMemberInfo> v2TIMGroupMemberInfoList = groupTipElem.getMemberList();
                for (int i = 0; i < v2TIMGroupMemberInfoList.size(); i++) {
                    V2TIMGroupMemberInfo v2TIMGroupMemberInfo = v2TIMGroupMemberInfoList.get(i);
                    String name = v2TIMGroupMemberInfo.getNickName();
                    if (StringUtils.isEmpty(name)) {
                        name = v2TIMGroupMemberInfo.getNameCard();
                    }
                    if (StringUtils.isEmpty(name)) {
                        name = v2TIMGroupMemberInfo.getUserID();
                    }
                    if (i == 0) {
                        user = user + name;
                    } else {
                        if (i == 2 && v2TIMGroupMemberInfoList.size() > 3) {
                            user = user + "等";
                            break;
                        } else {
                            user = user + "，" + name;
                        }
                    }
                }

            } else {
                String name = groupTipElem.getOpMember().getNickName();
                if (StringUtils.isEmpty(name)) {
                    name = groupTipElem.getOpMember().getNameCard();
                }
                if (StringUtils.isEmpty(name)) {
                    name = groupTipElem.getOpMember().getUserID();
                }
                user = name;
            }
            String message = TUIKitConstants.covert2HTMLString(user);
            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_JOIN) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_JOIN);
                message = message + "加入群组";
            }
            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_INVITE) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_JOIN);
                message = message + "被邀请进群";
            }
            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_QUIT) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_QUITE);
                message = message + "退出群组";
            }
            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_KICKED) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_KICK);
                message = message + "被踢出群组";
            }
            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_SET_ADMIN) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                message = message + "被设置管理员";
            }
            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_CANCEL_ADMIN) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                message = message + "被取消管理员";
            }
            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_GROUP_INFO_CHANGE) {
                List<V2TIMGroupChangeInfo> modifyList = groupTipElem.getGroupChangeInfoList();
                for (int i = 0; i < modifyList.size(); i++) {
                    V2TIMGroupChangeInfo modifyInfo = modifyList.get(i);
                    int modifyType = modifyInfo.getType();
                    if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_NAME) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NAME);
                        message = message + "修改群名称为\"" + modifyInfo.getValue() + "\"";
                    } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_NOTIFICATION) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                        message = message + "修改群公告为\"" + modifyInfo.getValue() + "\"";
                    } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_OWNER) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                        message = message + "转让群主给\"" + modifyInfo.getValue() + "\"";
                    } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_FACE_URL) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
//                        message = message + "修改了群头像";
                        return;
                    } else if (modifyType == V2TIMGroupChangeInfo.V2TIM_GROUP_INFO_CHANGE_TYPE_INTRODUCTION) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                        message = message + "修改群介绍为\"" + modifyInfo.getValue() + "\"";
                    }
                    if (i < modifyList.size() - 1) {
                        message = message + "、";
                    }
                }
            }
            if (tipsType == V2TIMGroupTipsElem.V2TIM_GROUP_TIPS_TYPE_MEMBER_INFO_CHANGE) {
                List<V2TIMGroupMemberChangeInfo> modifyList = groupTipElem.getMemberChangeInfoList();
                if (modifyList.size() > 0) {
                    long shutupTime = modifyList.get(0).getMuteTime();
                    if (shutupTime > 0) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                        message = message + "被禁言\"" + DateTimeUtil.formatSeconds(shutupTime) + "\"";
                    } else {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE);
                        message = message + "被取消禁言";
                    }
                }
            }
            if (TextUtils.isEmpty(message)) {
                return;
            }
            msgInfo.setExtra(message);
        } else {
            if (type == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT) {
                V2TIMTextElem txtEle = timMessage.getTextElem();
                msgInfo.setExtra(txtEle.getText());
            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_FACE) {
                V2TIMFaceElem faceElem = timMessage.getFaceElem();
                if (faceElem.getIndex() < 1 || faceElem.getData() == null) {
                    TUIKitLog.e("MessageInfoUtil", "faceElem data is null or index<1");
                    return;
                }
                msgInfo.setExtra("[自定义表情]");


            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_SOUND) {
                V2TIMSoundElem soundElemEle = timMessage.getSoundElem();
                if (msgInfo.isSelf()) {
                    msgInfo.setDataPath(soundElemEle.getPath());
                } else {
                    final String path = TUIKitConstants.RECORD_DOWNLOAD_DIR + soundElemEle.getUUID();
                    File file = new File(path);
                    if (!file.exists()) {
                        soundElemEle.downloadSound(path, new V2TIMDownloadCallback() {
                            @Override
                            public void onProgress(V2TIMElem.V2ProgressInfo progressInfo) {
                                long currentSize = progressInfo.getCurrentSize();
                                long totalSize = progressInfo.getTotalSize();
                                int progress = 0;
                                if (totalSize > 0) {
                                    progress = (int) (100 * currentSize / totalSize);
                                }
                                if (progress > 100) {
                                    progress = 100;
                                }
                                TUIKitLog.i("MessageInfoUtil getSoundToFile", "progress:" + progress);
                            }

                            @Override
                            public void onError(int code, String desc) {
                                TUIKitLog.e("MessageInfoUtil getSoundToFile", code + ":" + desc);
                            }

                            @Override
                            public void onSuccess() {
                                msgInfo.setDataPath(path);
                            }
                        });
                    } else {
                        msgInfo.setDataPath(path);
                    }
                }
                msgInfo.setExtra("[语音消息]");
            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE) {
                V2TIMImageElem imageEle = timMessage.getImageElem();
                String localPath = imageEle.getPath();
                if (msgInfo.isSelf() && !TextUtils.isEmpty(localPath)) {
                    msgInfo.setDataPath(localPath);
                    int size[] = ImageUtil.getImageSize(localPath);
                    msgInfo.setImgWidth(size[0]);
                    msgInfo.setImgHeight(size[1]);
                }
                //本地路径为空，可能为更换手机或者是接收的消息
                else {
                    List<V2TIMImageElem.V2TIMImage> imgs = imageEle.getImageList();
                    for (int i = 0; i < imgs.size(); i++) {
                        V2TIMImageElem.V2TIMImage img = imgs.get(i);
                        if (img.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_THUMB) {
                            final String path = TUIKitConstants.IMAGE_DOWNLOAD_DIR + img.getUUID();
                            msgInfo.setImgWidth(img.getWidth());
                            msgInfo.setImgHeight(img.getHeight());
                            File file = new File(path);
                            if (file.exists()) {
                                msgInfo.setDataPath(path);
                            }
                        }
                    }
                }
                msgInfo.setExtra("[图片]");
            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_VIDEO) {
                V2TIMVideoElem videoEle = timMessage.getVideoElem();
                if (msgInfo.isSelf() && !TextUtils.isEmpty(videoEle.getSnapshotPath())) {
                    int size[] = ImageUtil.getImageSize(videoEle.getSnapshotPath());
                    msgInfo.setImgWidth(size[0]);
                    msgInfo.setImgHeight(size[1]);
                    msgInfo.setDataPath(videoEle.getSnapshotPath());
                    msgInfo.setDataUri(FileUtil.getUriFromPath(videoEle.getVideoPath()));
                } else {
                    final String videoPath = TUIKitConstants.VIDEO_DOWNLOAD_DIR + videoEle.getVideoUUID();
                    Uri uri = Uri.parse(videoPath);
                    msgInfo.setDataUri(uri);
                    msgInfo.setImgWidth((int) videoEle.getSnapshotWidth());
                    msgInfo.setImgHeight((int) videoEle.getSnapshotHeight());
                    final String snapPath = TUIKitConstants.IMAGE_DOWNLOAD_DIR + videoEle.getSnapshotUUID();
                    //判断快照是否存在,不存在自动下载
                    if (new File(snapPath).exists()) {
                        msgInfo.setDataPath(snapPath);
                    }
                }

                msgInfo.setExtra("[视频]");
            } else if (type == V2TIMMessage.V2TIM_ELEM_TYPE_FILE) {
                V2TIMFileElem fileElem = timMessage.getFileElem();
                String filename = fileElem.getUUID();
                if (TextUtils.isEmpty(filename)) {
                    filename = System.currentTimeMillis() + fileElem.getFileName();
                }
                final String path = TUIKitConstants.FILE_DOWNLOAD_DIR + filename;
                File file = new File(path);
                if (file.exists()) {
                    if (msgInfo.isSelf()) {
                        msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                    } else {
                        msgInfo.setStatus(MessageInfo.MSG_STATUS_DOWNLOADED);
                    }
                    msgInfo.setDataPath(path);
                } else {
                    if (msgInfo.isSelf()) {
                        if (TextUtils.isEmpty(fileElem.getPath())) {
                            msgInfo.setStatus(MessageInfo.MSG_STATUS_UN_DOWNLOAD);
                            msgInfo.setDataPath(path);
                        } else {
                            file = new File(fileElem.getPath());
                            if (file.exists()) {
                                msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                                msgInfo.setDataPath(fileElem.getPath());
                            } else {
                                msgInfo.setStatus(MessageInfo.MSG_STATUS_UN_DOWNLOAD);
                                msgInfo.setDataPath(path);
                            }
                        }
                    } else {
                        msgInfo.setStatus(MessageInfo.MSG_STATUS_UN_DOWNLOAD);
                        msgInfo.setDataPath(path);
                    }
                }
                msgInfo.setExtra("[文件]");
            }
            int msgtype = TIMElemType2MessageInfoType(type);
            if (!msgInfo.isSelf() && msgtype == MessageInfo.MSG_TYPE_AUDIO) {
                msgtype = MessageInfo.MSG_TYPE_AUDIO_LEFT;
            }
            msgInfo.setMsgType(msgtype);
        }

        if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_LOCAL_REVOKED) {
            msgInfo.setStatus(MessageInfo.MSG_STATUS_REVOKE);
            msgInfo.setMsgType(MessageInfo.MSG_STATUS_REVOKE);
            if (msgInfo.isSelf()) {
                msgInfo.setExtra("您撤回了一条消息");
            } else if (msgInfo.isGroup()) {
                String message = TUIKitConstants.covert2HTMLString(msgInfo.getFromUser());
                msgInfo.setExtra(message + "撤回了一条消息");
            } else {
                msgInfo.setExtra("对方撤回了一条消息");
            }
        } else {
            if (msgInfo.isSelf()) {
                if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_SEND_FAIL) {
                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_FAIL);
                } else if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_SEND_SUCC) {
                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                } else if (timMessage.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_SENDING) {
                    msgInfo.setStatus(MessageInfo.MSG_STATUS_SENDING);
                }
            }
        }
        return;

    }
}
