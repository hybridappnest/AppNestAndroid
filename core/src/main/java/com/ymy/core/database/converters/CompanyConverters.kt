package com.ymy.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.ymy.core.ok3.GsonUtils
import com.ymy.core.user.CompanyInfo

/**
 * Created on 2020/9/10 10:21.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class CompanyConverters {

    @TypeConverter
    fun stringToObject(value: String): MutableList<CompanyInfo> {
        val listType = object : TypeToken<MutableList<CompanyInfo>>() {}.type
        if (value.isEmpty()) {
            return mutableListOf()
        }
        return GsonUtils.mGson.fromJson(value, listType)
    }

    @TypeConverter
    fun objectToString(list: MutableList<CompanyInfo>): String {
        return GsonUtils.mGson.toJson(list)
    }
}