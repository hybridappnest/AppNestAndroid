package com.ymy.net.client

import com.ymy.net.request.IMRequestInfo
import com.ymy.net.response.IMSignResp
import com.google.gson.JsonElement
import com.ymy.core.ok3.base.BaseResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created on 3/24/21 15:49.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

interface IMServiceClient {
    @POST("/api/v3/common/deleteGroupMember")
    suspend fun deleteGroupMember(@Body params: IMRequestInfo): BaseResponse<JsonElement>

    @POST("/api/v3/common/addGroupMember")
    suspend fun addGroupMember(@Body params: IMRequestInfo): BaseResponse<JsonElement>

    @POST("/api/v3/common/getImSign")
    suspend fun getImSign(): BaseResponse<IMSignResp>
}