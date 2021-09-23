package com.tencent.qcloud.tim.uikit.modules.message;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created on 1/12/21 09:08.
 *
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

public class Expand implements Serializable {
    public static final String DATA_TYPE_ALARM_IM_INFO = "alarm_im_info";

    @SerializedName("expand1")
    String expand1 = "";
    @SerializedName("dataType")
    String dataType = "";
    @SerializedName("data")
    JsonObject data = null;

    public String getExpand1() {
        return expand1 == null ? "" : expand1;
    }

    public void setExpand1(String expand1) {
        this.expand1 = expand1;
    }

    public String getDataType() {
        return dataType == null ? "" : dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
