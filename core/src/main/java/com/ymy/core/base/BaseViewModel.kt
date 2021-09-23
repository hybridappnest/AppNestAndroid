package com.ymy.core.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created on 2020/7/10 08:46.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
open class BaseViewModel : ViewModel() {

    val TAG = this::class.java.simpleName
    private val msgMutableLiveData = MutableLiveData<MsgUiState>()
    val msgLiveData: LiveData<MsgUiState> get() = msgMutableLiveData

    open class UiState<T>(
        var isLoading: Boolean = false,
        var isRefresh: Boolean = false,
        var isSuccess: T? = null,
        var isError: String? = null
    )


    open class BaseUiModel<T>(
        var showLoading: Boolean = false,
        var showError: String? = null,
        var showSuccess: T? = null,
        var showEnd: Boolean = false, // 加载更多
        var isRefresh: Boolean = false // 刷新
    )


    /**
     * 展示Toast
     */
    protected fun showToast(str: String,isSuccess:Boolean = true) {
        msgMutableLiveData.value = MsgUiState(str,isSuccess)
    }

    val mException: MutableLiveData<Throwable> = MutableLiveData()


    fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {

        viewModelScope.launch { block() }

    }

    suspend fun <T> launchOnIO(block: suspend CoroutineScope.() -> T) {
        withContext(Dispatchers.IO) {
            block
        }
    }
}