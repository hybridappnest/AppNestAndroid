package com.ymy.appnest.qrcode

import com.google.gson.JsonObject

/**
 * Created on 1/19/21 08:48.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
const val EVENT_TYPE_TRAIN = "train"
data class ScannerData(
    val eventType:String,
    val data: JsonObject,
)

data class TrainData(
    val id:Int,
    val show_type:String,
)
