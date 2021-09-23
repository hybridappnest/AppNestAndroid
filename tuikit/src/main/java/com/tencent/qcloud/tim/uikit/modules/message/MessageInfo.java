package com.tencent.qcloud.tim.uikit.modules.message;

import android.net.Uri;
import android.text.TextUtils;

import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;

import java.util.UUID;


public class MessageInfo {
    private final String TAG = "MessageInfo";

    public static final int MSG_TYPE_MIME = 0x1;

    /**
     * 文本类型消息
     */
    public static final int MSG_TYPE_TEXT = 0x00;
    /**
     * 图片类型消息
     */
    public static final int MSG_TYPE_IMAGE = 0x20;
    /**
     * 语音类型消息
     */
    public static final int MSG_TYPE_AUDIO = 0x30;
    /**
     * 语音类型消息
     */
    public static final int MSG_TYPE_AUDIO_LEFT = 0x31;
    /**
     * 视频类型消息
     */
    public static final int MSG_TYPE_VIDEO = 0x40;
    /**
     * 文件类型消息
     */
    public static final int MSG_TYPE_FILE = 0x50;
    /**
     * 位置类型消息
     */
    public static final int MSG_TYPE_LOCATION = 0x60;

    /**
     * 自定义图片类型消息
     */
    public static final int MSG_TYPE_CUSTOM_FACE = 0x70;
    /**
     * 自定义消息
     */
    public static final int MSG_TYPE_CUSTOM = 0x80;

    /**
     * 自定义消息消息 系统 纯文本
     */
    public static final int MSG_TYPE_CUSTOM_TEXT = 0x81;
    /**
     * 自定义消息 报警
     */
    public static final int MSG_TYPE_CUSTOM_BAOJING = 0x82;
    /**
     * 自定义消息 自定义音频消息
     */
    public static final int MSG_TYPE_AUDIO_CUSTOM = 0x83;
    /**
     * 自定义消息 功能号消息
     */
    public static final int MSG_TYPE_FUNCTION_CUSTOM = 0x84;
    /**
     * 自定义消息 普通群聊 功能号消息
     */
    public static final int MSG_TYPE_NORMAl_FUNCTION_CUSTOM = 0x85;


    /**
     * 自定义消息 接警
     */
    public static final int MSG_TYPE_CUSTOM_JIEJING = 0x80;

    /**
     * 提示类信息
     */
    public static final int MSG_TYPE_TIPS = 0x100;
    /**
     * 群创建提示消息
     */
    public static final int MSG_TYPE_GROUP_CREATE = 0x101;
    /**
     * 群解散提示消息
     */
    public static final int MSG_TYPE_GROUP_DELETE = 0x102;
    /**
     * 群成员加入提示消息
     */
    public static final int MSG_TYPE_GROUP_JOIN = 0x103;
    /**
     * 群成员退群提示消息
     */
    public static final int MSG_TYPE_GROUP_QUITE = 0x104;
    /**
     * 群成员被踢出群提示消息
     */
    public static final int MSG_TYPE_GROUP_KICK = 0x105;
    /**
     * 群名称修改提示消息
     */
    public static final int MSG_TYPE_GROUP_MODIFY_NAME = 0x106;
    /**
     * 群通知更新提示消息
     */
    public static final int MSG_TYPE_GROUP_MODIFY_NOTICE = 0x107;
    /**
     * 群音视频呼叫提示消息
     */
    public static final int MSG_TYPE_GROUP_AV_CALL_NOTICE = 0x108;

    /**
     * 消息未读状态
     */
    public static final int MSG_STATUS_READ = 0x111;
    /**
     * 消息删除状态
     */
    public static final int MSG_STATUS_DELETE = 0x112;
    /**
     * 消息撤回状态
     */
    public static final int MSG_STATUS_REVOKE = 0x113;
    /**
     * 消息正常状态
     */
    public static final int MSG_STATUS_NORMAL = 0;
    /**
     * 消息发送中状态
     */
    public static final int MSG_STATUS_SENDING = 1;
    /**
     * 消息发送成功状态
     */
    public static final int MSG_STATUS_SEND_SUCCESS = 2;
    /**
     * 消息发送失败状态
     */
    public static final int MSG_STATUS_SEND_FAIL = 3;
    /**
     * 消息内容下载中状态
     */
    public static final int MSG_STATUS_DOWNLOADING = 4;
    /**
     * 消息内容未下载状态
     */
    public static final int MSG_STATUS_UN_DOWNLOAD = 5;
    /**
     * 消息内容已下载状态
     */
    public static final int MSG_STATUS_DOWNLOADED = 6;

    private String id = UUID.randomUUID().toString();
    private long uniqueId = 0;
    private String fromUser;
    private String groupNameCard;
    private int msgType;
    private int status = MSG_STATUS_NORMAL;
    private boolean self;
    private boolean read;
    private boolean group;
    private Uri dataUri;
    private String dataPath;
    private Object extra;
    private long msgTime;
    private int imgWidth;
    private int imgHeight;
    private boolean peerRead;
    private boolean isSelect;//是否选择
    private boolean isShowSelect;//是否展示选择
    private V2TIMMessage timMessage;
    private String sendUserName;
    /**
     * 额外参数，用于自定义itemView的数据承载
     */
    private Object extraData;

