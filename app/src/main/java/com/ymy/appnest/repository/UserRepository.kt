package com.ymy.appnest.repository

import com.ymy.core.ok3.base.BaseRepository
import com.ymy.appnest.net.UserServiceClient


/**
 * Created on 2020/7/17 14:15.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

class UserRepository constructor(val service: UserServiceClient) : BaseRepository() {

    /**
//     * 用户名登录
//     * @param params AppUserLoginRequest
//     * @return HttpResult<UserResponse>
//     */
//    suspend fun login(params: AppUserReqParams): HttpResult<LoginAccountResponseV2> {
//        return safeApiCall(
//            call = {
//                requestLogin(params)
//            },
//            errorMessage = "登录异常"
//        )
//    }
//
//    private suspend fun requestLogin(params: AppUserReqParams): HttpResult<LoginAccountResponseV2> {
//        val baseResponse = service.userLogin(params)
//        return executeLoginResponse(baseResponse)
//    }
//
//    /**
//     * 处理登录完成后的数据
//     * @param baseAccountResponse BaseResponse<UserResponse>
//     * @return HttpResult<UserResponse>
//     */
//    private suspend fun executeLoginResponse(baseAccountResponse: BaseResponse<LoginAccountResponseV2>): HttpResult<LoginAccountResponseV2> {
////        if (baseResponse.code == CODE_200) {
////            saveUserFromResponse(baseResponse.body)
////        }
//        return executeResponse(baseAccountResponse)
//    }
//
//
//    /**
//     * 获取验证码
//     * @param mobile String
//     * @return HttpResult<UserResponse>
//     */
//    suspend fun getSendVerificationCode(mobile: String): HttpResult<GetVarCodeResponse> {
//        val params = GetVerificationCodeReq(mobile)
//        return safeApiCall(
//            call = {
//                getSendVerificationCodePost(params)
//            }
//        )
//    }
//
//    private suspend fun getSendVerificationCodePost(params: GetVerificationCodeReq): HttpResult<GetVarCodeResponse> {
//        val baseResponse = service.sendVerificationCode(params)
//        return executeResponse(baseResponse)
//    }
//
//    /**
//     * 修改昵称头像
//     * @param nickname String
//     * @param avatarUrl String
//     * @return HttpResult<UserResponse>
//     */
//    suspend fun modifyUserInfo(nickname: String, avatarUrl: String): HttpResult<EmptyResponse> {
//        val params = ModifyUserInfoReq(nickname, avatarUrl)
//        return safeApiCall(
//            call = {
//                modifyUserInfoPost(params)
//            }
//        )
//    }
//
//    private suspend fun modifyUserInfoPost(
//        params: ModifyUserInfoReq,
//    ): HttpResult<EmptyResponse> {
//        val baseResponse = service.modifyUserInfo(params)
//        return executeResponse(baseResponse)
//    }
//
//    /**
//     * 忘记密码，手机验证码修改密码
//     * @param mobile String
//     * @param password String
//     * @param code String
//     * @return HttpResult<EmptyResponse>
//     */
//    suspend fun modifyForgetPassWord(
//        mobile: String = "",
//        password: String = "",
//        code: String = "",
//        getVarCodeToken: String = "",
//    ): HttpResult<String> {
//        val params =
//            ModifyForgetPassWordReq(
//                mobile,
//                MD5Utils.md5Encrypt32Lower("dingbaox$password"),
//                code,
//                getVarCodeToken
//            )
//        return safeApiCall(
//            call = {
//                modifyForgetPassWordPost(params)
//            }
//        )
//    }
//
//    private suspend fun modifyForgetPassWordPost(
//        params: ModifyForgetPassWordReq,
//    ): HttpResult<String> {
//        val baseResponse = service.modifyForget(params)
//        return executeResponse(baseResponse)
//    }
//
//    /**
//     * 检查验证码是否正确
//     * @param mobile String
//     * @param code String
//     * @return HttpResult<EmptyResponse>
//     */
//    suspend fun checkVarCode(
//        mobile: String = "",
//        code: String = "",
//        getVarCodeToken: String = "",
//    ): HttpResult<Boolean> {
//        val params = ModifyForgetPassWordReq(
//            mobile, "",
//            code,
//            getVarCodeToken
//        )
//        return safeApiCall(
//            call = {
//                checkVarCodePost(params)
//            }
//        )
//    }
//
//    private suspend fun checkVarCodePost(
//        params: ModifyForgetPassWordReq,
//    ): HttpResult<Boolean> {
//        val baseResponse = service.checkVarCode(params)
//        return executeResponse(baseResponse)
//    }
//
//    /**
//     * 修改密码
//     * @param oldPassword String
//     * @param password String
//     * @return HttpResult<EmptyResponse>
//     */
//    suspend fun modifyPassword(
//        oldPassword: String = "",
//        password: String = "",
//    ): HttpResult<JsonElement> {
//        val params = ModifyPasswordReq(
//            MD5Utils.md5Encrypt32Lower("dingbaox$oldPassword"),
//            MD5Utils.md5Encrypt32Lower("dingbaox$password")
//        )
//        return safeApiCall(
//            call = {
//                modifyPasswordPost(params)
//            }
//        )
//    }
//
//    private suspend fun modifyPasswordPost(
//        params: ModifyPasswordReq,
//    ): HttpResult<JsonElement> {
//        val baseResponse = service.modifyPassword(params)
//        return executeResponse(baseResponse)
//    }
//
//    /**
//     * 修改手机
//     * @param mobile String
//     * @param code String
//     * @return HttpResult<EmptyResponse>
//     */
//    suspend fun modifyMobile(
//        mobile: String = "",
//        code: String = "",
//        getVarCodeToken: String = "",
//    ): HttpResult<EmptyResponse> {
//        val params = ModifyMobileReq(mobile, code, getVarCodeToken)
//        return safeApiCall(
//            call = {
//                modifyMobilePost(params)
//            }
//        )
//    }
//
//    private suspend fun modifyMobilePost(
//        params: ModifyMobileReq,
//    ): HttpResult<EmptyResponse> {
//        val baseResponse = service.modifyMobile(params)
//        return executeResponse(baseResponse)
//    }
//
//    /**
//     * 获取用户相关企业列表
//     * @param mobile String
//     * @param code String
//     * @return HttpResult<EmptyResponse>
//     */
//    suspend fun getCompanyList(
//    ): HttpResult<JsonArray> {
//        val params = GetUserCompanyReq()
//        return safeApiCall(
//            call = {
//                getCompanyListPost(params)
//            }
//        )
//    }
//
//    private suspend fun getCompanyListPost(params: GetUserCompanyReq): HttpResult<JsonArray> {
//        val baseResponse = service.getUserCompanyList(params)
//        return executeResponse(baseResponse)
//    }
//
//    /**
//     * 刷新用户数据
//     * @return HttpResult<LoginResponseV2>
//     */
//    suspend fun refreshUserInfo(): HttpResult<LoginUserResponseV2> {
//        return safeApiCall(
//            call = {
//                refreshUserInfoPost()
//            },
//            errorMessage = "获取用户数据异常"
//        )
//    }
//
//    private suspend fun refreshUserInfoPost(): HttpResult<LoginUserResponseV2> {
//        val baseResponse = service.refreshUserInfo()
//        return executeResponse(baseResponse)
//    }
//
//
//    /**
//     * 切换公司
//     * @return HttpResult<LoginResponseV2>
//     */
//    suspend fun switchUserCompany(companyId:Int): HttpResult<LoginUserResponseV2> {
//        return safeApiCall(
//            call = {
//                doSwitchUserCompany(companyId)
//            },
//            errorMessage = "获取用户数据异常"
//        )
//    }
//
//    private suspend fun doSwitchUserCompany(companyId:Int): HttpResult<LoginUserResponseV2> {
//        val baseResponse = service.switchCompany(companyId)
//        return executeResponse(baseResponse)
//    }
//
//    /**
//     * 微信登录绑定手机号
//     * @param code String
//     * @param phone String
//     * @param unicode String
//     * @return HttpResult<LoginUserResponseV2>
//     */
//    suspend fun webChatBindingPhone(unicode:String, getVarCodeToken: String,code:String,phone:String): HttpResult<LoginAccountResponseV2> {
//        return safeApiCall(
//            call = {
//                doWebChatBindingPhone(WeChatBindingPhoneReq(unicode,getVarCodeToken,code,phone))
//            },
//            errorMessage = "获取用户数据异常"
//        )
//    }
//
//    private suspend fun doWebChatBindingPhone(req: WeChatBindingPhoneReq): HttpResult<LoginAccountResponseV2> {
//        val baseResponse = service.webChatBindingPhone(req)
//        return executeResponse(baseResponse)
//    }
}