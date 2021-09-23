package com.ymy.core.user

import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.ymy.core.Ktx
import com.ymy.core.database.AppUserDatabase
import com.ymy.core.database.dao.UserInfoDao
import com.ymy.core.utils.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Created on 2020/7/16 17:30.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object YmyUserManager {
    const val TAG = "YmyUserManager"

    var user: UserInfoDB = UserInfoDB()

    var token: String = ""

    private val mainScope = MainScope()

    private var userAccountId: Long by Preference(Preference.USER_ACCOUNT_ID, 0L)

    fun isLogin() = user.accountId != 0L && user.imId.isNotEmpty()

    private val USER_INFO_DAO: UserInfoDao =
        AppUserDatabase.getInstance(Ktx.app).userInfoDao()

    private lateinit var permissionList: MutableList<Long>

    fun initUser() {
        mainScope.launch(Dispatchers.IO) {
            user = USER_INFO_DAO.getUser(userAccountId) ?: UserInfoDB(0)
            initUserData()
            Logger.e("getUser:user:${getUserJson()}")
        }
    }

    private fun getUserJson(): String? {
        return Gson().toJson(user)
    }

    /**
     * 删除用户，插入一个临时空白用户
     */
    fun deleteUser() {
        mainScope.launch(Dispatchers.IO) {
            USER_INFO_DAO.deleteUser(user.accountId)
            Logger.e("deleteUser:userId = ${user.userId}")
        }
        saveUserInfo(UserInfoDB())
    }

    private fun userInfoChanged() {
        mainScope.launch(Dispatchers.IO) {
            USER_INFO_DAO.insertUser(user)
            Logger.e("userInfoChanged:${getUserJson()}")
        }
    }

    fun setUserAvatarUrl(avatarUrl: String) {
        user.avatarUrl = avatarUrl
        userInfoChanged()
    }

    fun setUserNickname(nickname: String) {
        user.nickname = nickname
        userInfoChanged()
    }

    fun setUserImSign(imSign: String) {
        user.imSign = imSign
        userInfoChanged()
    }

    fun checkPermission(checkPermission: Long): Boolean {
        return if (::permissionList.isInitialized) {
            permissionList.contains(DBXPermission.app_all) or permissionList.contains(
                checkPermission
            )
        } else {
            false
        }
    }


    fun setUserPhone(mobile: String) {
        user.phone = mobile
        userInfoChanged()
    }


    fun setUserCompany(companyId: Int, companyName: String) {
        user.companyId = companyId
        user.companyName = companyName
        userInfoChanged()
    }

    fun setUserInfo(
        mobile: String,
        idCardNo: String,
        gender: Int,
        nickname: String,
        realName: String,
        avatarUrl: String,
        permissions: MutableList<Long>,
        companies: MutableList<CompanyInfo>,
        companyName: String,
    ) {
        user.phone = mobile
        user.idCardNo = idCardNo
        user.gender = gender
        user.nickname = nickname
        user.realName = realName
        user.avatarUrl = avatarUrl
        user.permissionCodes = permissions
        user.companies = companies
        user.companyName = companyName
        initUserData()
        userInfoChanged()
    }

    /**
     * 地图id
     */
    var mapId = ""

    /**
     * 定位Id
     */
    var mapLocationId = ""

    /**
     * 保存用户信息
     */
    fun saveUserInfo(newUserDB: UserInfoDB) {
        user = newUserDB
        initUserData()
        userInfoChanged()
    }

    /**
     * 初始化用户数据
     */
    private fun initUserData() {
        token = user.token
        userAccountId = user.accountId
        permissionList = user.permissionCodes
        user.run {
            if (companyId != 0 && companies.isNotEmpty()) {
                companies.forEach {
                    if (it.companyId == companyId) {
                        mapId = it.mapId
                        mapLocationId = it.mapLocationId
                    }
                }
            }
        }
    }

    fun getUserCompanies(): MutableList<String> {
        val result = mutableListOf<String>()
        user.companies.forEach {
            result.add(it.companyId.toString())
        }
        return result
    }
}

