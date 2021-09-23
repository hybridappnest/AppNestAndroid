package com.ymy.core.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created on 1/15/21 10:02.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
const val function_jump_type_custom = "custom"
const val function_jump_type_normal = "normal"

data class FunctionInfo(
    @SerializedName("avatarUrl")
    val avatarUrl: String = "",
    @SerializedName("channel")
    val channel: String = "",
    @SerializedName("closeable")
    val closeable: Boolean = false,
    @SerializedName("companyId")
    val companyId: Int = 0,
    @SerializedName("description")
    val description: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("imId")
    val imId: String = "",
    @SerializedName("menus")
    val menus: Any? = null,
    @SerializedName("sort")
    val sort: Int = 0,
    @SerializedName("status")
    val status: Int = 0,
    @SerializedName("url")
    val url: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("jumpType")
    val jumpType: String = "",
    @SerializedName("unsubscribable")
    val unsubscribable: Boolean = false
) : Serializable, Comparable<FunctionInfo> {
    override fun compareTo(other: FunctionInfo): Int {
        return this.sort - other.sort
    }
}