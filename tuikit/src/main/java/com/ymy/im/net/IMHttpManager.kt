package com.ymy.im.net

import com.ymy.im.net.client.IMServiceClient
import com.ymy.im.net.repository.IMRepository
import com.ymy.core.ok3.DBXRetrofitClient
import com.ymy.core.ok3.checkResult
import com.ymy.core.user.YmyUserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Created on 3/24/21 17:30.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object IMHttpManager {
    val repository: IMRepository = IMRepository(
        DBXRetrofitClient.getService(
            IMServiceClient::class.java,
            ""
        )
    )

    interface CallBack {
        fun callSuccess(any: Any? = null)
        fun callError()
    }

    @JvmStatic
    fun getIMSign(callBack: CallBack) {
        GlobalScope.launch(Dispatchers.Main) {
            val job = async(Dispatchers.IO) {
                repository.getImSign()
            }
            val result = job.await()
            result.checkResult(
                onSuccess = {
                    YmyUserManager.setUserImSign("")
                    callBack.callSuccess(YmyUserManager.user.imSign)
                },
                onError = {
                    callBack.callSuccess(YmyUserManager.user.imSign)
                })
        }
    }

    @JvmStatic
    fun addGroupMember(group_name: String, member_userids: String, callBack: CallBack) {
        GlobalScope.launch(Dispatchers.Main) {
            val job = async(Dispatchers.IO) {
                repository.addGroupMember(group_name, member_userids)
            }
            val result = job.await()
            result.checkResult(
                onSuccess = {
                    callBack.callSuccess()
                },
                onError = {
                    callBack.callError()
                })
        }
    }

    @JvmStatic
    fun deleteGroupMember(group_name: String, member_userids: String, callBack: CallBack) {
        GlobalScope.launch(Dispatchers.Main) {
            val job = async(Dispatchers.IO) {
                repository.deleteGroupMember(group_name, member_userids)
            }
            val result = job.await()
            result.checkResult(
                onSuccess = {
                    callBack.callSuccess()
                },
                onError = {
                    callBack.callError()
                })
        }
    }
}