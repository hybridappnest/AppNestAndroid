package com.ymy.core.bean

import java.io.Serializable

/**
 * Created on 2020/11/2 14:59.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
/**
 * 以弃用
 * @Deprecated
 */
@Deprecated("以弃用,V1版本中使用")
const val EVENT_COME_FROM_IM_TYPE_ALARM = "1"

/**
 * 以弃用
 * @Deprecated
 */
@Deprecated("以弃用,V1版本中使用")
const val EVENT_COME_FROM_IM_TYPE_YAYL = "2"

/**
 * im传递来的报警消息
 */
const val EVENT_COME_FROM_IM_TYPE_ALARM_V2 = "3"

data class YAYLAlarmInfo(
    val title: String = "",
    val address: String = "",
    val createdTime: Long = 0,
    val groupId: String = "",
) : Serializable

/**
 * 报警弹窗类型--报警
 */
const val alarm_come_channel_alarm = "alarm"

/**
 * 报警弹窗类型--预案演练
 */
const val alarm_come_channel_yjya = "emergency"

/**
 * 报警弹窗来源--模拟报警
 */
const val alarm_come_source_user = "user"

/**
 * 报警弹窗来源--报警
 */
const val alarm_come_source_system = "system"

data class AlarmComeInfo(
    val title: String = "",
    val address: String = "",
    val createdTime: String = "",
    val groupId: String = "",
    val level: Int = 1,
    val collectorType: Int = CollectorType.UNKNOWN,
    val senderId: String = "",
    val url: String = "",
    val source: String = "",
    val channel: String = "",
) : Serializable

object CollectorType {
    /**
     * 未知数值
     */
    const val UNKNOWN = 0

    /**
     * 消防主机
     */
    const val FIRE_HOST = 1

    /**
     *电器相关
     */
    const val ELECTRON = 2

    /**
     *水压
     */
    const val WATER_PRESSURE = 3

    /**
     *水深
     */
    const val WATER_DEPTH = 4

    /**
     *风压
     */
    const val WIND_PRESSURE = 5
}