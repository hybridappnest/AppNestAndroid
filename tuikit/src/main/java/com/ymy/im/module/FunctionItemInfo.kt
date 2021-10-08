package com.ymy.im.module

import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo
import com.ymy.core.bean.FunctionInfo
import java.io.Serializable

/**
 * Created on 1/16/21 09:15.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
data class CompanyFunctionData(
    val companyId: String = "",
    val companyName: String = "",
    val functionList: MutableList<FunctionInfo> = mutableListOf(),
    var functionItemList: MutableList<FunctionItemInfo> = mutableListOf()
) : Serializable

data class FunctionItemInfo(
    val imId: String,
    val functionInfo: FunctionInfo = FunctionInfo(),
    var conversationInfo: ConversationInfo? = null,
    var avater: String = "",
    var name: String = "",
) {
    fun initShowData() {
        var icon = ""
        var title = ""
        conversationInfo?.run {
            if (iconUrlList.isNotEmpty()) {
                icon = iconUrlList[0].toString()
            }
            if (this.title.isNotEmpty()) {
                title = this.title
            }
        }
        avater = when {
            functionInfo.avatarUrl.isNotEmpty() -> {
                functionInfo.avatarUrl
            }
            icon.isNotEmpty() -> {
                icon
            }
            else -> {
//                getAvatarByChannel(functionInfo.channel)
                ""
            }
        }
        name = when {
            functionInfo.title.isNotEmpty() -> {
                functionInfo.title
            }
            title.isNotEmpty() -> {
                title
            }
            else -> {
                ""
            }
        }
    }

//    private fun getAvatarByChannel(channel: String): String {
//
//    }

}
