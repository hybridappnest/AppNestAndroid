package com.ymy.im

import android.text.TextUtils
import com.ymy.im.function.Func
import com.ymy.im.module.CompanyFunctionData
import com.ymy.im.module.FunctionItemInfo
import com.orhanobut.logger.Logger
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.qcloud.tim.uikit.modules.conversation.base.ConversationInfo
import com.tencent.qcloud.tim.uikit.modules.conversation.holder.ConversationCommonHolder
import com.ymy.core.bean.FunctionInfo
import com.ymy.core.permission.Weak
import com.ymy.core.user.YmyUserManager

/**
 * Created on 1/15/21 16:45.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object IMConversationManager {

    var mRefreshListener: RefreshListener? by Weak<RefreshListener>()

    interface RefreshListener {
        fun dataChange(needReloadIMList: Boolean = false)
    }

    val mTopFuncList = listOf<String>(Func.FUNC_ALARM, Func.FUNC_PATROL, Func.FUNC_THRESHOLD)

    /**
     * 所有功能号数据map
     */
    val mAllFunctionMap: MutableMap<String, FunctionItemInfo> = mutableMapOf()

    /**
     * 公司功能号数据列表，用于页面展示
     */
    val companyFunctionList: MutableList<CompanyFunctionData> = mutableListOf()

    /**
     * 公司-功能号列表Map
     */
    private val companyConversationMap: MutableMap<String, MutableList<ConversationInfo>> =
        mutableMapOf()


    var currentCompany: CompanyFunctionData? = null

    var currentCompanyFunctionList: MutableList<FunctionInfo> = mutableListOf()
        set(value) {
            field.clear()
            field.addAll(value)
        }

    /**
     * 设置用户公司功能数据
     * @param list MutableList<CompanyFunctionData>
     */
    fun setUserCompanyData(list: MutableList<CompanyFunctionData>) {
        list.forEach {
            val companyFunctionItemList = mutableListOf<FunctionItemInfo>()
            it.functionList.forEach { item ->
                item.run {
                    val functionItemInfo = FunctionItemInfo(imId, this)
                    functionItemInfo.initShowData()
                    mAllFunctionMap[item.imId] = functionItemInfo
                    companyFunctionItemList.add(functionItemInfo)
                }
            }
            it.functionItemList = companyFunctionItemList
        }
        companyFunctionList.clear()
        companyFunctionList.addAll(list)

        var currentCompany: CompanyFunctionData? = null
        companyFunctionList.forEach {
            if (TextUtils.equals(it.companyId, YmyUserManager.user.companyId.toString())) {
                currentCompany = it
            }
        }
        companyFunctionList.remove(currentCompany)
        currentCompany?.let {
            companyFunctionList.add(0, it)
            IMConversationManager.currentCompany = it
        }
        mRefreshListener?.dataChange(true)
    }

    /**
     * 需要统计公司相关的所有群组的未读数，显示的功能号+公司群+部门群，
     * **注意需要过滤掉不显示的功能号**
     * @param companyId String
     * @return Int
     */
    @JvmStatic
    fun getHideCompanyUnread(companyId: String): Int {
        var unReadCount = 0
        companyConversationMap[companyId]?.run {
            this.forEach {
                val conversationId = it.id
                if (conversationId.startsWith(Func.FUNC_PREFIX)) {
                    //过滤掉不显示的未读数
                    val functionItemInfo = mAllFunctionMap[it.id]
                    if (functionItemInfo != null) {
                        unReadCount += it.unRead
                    }
                } else {
                    unReadCount += it.unRead
                }
            }
        }
        return unReadCount
    }

    @JvmStatic
    fun getAllHideCompanyUnread(): Int {
        var unReadCount = 0
        val userCompanies = YmyUserManager.getUserCompanies()
        val currentCompanyId = YmyUserManager.user.companyId.toString()
        companyConversationMap.forEach { entry ->
            val conversationCompanyId = entry.key
            if (conversationCompanyId != currentCompanyId) {
                if (userCompanies.contains(conversationCompanyId)) {
                    entry.value.forEach {
                        val conversationId = it.id
                        if (conversationId.startsWith(Func.FUNC_PREFIX)) {
                            //过滤掉不显示的未读数
                            val functionItemInfo = mAllFunctionMap[it.id]
                            if (functionItemInfo != null) {
                                unReadCount += it.unRead
                            }
                        } else {
                            unReadCount += it.unRead
                        }
                    }
                }
//                else {
//                    deleteLocalConversation(entry.value)
//                }
            }
        }
        return unReadCount
    }

    /**
     * 处理用户被从公司中删除，遗留下的被删除的公司的本地会话
     * @param value MutableList<ConversationInfo>
     */
    private fun deleteLocalConversation(value: MutableList<ConversationInfo>) {
        value.forEach {
            //检测本地群不存在，删除
            V2TIMManager.getConversationManager()
                .deleteConversation(it.conversationId, null)
        }
    }

    /**
     * 只统计当前公司显示的功能号的未读数
     * @return Int
     */
    @JvmStatic
    fun getCurrentCompanyFunctionUnread(): Int {
        var unReadCount = 0
        currentCompany?.run {
            this.functionItemList.forEach {
                it.conversationInfo?.run {
                    unReadCount += this.unRead
                }
            }
        }
        return unReadCount
    }

    private fun addCompanyConversationInfo(companyId: String, item: ConversationInfo) {
        val companyConversationList = companyConversationMap[companyId]
        if (companyConversationList == null) {
            companyConversationMap[companyId] = mutableListOf(item)
        } else {
            companyConversationMap[companyId]?.add(item)
        }
    }

    @JvmStatic
    fun filterShowList(
        dataSource: MutableList<ConversationInfo>
    ): MutableList<ConversationInfo> {
        val showData = mutableListOf<ConversationInfo>()
        val topShowData = mutableListOf<ConversationInfo>()
        if (mAllFunctionMap.isEmpty()) {
            return showData
        }
        companyConversationMap.clear()
        dataSource.forEach { element ->
            val conversationId = element.id
            if (conversationId.startsWith(Func.FUNC_PREFIX)) {
                val split = conversationId.split("_")
                val funcCompanyId = split[2]
                addCompanyConversationInfo(funcCompanyId, element)
                val functionItemInfo = mAllFunctionMap[conversationId]
                if (functionItemInfo != null) {
                    if (funcCompanyId == YmyUserManager.user.companyId.toString()) {
                        if (element.iconUrlList.isNotEmpty()) {
                            val any = element.iconUrlList[0]
                            if (!any.toString().startsWith("http")) {
                                element.iconUrlList.clear()
                                element.iconUrlList.add(functionItemInfo.avater)
                            }
                        }
                        if (element.title == conversationId && element.title != functionItemInfo.functionInfo.title) {
                            element.title = functionItemInfo.functionInfo.title
                        }
                        val prefix = "${split[0]}_${split[1]}"
                        if (mTopFuncList.contains(prefix)) {
                            element.lable = ConversationCommonHolder.topLabel
                            topShowData.add(element)
                        } else {
                            showData.add(element)
                        }
                    }
                }
            } else {
                val companyId = element.company_id
                if (companyId.isNullOrEmpty()) {
                    showData.add(element)
                } else {
                    if (companyId == YmyUserManager.user.companyId.toString()) {
                        showData.add(element)
                    } else {
                        addCompanyConversationInfo(companyId, element)
                    }
                }
            }
        }
        if (topShowData.isNotEmpty()) {
            showData.addAll(0, topShowData)
        }
        Logger.e("刷新列表数据")
        return showData
    }

    @JvmStatic
    fun callRefresh() {
        mRefreshListener?.dataChange()
    }

    @JvmStatic
    fun filterFunctionConversation(dataSource: MutableList<ConversationInfo>): List<ConversationInfo> {
        return dataSource.filter {
            !it.id.startsWith(Func.FUNC_PREFIX)
        }
    }

}