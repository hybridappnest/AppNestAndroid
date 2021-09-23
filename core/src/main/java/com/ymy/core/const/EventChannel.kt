package com.ymy.core.const

/**
 * Created on 4/9/21 10:36.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
interface EventChannel {
    //    /**
//     * 报警，包含消防主机、压力异常、用户手动发布、用户模拟等所有形式的消防告警
//     */
//    ALARM("alarm", "报警"),
//
//    /**
//     * 启动预案演练，包含因火势较大或失控后启动的应急响应机制和客户企业举行的常规预案演练
//     */
//    EMERGENCY("emergency", "预案演练"),
//
//    /**
//     * 消防主机记录
//     */
//    ENGINE_LOG("engine-log", "消防主机记录"),
//
//    /**
//     * 设备设施的周期性巡检
//     */
//    INSPECT("inspect", "设备巡检"),
//
//    /**
//     * 重要、重点区域的周期性巡查
//     */
//    PATROL("patrol", "重点区域巡查"),
//
//    /**
//     * 工单，包含技术支持、维保等各类通用工单
//     */
//    WORK_ORDER("work-order", "工单"),
//
//    /**
//     * 隐患排查，包含周期性的常规排查和突击性集中排查
//     */
//    TROUBLE_SHOOT("trouble-shoot", "隐患排查"),
//
//    /**
//     * 责任人交接，包含中控交接等岗位交接班事件
//     */
//    HANDOVER("handover", "交接"),
//
//    /**
//     * 消防培训
//     */
//    TRAIN("train", "消防培训"),
//
//    /**
//     * 消防记录
//     */
//    FIRE_RECORD("fire-record", "消防记录"),
//
//    /**
//     * 通知通告
//     */
//    NOTICE("notice", "通知通告"),
//
//    /**
//     * 室内地图
//     */
//    MAP("map", "室内地图");
    companion object {
        const val event_channel_alarm = "alarm"
        const val event_channel_emergency = "emergency"
        const val event_channel_engine_log = "engine-log"
        const val event_channel_inspect = "inspect"
        const val event_channel_patrol = "patrol"
        const val event_channel_work_order = "work-order"
        const val event_channel_trouble_shoot = "trouble-shoot"
        const val event_channel_handover = "handover"
        const val event_channel_fire_record = "fire-record"
        const val event_channel_notice = "notice"
        const val event_channel_map = "map"
    }
}