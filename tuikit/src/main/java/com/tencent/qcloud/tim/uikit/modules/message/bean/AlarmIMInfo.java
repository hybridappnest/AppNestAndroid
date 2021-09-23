package com.tencent.qcloud.tim.uikit.modules.message.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created on 2/8/21 11:13.
 *
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
public class AlarmIMInfo implements Serializable {
    /**
     * address : 中银2层大堂
     * isSound : true
     * principal : cd022fb5a4ec4c73,9405a8e7098444d5,e247755ad56744b4
     * source : system
     * time : 21-01-30 08:25:43
     * title : 中银2层大堂烟感null
     * type : 点型感烟探测器
     */
    @SerializedName("address")
    private String address;
    @SerializedName("isSound")
    private Boolean isSound;
    @SerializedName("principal")
    private String principal;
    @SerializedName("source")
    private String source;
    @SerializedName("time")
    private String time;
    @SerializedName("title")
    private String title;
    @SerializedName("type")
    private String type;
    @SerializedName("collectorType")
    private int collectorType;
    @SerializedName("level")
    private int level;
    @SerializedName("users")
    private String users;
    @SerializedName("url")
    private String url;
    @SerializedName("channel")
    private String channel;

    public String getAddress() {
        return address == null ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getSound() {
        return isSound;
    }

    public void setSound(Boolean sound) {
        isSound = sound;
    }

    public String getPrincipal() {
        return principal == null ? "" : principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getSource() {
        return source == null ? "" : source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTime() {
        return time == null ? "" : time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCollectorType() {
        return collectorType;
    }

    public void setCollectorType(int collectorType) {
        this.collectorType = collectorType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getUsers() {
        return users == null ? "" : users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChannel() {
        return channel == null ? "" : channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
