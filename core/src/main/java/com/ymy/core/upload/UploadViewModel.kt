package com.ymy.core.upload

import com.ymy.core.base.BaseViewModel

/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class UploadViewModel : BaseViewModel() {

    interface CallBack {
        fun onSuccess(ossTag: String, resultList: ArrayList<String>)
        fun showLoading(show:Boolean)
        fun onError(errorMsg: String)
    }

    fun uploadFile(fileList: ArrayList<String>, path: String,callBack:CallBack) {
        callBack.showLoading(true)
        OSSManager().startUploadFileList(fileList, path, callBack)
    }
}