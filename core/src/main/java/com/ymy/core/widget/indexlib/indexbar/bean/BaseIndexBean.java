package com.ymy.core.widget.indexlib.indexbar.bean;

import com.ymy.core.widget.indexlib.suspension.ISuspensionInterface;

import java.io.Serializable;

/**
 * 介绍：索引类的标志位的实体基类
 */

public abstract class BaseIndexBean implements ISuspensionInterface, Serializable {

    private String baseIndexTag;//所属的分类（名字的汉语拼音首字母）

    public String getBaseIndexTag() {
        return baseIndexTag;
    }

    public BaseIndexBean setBaseIndexTag(String baseIndexTag) {
        this.baseIndexTag = baseIndexTag;
        return this;
    }

    @Override
    public String getSuspensionTag() {
        return baseIndexTag;
    }

    @Override
    public boolean isShowSuspension() {
        return true;
    }
}
