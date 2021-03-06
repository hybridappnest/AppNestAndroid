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
            1 -> "?????????"
            2 -> "?????????"
            3 -> "?????????"
            4 -> "?????????"
            else -> "?????????"
        }
    }

    @JvmStatic
    fun getWorkOrderStateStr(status: Int): String {
        return when (status) {
            1 -> "?????????"
            2 -> "?????????"
            3 -> "?????????"
            else -> "?????????"
        }
    }

    @JvmStatic
    fun getXJStateStr(status: Int?): String {
        return when (status) {
            -1 -> "??????????????????"
            1 -> "?????????"
            2 -> "?????????"
            else -> "????????????"
        }
    }

    @JvmStatic
    fun getYHPCStateStr(status: Int?): String {
        return when (status) {
            1 -> "?????????"
            2 -> "?????????"
            else -> "????????????"
        }
    }

    @JvmStatic
    fun getYAYLStateStr(status: Int?): String {
        return when (status) {
            1 -> "?????????"
            2 -> "?????????"
            else -> "?????????"
        }
    }


}