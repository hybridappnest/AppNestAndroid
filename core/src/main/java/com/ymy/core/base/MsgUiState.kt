package com.ymy.core.base

/**
 * Created by hanxueqiang
 * on 2020/3/30 15:34
 */
data class MsgUiState(
    var isToastMsg: String? = null,
    var isSuccess: Boolean = true
) : BaseViewModel()