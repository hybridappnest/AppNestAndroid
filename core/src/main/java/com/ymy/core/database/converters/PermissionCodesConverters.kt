package com.ymy.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.ymy.core.ok3.GsonUtils

/**
 * Created on 2020/9/10 10:21.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class PermissionCodesConverters {

    @TypeConverter
    fun stringToObject(value: String): MutableList<Long> {
        val listType = object : TypeToken<MutableList<Long>>() {}.type
        if (value.isEmpty()) {
            return mutableListOf()
        }
        return GsonUtils.mGson.fromJson(value, listType)
    }

    @TypeConverter
    fun objectToString(list: MutableList<Long>): String {
        return GsonUtils.mGson.toJson(list)
    }
}