    public String getSendUserName() {
        return sendUserName;
    }

    public void setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
    }

    /**
     * 获取消息唯一标识
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 设置消息唯一标识
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    public long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(long uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * 获取消息发送方 ID
     *
     * @return
     */
    public String getFromUser() {
        return fromUser;
    }

    /**
     * 设置消息发送方 ID
     *
     * @param fromUser
     */
    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    /**
     * 获取群名片
     *
     * @return
     */
    public String getGroupNameCard() {
        return groupNameCard;
    }

    /**
     * 设置群名片
     *
     * @param groupNameCard
     */
    public void setGroupNameCard(String groupNameCard) {
        this.groupNameCard = groupNameCard;
    }

    /**
     * 获取消息类型
     *
     * @return
     */
    public int getMsgType() {
        return msgType;
    }

    /**
     * 设置消息类型
     *
     * @param msgType
     */
    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    /**
     * 获取消息发送状态
     *
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置消息发送状态
     *
     * @param status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 获取消息是否为登录用户发送
     *
     * @return
     */
    public boolean isSelf() {
        return self;
    }

    /**
     * 设置消息是否是登录用户发送
     *
     * @param self
     */
    public void setSelf(boolean self) {
        this.self = self;
    }

    /**
     * 获取消息是否已读
     *
     * @return
     */
    public boolean isRead() {
        return read;
    }

    /**
     * 设置消息已读
     *
     * @param read
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * 获取消息是否为群消息
     *
     * @return
     */
    public boolean isGroup() {
        return group;
    }

    /**
     * 设置消息是否为群消息
     *
     * @param group
     */
    public void setGroup(boolean group) {
        this.group = group;
    }

    /**
     * 获取多媒体消息的数据源
     *
     * @return
     */
    public Uri getDataUri() {
        return dataUri;
    }

    /**
     * 设置多媒体消息的数据源
     *
     * @param dataUri
     */
    public void setDataUri(Uri dataUri) {
        this.dataUri = dataUri;
    }

    /**
     * 获取多媒体消息的保存路径
     *
     * @return
     */
    public String getDataPath() {
        return dataPath;
    }

    /**
     * 设置多媒体消息的保存路径
     *
     * @param dataPath
     */
    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public int getCustomInt() {
        if (timMessage == null) {
            return 0;
        }
        return timMessage.getLocalCustomInt();
    }

    public void setCustomInt(int value) {
        if (timMessage == null) {
            return;
        }
        timMessage.setLocalCustomInt(value);
    }

    public boolean checkEquals(String msgID) {
        if (TextUtils.isEmpty(msgID)) {
            return false;
        }
        return timMessage.getMsgID().equals(msgID);
    }

    public boolean remove() {
        if (timMessage == null) {
            return false;
        }
        V2TIMManager.getMessageManager().deleteMessageFromLocalStorage(timMessage, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                TUIKitLog.e(TAG, "deleteMessageFromLocalStorage error code = " + code + ", desc = " + desc);
            }

            @Override
            public void onSuccess() {
            }
        });
        return true;
    }

    /**
     * 获取SDK的消息bean
     *
     * @return
     */
    public V2TIMMessage getTimMessage() {
        return timMessage;
    }

    /**
     * 设置SDK的消息bean
     *
     * @param timMessage
     */
    public void setTimMessage(V2TIMMessage timMessage) {
        this.timMessage = timMessage;
    }

    /**
     * 非文字消息在会话列表时展示的文字说明，比如照片在会话列表展示为“[图片]”
     *
     * @return
     */
    public Object getExtra() {
        return extra;
    }

    /**
     * 设置非文字消息在会话列表时展示的文字说明，比如照片在会话列表展示为“[图片]”
     *
     * @param extra
     */
    public void setExtra(Object extra) {
        this.extra = extra;
    }

    /**
     * 获取图片或者视频缩略图的图片宽
     *
     * @return
     */
    public int getImgWidth() {
        return imgWidth;
    }

    /**
     * 设置图片或者视频缩略图的图片宽
     *
     * @param imgWidth
     */
    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    /**
     * 获取图片或者视频缩略图的图片高
     *
     * @return
     */
    public int getImgHeight() {
        return imgHeight;
    }

    /**
     * 设置图片或者视频缩略图的图片高
     *
     * @param imgHeight
     */
    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public boolean isShowSelect() {
        return isShowSelect;
    }

    public void setShowSelect(boolean showSelect) {
        isShowSelect = showSelect;
    }

    /**
     * 获取消息发送时间，单位是秒
     *
     * @return
     */
    public long getMsgTime() {
        return msgTime;
    }

    /**
     * 设置消息发送时间，单位是秒
     *
     * @param msgTime
     */
    public void setMsgTime(long msgTime) {
        this.msgTime = msgTime;
    }

    public boolean isPeerRead() {
        return peerRead;
    }

    public void setPeerRead(boolean peerRead) {
        this.peerRead = peerRead;
    }

    public Object getExtraData() {
        return extraData;
    }

    public void setExtraData(Object extraData) {
        this.extraData = extraData;
    }
}
