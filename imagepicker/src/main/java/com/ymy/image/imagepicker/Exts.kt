package com.ymy.image.imagepicker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

/**
 * Created on 2020/7/31 08:21.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

/**
 * Uri获取文件本地路径
 */
fun Uri.getFileLocalPath(context: Context): String {
    val scheme = scheme
    var result = ""

    if (scheme == null)
        result = path.toString()
    else if (ContentResolver.SCHEME_FILE == scheme) {
        result = path.toString()
    } else if (ContentResolver.SCHEME_CONTENT == scheme) {
        val cursor = context.contentResolver.query(
            this,
            arrayOf(MediaStore.Images.ImageColumns.DATA),
            null,
            null,
            null
        )
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                if (index > -1) {
                    result = cursor.getString(index);
                }
            }
            cursor.close();
        }
    }
    return result
}
