package com.ymy.appnest.ui.setting.viewmodel

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ymy.core.base.BaseViewModel
import com.ymy.appnest.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class UserModifyViewModel constructor(val repository: UserRepository) : BaseViewModel() {

    /**
     * 网络请求LiveData
     */
    private val netUiMutableLiveData = MutableLiveData<UiState<String>>()
    val netUiLiveData: LiveData<UiState<String>> get() = netUiMutableLiveData

    private fun emitLoginUiState(
        isLoading: Boolean = false,
        isRefresh: Boolean = false,
        isSuccess: String? = null,
        isError: String? = null,
    ) {
        netUiMutableLiveData.value = UiState(
            isLoading,
            isRefresh,
            isSuccess,
            isError,
        )
    }

    /**
     * 修改头像昵称
     * @param nickname String
     * @param avatarUrl String
     */
    @Keep
    fun modifyUserInfo(nickname: String = "",avatarUrl: String ="") {
        emitLoginUiState(isLoading = true)
        viewModelScope.launch(Dispatchers.Main) {
//            val job = async(Dispatchers.IO) {
//                repository.modifyUserInfo(nickname,avatarUrl)
//            }
//            val result = job.await()
//            result.checkResult(
//                onSuccess = {
//                    emitLoginUiState(
//                        isLoading = false,
//                        isSuccess = "")
//                },
//                onError = {
//                    emitLoginUiState(
//                        isLoading = false,
//                        isError = it)
//                })
        }
    }
}