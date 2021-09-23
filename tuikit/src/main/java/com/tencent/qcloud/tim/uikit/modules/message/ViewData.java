package com.tencent.qcloud.tim.uikit.modules.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created on 1/12/21 09:05.
 *
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

public class ViewData implements Serializable {

    @SerializedName("channel")
    String channel = "";
    @SerializedName("desc")
    String desc = "";
    @SerializedName("entrys")
    ArrayList<Entry> entrys = new ArrayList<Entry>();
    @SerializedName("eventId")
    int eventId = 0;
    @SerializedName("expand")
    Expand expand = new Expand();
    @SerializedName("status")
    int status = 0;
    @SerializedName("time")
    Long time = 0L;
    @SerializedName("title")
    String title = "";
    @SerializedName("type")
    String type = "";
    @SerializedName("url")
    String url = "";

    public String getChannel() {
        return channel == null ? "" : channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDesc() {
        return desc == null ? "" : desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public ArrayList<Entry> getEntrys() {
        if (entrys == null) {
            return new ArrayList<>();
        }
        return entrys;
    }

    public void setEntrys(ArrayList<Entry> entrys) {
        this.entrys = entrys;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Expand getExpand() {
        return expand;
    }

    public void setExpand(Expand expand) {
        this.expand = expand;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
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

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
