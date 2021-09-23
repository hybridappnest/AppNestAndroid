package com.ymy.core.upload

/**
 * Created on 2020/8/6 08:40.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
data class UploadModel(
    val error: String? = null,
    val successPath: String? = null,
    val successImageUrls: ArrayList<String>? = null
)