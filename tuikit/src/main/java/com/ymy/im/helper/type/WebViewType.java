package com.ymy.im.helper.type;

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
@StringDef(value = {WebViewType.EVENT_DESC,
        WebViewType.ALARM_HISTORY,
        WebViewType.EVENT_MOVELINE,
        WebViewType.ALARM_TIMELINE,
        WebViewType.ALARM_SJPX_TEST,
        WebViewType.ALARM_YANLIAN,})
public @interface WebViewType {
    /**
     * 事件详情
     */
    String EVENT_DESC = "event_desc";

    /**
     * 报警历史
     */
    String ALARM_HISTORY = "alarm_history";
    /**
     * 报警移动轨迹
     */
    String EVENT_MOVELINE = "event_moveline";
    /**
     * 报警时间轴
     */
    String ALARM_TIMELINE = "alarm_timeline";
    /**
     * 演练
     */
    String ALARM_YANLIAN = "alarm_yanLian";
    /**
     * 交接
     */
    String ALARM_JIAOJIE = "alarm_jiaoJie";
    /**
     * 实践能力测试1
     */
    String ALARM_SJPX_TEST = "alarm_sjpx_test";
}