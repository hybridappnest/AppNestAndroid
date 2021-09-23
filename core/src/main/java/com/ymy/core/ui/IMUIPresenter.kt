package com.ymy.core.ui

import com.ymy.core.Ktx
import com.ymy.core.R

/**
 * Created on 2020/8/19 18:31.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object IMUIPresenter {
    @JvmStatic
    fun getAlarmLevelColor(level: Int): Int {
        return when (level) {
            1 ->
                Ktx.app.resources.getColor(R.color.redDF2024)
            2 ->
                Ktx.app.resources.getColor(R.color.orangeFE7100)
            3 ->
                Ktx.app.resources.getColor(R.color.yellowEEE401)
            4 ->
                Ktx.app.resources.getColor(R.color.blue1389E8)
            else ->
                Ktx.app.resources.getColor(R.color.green00EFC1)
        }
    }

    @JvmStatic
    fun getWorkOrderLevelColor(level: Int): Int {
        return when (level) {
            1 ->
                Ktx.app.resources.getColor(R.color.orangeFE7100)
            2 ->
                Ktx.app.resources.getColor(R.color.blue1389E8)
            else ->
                Ktx.app.resources.getColor(R.color.green00EFC1)
        }
    }

    @JvmStatic
    fun getActingLevelColor(level: Int): Int {
        return when (level) {
            1 ->
                Ktx.app.resources.getColor(R.color.green00EFC1)
            2 ->
                Ktx.app.resources.getColor(R.color.blue1389E8)
            3 ->
                Ktx.app.resources.getColor(R.color.yellowEEE401)
            4 ->
                Ktx.app.resources.getColor(R.color.orangeFE7100)
            else ->
                Ktx.app.resources.getColor(R.color.redDF2024)
        }
    }

    @JvmStatic
    fun getTrainingLevelColor(level: Int): Int {
        return Ktx.app.resources.getColor(R.color.blue1389E8)
    }

    @JvmStatic
    fun getZKJJLevelColor(level: Int): Int {
        return Ktx.app.resources.getColor(R.color.blue1389E8)
    }

    @JvmStatic
    fun getYHPCLevelColor(level: Int): Int {
        return Ktx.app.resources.getColor(R.color.orangeFE7100)
    }

    @JvmStatic
    fun getXJLevelColor(level: Int): Int {
        return when (level) {
            2 ->
                Ktx.app.resources.getColor(R.color.orangeFE7100)
            3 ->
                Ktx.app.resources.getColor(R.color.yellowEEE401)
            4 ->
                Ktx.app.resources.getColor(R.color.blue1389E8)
            else ->
                Ktx.app.resources.getColor(R.color.green00EFC1)
        }
    }


    @JvmStatic
    fun getAlarmStateStr(status: Int?): String {
        return when (status) {
            1 -> "待接警"
            2 -> "已接警"
            3 -> "已到位"
            4 -> "已处理"
            else -> "已关闭"
        }
    }

    @JvmStatic
    fun getWorkOrderStateStr(status: Int): String {
        return when (status) {
            1 -> "待接受"
            2 -> "已接受"
            3 -> "已处理"
            else -> "已关闭"
        }
    }

    @JvmStatic
    fun getXJStateStr(status: Int?): String {
        return when (status) {
            -1 -> "重点区域巡检"
            1 -> "待巡检"
            2 -> "巡检中"
            else -> "巡检完毕"
        }
    }

    @JvmStatic
    fun getYHPCStateStr(status: Int?): String {
        return when (status) {
            1 -> "待排查"
            2 -> "排查中"
            else -> "排查完毕"
        }
    }

    @JvmStatic
    fun getYAYLStateStr(status: Int?): String {
        return when (status) {
            1 -> "未开始"
            2 -> "进行中"
            else -> "已结束"
        }
    }


}