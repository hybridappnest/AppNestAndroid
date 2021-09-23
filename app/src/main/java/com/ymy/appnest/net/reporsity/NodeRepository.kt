package com.ymy.appnest.net.reporsity

import com.google.gson.JsonArray
import com.ymy.core.ok3.HttpResult
import com.ymy.core.ok3.base.QuickBaseRepository
import com.ymy.appnest.net.QuickApiClient
import com.ymy.appnest.net.model.AppConfigReq


/**
 * Created on 2021/8/25 14:15.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class NodeRepository constructor(private val service: QuickApiClient) : QuickBaseRepository() {

    suspend fun getAppConfig(): HttpResult<JsonArray> {
        return safeApiCall(
            call = { doGetAppConfig() }
        )
    }

    private suspend fun doGetAppConfig(): HttpResult<JsonArray> {
        val response = service.getAppConfig(AppConfigReq())
        return executeResponse(response)
    }
}