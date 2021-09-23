package com.ymy.core.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.orhanobut.logger.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


/**
 * Created on 2020/7/30 18:29.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

//在系统的图片文件夹下创建了一个相册文件夹，名为“myPhotos"，所有的图片都保存在该文件夹下。
const val DIR_NAME = "dingbaoxin"

//图片统一保存在系统的图片文件夹中
val mPicDir: File = File(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
    DIR_NAME
)

//视频统一保存在系统的DCIM文件夹下
val mVideoDir: File = File(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
    DIR_NAME
)


class GalleryFileSaver {

    /**
     * bitmap到本地
     */
    fun saveBitmapToGallery(fileName: String, bitmap: Bitmap):String{
        var result = ""
        var out: OutputStream? = null
        try {
            if (!mPicDir.exists()) {
                mPicDir.mkdirs()
            }
            val filePic = File(mPicDir,fileName)
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

    companion object {
        /**
         * 针对非系统文件夹下的文件,使用该方法
         * 插入时初始化公共字段
         *
         * @param filePath 文件
         * @param time     ms
         * @return ContentValues
         */
        private fun initCommonContentValues(
            filePath: String,
            time: Long
        ): ContentValues {
            val values = ContentValues()
            val saveFile = File(filePath)
            val timeMillis = getTimeWrap(time)
            values.put(MediaStore.MediaColumns.TITLE, saveFile.name)
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.name)
            values.put(MediaStore.MediaColumns.DATE_MODIFIED, timeMillis)
            values.put(MediaStore.MediaColumns.DATE_ADDED, timeMillis)
            values.put(MediaStore.MediaColumns.DATA, saveFile.absolutePath)
            values.put(MediaStore.MediaColumns.SIZE, saveFile.length())
            return values
        }

        /**
         * 保存到照片到本地，并插入MediaStore以保证相册可以查看到,这是更优化的方法，防止读取的照片获取不到宽高
         *
         * @param context    上下文
         * @param filePath   文件路径
         * @param createTime 创建时间 <=0时为当前时间 ms
         * @param width      宽度
         * @param height     高度
         */
        fun insertImageToMediaStore(
            context: Context,
            filePath: String?,
            createTime: Long,
            width: Int,
            height: Int
        ) {
            var createTime = createTime
            if (!checkFile(filePath!!)) return
            createTime = getTimeWrap(createTime)
            val values = initCommonContentValues(filePath, createTime)
            values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, createTime)
            values.put(MediaStore.Images.ImageColumns.DATE_ADDED, createTime)
            values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, createTime)
            if (width > 0) values.put(MediaStore.Images.ImageColumns.WIDTH, width)
            if (height > 0) values.put(MediaStore.Images.ImageColumns.HEIGHT, height)
            values.put(MediaStore.MediaColumns.MIME_TYPE, getPhotoMimeType(filePath))
            context.applicationContext.contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }


        /**
         * 保存到视频到本地，并插入MediaStore以保证相册可以查看到,这是更优化的方法，防止读取的视频获取不到宽高
         *
         * @param context    上下文
         * @param filePath   文件路径
         * @param createTime 创建时间 <=0时为当前时间 ms
         * @param duration   视频长度 ms
         * @param width      宽度
         * @param height     高度
         */
        fun insertVideoToMediaStore(
            context: Context,
            filePath: String,
            createTime: Long,
            width: Int,
            height: Int,
            duration: Long
        ) {
            var createTime = createTime
            if (!checkFile(filePath)) return
            createTime = getTimeWrap(createTime)
            val values: ContentValues = initCommonContentValues(filePath, createTime)
            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, createTime)
            if (duration > 0) values.put(MediaStore.Video.VideoColumns.DURATION, duration)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                if (width > 0) values.put(MediaStore.Video.VideoColumns.WIDTH, width)
                if (height > 0) values.put(MediaStore.Video.VideoColumns.HEIGHT, height)
            }
            values.put(MediaStore.MediaColumns.MIME_TYPE, getVideoMimeType(filePath))
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        }


        // 是不是系统相册
        private fun isSystemDcim(path: String): Boolean {
            return path.toLowerCase().contains("dcim") || path.toLowerCase().contains("camera")
        }

        // 获取照片的mine_type
        private fun getPhotoMimeType(path: String): String? {
            val lowerPath = path.toLowerCase()
            if (lowerPath.endsWith("jpg") || lowerPath.endsWith("jpeg")) {
                return "image/jpeg"
            } else if (lowerPath.endsWith("png")) {
                return "image/png"
            } else if (lowerPath.endsWith("gif")) {
                return "image/gif"
            }
            return "image/jpeg"
        }

        // 获取video的mine_type,暂时只支持mp4,3gp
        private fun getVideoMimeType(path: String): String? {
            val lowerPath = path.toLowerCase()
            if (lowerPath.endsWith("mp4") || lowerPath.endsWith("mpeg4")) {
                return "video/mp4"
            } else if (lowerPath.endsWith("3gp")) {
                return "video/3gp"
            }
            return "video/mp4"
        }

        // 获得转化后的时间
        private fun getTimeWrap(time: Long): Long {
            return if (time <= 0) {
                System.currentTimeMillis()
            } else time
        }

        // 检测文件存在
        private fun checkFile(filePath: String): Boolean {
            //boolean result = FileUtil.fileIsExist(filePath);
            var result = false
            val mFile = File(filePath)
            if (mFile.exists()) {
                result = true
            }
            Logger.e("文件不存在 path = $filePath")
            return result
        }
    }
}