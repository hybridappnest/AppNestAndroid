package com.tencent.qcloud.tim.uikit.modules.chat.layout.input;

/**
 * Created on 1/11/21 15:06.
 *
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

public class FunctionBtnData implements Comparable<FunctionBtnData> {
    /**
     * 跳转web页面
     */
    public static final int INVOKE_TYPE_WEB = 1;

    int invokeType = 0;
    String url = "";
    String text = "";
    int sort = 0;

    public int getInvokeType() {
        return invokeType;
    }

    public void setInvokeType(int invokeType) {
        this.invokeType = invokeType;
    }

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int compareTo(FunctionBtnData other) {
        return this.sort - other.sort;
    }
}