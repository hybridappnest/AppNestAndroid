package com.ymy.helper.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.StringDef;

/**
 * Created on 2020/8/25 09:40.
 *
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER})
@StringDef(value = {
        EventType.TYPE_ALARM,
        EventType.TYPE_WORKORDER,
        EventType.TYPE_XJ,
        EventType.TYPE_YHPC,
        EventType.TYPE_YAYL,
        EventType.TYPE_SJPX,
        EventType.TYPE_ZKJJ})
public @interface EventType {
    /**
     * 报警类型
     */
    String TYPE_ALARM = "EVENT_BJ";
    /**
     * 工单类型
     */
    String TYPE_WORKORDER = "EVENT_GD";
    /**
     * 巡检
     */
    String TYPE_XJ = "EVENT_XJ";
    /**
     * 隐患排查
     */
    String TYPE_YHPC = "EVENT_YHPC";
    /**
     * 预案演练
     */
    String TYPE_YAYL = "EVENT_YAYL";
    /**
     * 实践训练
     */
    String TYPE_SJPX = "EVENT_SJPX";
    /**
     * 中控交接
     */
    String TYPE_ZKJJ = "EVENT_ZKJJ";

    /**
     * 普通群
     */
    String TYPE_NORMAL = "GROUP_NORMAL";
}