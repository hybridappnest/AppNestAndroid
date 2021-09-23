package com.ymy.core.user

import androidx.room.*
import com.ymy.core.database.TableConst
import com.ymy.core.database.converters.CompanyConverters
import com.ymy.core.database.converters.PermissionCodesConverters
import java.io.Serializable

@Entity(tableName = TableConst.userInfoTableName)
@TypeConverters(CompanyConverters::class, PermissionCodesConverters::class)
data class UserInfoDB(
    @PrimaryKey
    @ColumnInfo(name = "id")
    //用户主账户Id
    var accountId: Long = 0L,
    //用户id
    var userId: String = "",
    //用户imId用于im登录
    var imId: String = "",
    //用户imSign用于im登录
    var imSign: String = "",
    //用户账号
    var account: String = "",
    //用户头像
    var avatarUrl: String = "",
    //用户性别，1男，0女
    var gender: Int = 0,
    //用户身份证
    var idCardNo: String = "",
    //用户昵称
    var nickname: String = "",
    //用户电话
    var phone: String = "",
    //用户真名
    var realName: String = "",
    //用户token
    var token: String = "",
    //企业id，切换企业时替换
    var companyId: Int = 0,

    var companyName: String = "",

    //TypeConverters会转json序列化，反序列化时会转为对象
    var companies: MutableList<CompanyInfo> = mutableListOf(),

    //TypeConverters会转json序列化，反序列化时会转为对象
    var permissionCodes: MutableList<Long> = mutableListOf(),

    ) : Serializable {
    @Ignore
    constructor() : this(0)

    override fun equals(other: Any?): Boolean {
        return if (other is UserInfoDB) {
            this.accountId == other.accountId
        } else false
    }
}

data class CompanyInfo(
    //公司id
    val companyId: Int = 0,
    //公司名
    val companyName: String = "",
    //公司昵称
    var nickname: String = "",
    //是否为公司领导
    var summit: Boolean = false,
    //状态
    val status: Int = 0,
    //地图id
    var mapId: String = "",
    //地图定位id
    var mapLocationId: String = "",

    ) : Serializable {
    override fun equals(other: Any?): Boolean {
        return if (other is CompanyInfo) {
            this.companyId == other.companyId
        } else false
    }
}