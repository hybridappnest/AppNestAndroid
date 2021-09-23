package com.tencent.qcloud.tim.uikit.modules.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created on 1/12/21 09:08.
 *
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

public class Entry implements Serializable {
    @SerializedName("key")
    String key = "";
    @SerializedName("value")
    String value = "";

    public String getKey() {
        return key == null ? "" : key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value == null ? "" : value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
