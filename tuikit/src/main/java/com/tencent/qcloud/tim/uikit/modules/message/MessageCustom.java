package com.tencent.qcloud.tim.uikit.modules.message;

import java.util.ArrayList;

public class MessageCustom {
    public static final String BUSINESS_ID_GROUP_CREATE = "group_create";
    public static final String BUSINESS_ID_AV_CALL = "av_call";

    public int version = 0;
    public String businessID;
    public String opUser;
    public String content;
    public  String event_id;
    public  String event_type;
    public  String title;
    public  String desc;
    public String mark;   //是否需要弹窗 1 需要 0 不 需要

    public  Action action;
    String localPath;
    String remoteUrl;
    int duration;
    String discernResult;//翻译信息

    /**
     * 消息类型
     * @see MessageViewType
     */
    String type;

    public interface MessageViewType {
        String FUNCTIONAL = "functional";
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDiscernResult() {
        return discernResult;
    }

    public void setDiscernResult(String discernResult) {
        this.discernResult = discernResult;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public ArrayList<Event_property> event_property;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getBusinessID() {
        return businessID;
    }

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

    public String getOpUser() {
        return opUser;
    }

    public void setOpUser(String opUser) {
        this.opUser = opUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Event_property> getEvent_property() {
        return event_property;
    }

    public void setEvent_property(ArrayList<Event_property> event_property) {
        this.event_property = event_property;
    }
    public class Action{
        public String action_type;
        public String changeEventType;
        public Params params;

        public Params getParams() {
            return params;
        }

        public void setParams(Params params) {
            this.params = params;
        }

        public String getAction_type() {
            return action_type;
        }

        public String getChangeEventType() {
            return changeEventType == null ? "" : changeEventType;
        }

        public void setChangeEventType(String changeEventType) {
            this.changeEventType = changeEventType;
        }

        public void setAction_type(String action_type) {
            this.action_type = action_type;
        }
    }
    public class Params{
        public  String h5_url;
        public  String userId;
        public  String status;
        public  String handoverId;
        /**
         * 单位秒，注意！！！
         */
        public  long createdTime;
        public  String address;
        public String imGroupId;

        public String getH5_url() {
            return h5_url;
        }

        public void setH5_url(String h5_url) {
            this.h5_url = h5_url;
        }

        public String getUserId() {
            return userId == null ? "" : userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getStatus() {
            return status == null ? "" : status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getHandoverId() {
            return handoverId == null ? "" : handoverId;
        }

        public void setHandoverId(String handoverId) {
            this.handoverId = handoverId;
        }

        public long getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(long createdTime) {
            this.createdTime = createdTime;
        }

        public String getAddress() {
            return address == null ? "" : address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getImGroupId() {
            return imGroupId == null ? "" : imGroupId;
        }

        public void setImGroupId(String imGroupId) {
            this.imGroupId = imGroupId;
        }
    }
}
