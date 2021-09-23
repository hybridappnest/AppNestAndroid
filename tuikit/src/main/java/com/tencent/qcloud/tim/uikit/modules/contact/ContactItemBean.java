package com.tencent.qcloud.tim.uikit.modules.contact;

import android.text.TextUtils;

import com.tencent.imsdk.v2.V2TIMFriendInfo;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.qcloud.tim.uikit.component.indexlib.IndexBar.bean.BaseIndexPinyinBean;

public class ContactItemBean extends BaseIndexPinyinBean {

    public static final String INDEX_STRING_TOP = "↑";
    private String id;
    private boolean isTop;//是否是最上面的 不需要被转化成拼音的
    private boolean isSelected;
    private boolean isBlackList;
    private String remark;
    private String nickname;
    private String avatarurl;
    private boolean isGroup;
    private boolean isFriend = true;
    private boolean isEnable = true;
    private String event_group_type;

    public String getEvent_group_type() {
        return event_group_type;
    }

    public void setEvent_group_type(String event_group_type) {
        this.event_group_type = event_group_type;
    }

    public ContactItemBean() {
    }

    public ContactItemBean(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public ContactItemBean setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isTop() {
        return isTop;
    }

    public ContactItemBean setTop(boolean top) {
        isTop = top;
        return this;
    }

    @Override
    public String getTarget() {
        if (!TextUtils.isEmpty(remark)) {
            return remark;
        }
        if (!TextUtils.isEmpty(nickname)) {
            return nickname;
        }
        return id;
    }

    @Override
    public boolean isNeedToPinyin() {
        return !isTop;
    }

    @Override
    public boolean isShowSuspension() {
        return !isTop;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public boolean isBlackList() {
        return isBlackList;
    }

    public void setBlackList(boolean blacklist) {
        isBlackList = blacklist;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public ContactItemBean covertTIMFriend(V2TIMFriendInfo friendInfo) {
        if (friendInfo == null) {
            return this;
        }
        setId(friendInfo.getUserID());
        setRemark(friendInfo.getFriendRemark());
        setNickname(friendInfo.getUserProfile().getNickName());
        setAvatarurl(friendInfo.getUserProfile().getFaceUrl());
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    public ContactItemBean covertTIMGroupBaseInfo(V2TIMGroupInfo group) {
        if (group == null) {
            return this;
        }
        setId(group.getGroupID());
        setRemark(group.getGroupName());
        setAvatarurl(group.getFaceUrl());
        setGroup(true);
        return this;
    }
}
