package com.ymy.core.upload

import com.ymy.core.user.YmyUserManager
import com.ymy.core.utils.MD5Utils

/**
 * Created on 2020/8/6 10:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object UploadUtils {

    @JvmStatic
    fun getUploadFilePath(folder:String,tag:String):String{
        val folderName =
            MD5Utils.md5Encrypt32Lower("${YmyUserManager.user.userId}_${tag}_${System.currentTimeMillis()}")
        return "${folder}$folderName"
    }
}