package com.ymy.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.*

/**
 * Created on 2020/11/13 10:58.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object BitmapOrientationUtils {
    /**
     * 修正图片方向，用于调用系统相机拍照时，修改部分系统拍照方向异常的情况
     * @param context Context?
     * @param mFilePath String?
     */
    fun revisePhotoOrientation(context: Context?, mFilePath: String?) {
        if (context == null || mFilePath == null) {
            return
        }
        val bitmapDegree = getBitmapDegree(mFilePath)
        if (bitmapDegree != 0) {
            createNewPhoto(context, mFilePath, bitmapDegree)
        }
    }

    private fun createNewPhoto(context: Context, path: String, bitmapDegree: Int) {
        try {
            val file = File(path)
            val uri = Uri.fromFile(file)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri), null,
                options
            )
            options.inSampleSize = 4
            // 压缩完后便可以将inJustDecodeBounds设置为false
            options.inJustDecodeBounds = false
            // 把流转化为Bitmap图片
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri),
                null,
                options
            )?.apply {
                val returnBm =
                    Bitmap.createBitmap(this, 0, 0, this.width, this.height, Matrix().apply {
                        postRotate(bitmapDegree.toFloat())
                    }, true)
                saveBitmapToFile(
                    path, returnBm
                )
                this.recycle()
                returnBm.recycle()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    /**
     * bitmap到本地
     */
    fun saveBitmapToFile(fileNameWithPath: String, bitmap: Bitmap): String {
        var result = ""
        var out: OutputStream? = null
        try {
            val filePic = File(fileNameWithPath)
            if (!filePic.exists()) {
                filePic.createNewFile()
            }
            out = FileOutputStream(filePic)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            result = filePic.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private fun getBitmapDegree(path: String): Int {
        var degree = 0
        try {
            val orientation = ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            degree = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }
}