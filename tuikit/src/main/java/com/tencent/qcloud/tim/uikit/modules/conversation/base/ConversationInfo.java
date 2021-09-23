package com.tencent.qcloud.tim.uikit.modules.conversation.base;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.tencent.qcloud.tim.uikit.modules.message.MessageInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConversationInfo implements Serializable, Comparable<ConversationInfo> {

    public static final int TYPE_COMMON = 1;
    public static final int TYPE_CUSTOM = 2;
    public static final String event_group_type_company = "company";
    public static final String event_group_type_department = "department";
    /**
     * 会话类型，自定义会话or普通会话
     */
    private int type;

    /**
     * 消息未读数
     */
    private int unRead;
    /**
     * 会话ID
     */
    private String conversationId;
    /**
     * 会话标识，C2C为对方用户ID，群聊为群组ID
     */
    private String id;
    /**
     * 会话头像url
     */
    private List<Object> iconUrlList = new ArrayList<>();

    public List<Object> getIconUrlList() {
        return iconUrlList;
    }

    public void setIconUrlList(List<Object> iconUrlList) {
        this.iconUrlList = iconUrlList;
    }

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话头像
     */
    private Bitmap icon;
    /**
     * 是否为群会话
     */
    private boolean isGroup;
    /**
     * 是否为置顶会话
     */
    private boolean top;
    /**
     * 最后一条消息时间
     */
    private long lastMessageTime;
    /**
     * 最后一条消息，MessageInfo对象
     */
    private MessageInfo lastMessage;
//    private String[] key = new String[]{"EVENT_ALL", "EVENT_BJ", "EVENT_GD", "EVENT_XJ", "EVENT_YHPC", "EVENT_YAYL", "EVENT_SJPX", "EVENT_ZKJJ"};
//    private String[] value = new String[]{"全部", "报警", "工单", "巡检", "隐患排查", "预案演练", "实践能力", "中控交接"};
    private String event_group_type;
    private String company_name;
    private String company_id;
    private String is_key_part;
    private String lable;

    public String getIs_key_part() {
        return is_key_part == null ? "" : is_key_part;
    }

    public void setIs_key_part(String is_key_part) {
        this.is_key_part = is_key_part;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getEvent_group_type() {
        return event_group_type;
    }

    public void setEvent_group_type(String event_group_type) {
        this.event_group_type = event_group_type;
    }
    public ConversationInfo() {

    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUnRead() {
        return unRead;
    }

    public void setUnRead(int unRead) {
        this.unRead = unRead;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    /**
     * 获得最后一条消息的时间，单位是秒
     */
    public long getLastMessageTime() {
        return lastMessageTime;
    }

    /**
     * 设置最后一条消息的时间，单位是秒
     * @param lastMessageTime
     */
    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLable() {
        return lable == null ? "" : lable;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

    public MessageInfo getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageInfo lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public int compareTo(@NonNull ConversationInfo other) {
        if(this.lastMessageTime == other.lastMessageTime){
            return 0;
        }
        return this.lastMessageTime > other.lastMessageTime ? -1 : 1;
    }

    @Override
    public String toString() {
        return "ConversationInfo{" +
                "type=" + type +
                ", unRead=" + unRead +
                ", conversationId='" + conversationId + '\'' +
                ", id='" + id + '\'' +
                ", iconUrl='" + iconUrlList.size() + '\'' +
                ", title='" + title + '\'' +
                ", icon=" + icon +
                ", isGroup=" + isGroup +
                ", top=" + top +
                ", lastMessageTime=" + lastMessageTime +
                ", lastMessage=" + lastMessage +
                '}';
    }
}
