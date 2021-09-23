package com.ymy.appnest.net

import com.google.gson.JsonArray
import com.ymy.core.ok3.base.QuickBaseResponse
import com.ymy.appnest.net.model.AppConfigReq
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created on 2020/9/9 15:36.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

interface QuickApiClient {

    @POST("/quickApi/getAppConfig")
    suspend fun getAppConfig(@Body params: AppConfigReq): QuickBaseResponse<JsonArray>

}

interface UserServiceClient {

//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/login")
//    suspend fun userLogin(
//        @Body params: AppUserReqParams,
//    ): BaseResponse<LoginAccountResponseV2>
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/switch/{companyId}")
//    suspend fun switchCompany(
//        @Path("companyId") id: Int
//    ): BaseResponse<LoginUserResponseV2>
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/user/info")
//    suspend fun refreshUserInfo(): BaseResponse<LoginUserResponseV2>
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/sendVerificationCode")
//    suspend fun sendVerificationCode(
//        @Body params: GetVerificationCodeReq,
//    ): BaseResponse<GetVarCodeResponse>
//
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/modifyUserInfo")
//    suspend fun modifyUserInfo(
//        @Body params: ModifyUserInfoReq,
//    ): BaseResponse<EmptyResponse>
//
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/modifyForget")
//    suspend fun modifyForget(
//        @Body params: ModifyForgetPassWordReq,
//    ): BaseResponse<String>
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/checkVerifyCode")
//    suspend fun checkVarCode(
//        @Body params: ModifyForgetPassWordReq,
//    ): BaseResponse<Boolean>
//
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/modifyMobile")
//    suspend fun modifyMobile(
//        @Body params: ModifyMobileReq,
//    ): BaseResponse<EmptyResponse>
//
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/modifyPassword")
//    suspend fun modifyPassword(
//        @Body params: ModifyPasswordReq,
//    ): BaseResponse<JsonElement>
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/sysUserCompany/getPageList")
//    suspend fun getUserCompanyList(
//        @Body params: GetUserCompanyReq,
//    ): BaseResponse<JsonArray>
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/common/addAlarmGroup")
//    suspend fun addAlarmGroup(
//        @Body params: AddAlarmGroupReq,
//    ): BaseResponse<Boolean>
//
//    @Headers(("Domain-Name: $javaHost"))
//    @POST("/api/webchat/bind ")
//    suspend fun webChatBindingPhone(
//        @Body params: WeChatBindingPhoneReq,
//    ): BaseResponse<LoginAccountResponseV2>
}
