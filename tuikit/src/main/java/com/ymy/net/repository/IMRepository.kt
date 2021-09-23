package com.ymy.net.repository

import com.ymy.net.client.IMServiceClient
import com.ymy.net.request.IMRequestInfo
import com.ymy.net.response.IMSignResp
import com.google.gson.JsonElement
import com.ymy.core.ok3.HttpResult
import com.ymy.core.ok3.base.BaseRepository


/**
 * Created on 2020/7/17 14:15.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class IMRepository constructor(val service: IMServiceClient) : BaseRepository() {

    /**
     * 加人进群
     * @return HttpResult<EmptyResponse>
     */
    suspend fun addGroupMember(group_name: String, member_userids: String): HttpResult<JsonElement> {
        val params = IMRequestInfo(group_name,member_userids)
        return safeApiCall(
            call = { getAddGroupMember(params) }
        )
    }

    private suspend fun getAddGroupMember(params: IMRequestInfo): HttpResult<JsonElement> {
        val response = service.addGroupMember(params)
        return executeResponse(response)
    }

    /**
     * 删除人员
     * @return HttpResult<EmptyResponse>
     */
    suspend fun deleteGroupMember(group_name: String, member_userids: String): HttpResult<JsonElement> {
        val params = IMRequestInfo(group_name,member_userids)
        return safeApiCall(
            call = { getDeleteGroupMember(params) }
        )
    }

    private suspend fun getDeleteGroupMember(params: IMRequestInfo): HttpResult<JsonElement> {
        val response = service.deleteGroupMember(params)
        return executeResponse(response)
    }

    /**
     * 获取用户sign
     * @return HttpResult<IMSignResp>
     */
    suspend fun getImSign(): HttpResult<IMSignResp> {
        return safeApiCall(
            call = { doGetImSign() }
        )
    }

    private suspend fun doGetImSign(): HttpResult<IMSignResp> {
        val response = service.getImSign()
        return executeResponse(response)
    }

}