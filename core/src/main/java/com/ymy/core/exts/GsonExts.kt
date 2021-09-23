package com.ymy.core.exts

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

/**
 * Created on 2020/8/19 09:05.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
//reified标示的泛型会在编译期进行具体化，须配合inline函数使用
//inline函数在编译后会被合并到具体的调用位置中，由reified标示的泛型也会被具体为需要的数据类型
inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, object : TypeToken<T>() {}.type)
}

//reified标示的泛型会在编译期进行具体化，须配合inline函数使用
//inline函数在编译后会被合并到具体的调用位置中，由reified标示的泛型也会被具体为需要的数据类型
inline fun <reified T> Gson.fromJson(json: JsonElement): T {
    return fromJson(json, object : TypeToken<T>() {}.type)
}