package com.ymy.core.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import com.watermark.androidwm_light.WatermarkBuilder
import com.watermark.androidwm_light.bean.WatermarkText
import com.ymy.core.Ktx
import com.ymy.core.user.YmyUserManager
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


/**
 * Created on 12/10/20 15:09.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object WatermarkSettings {

    interface CallBack {
        fun onSuccess(resultMap: Map<String, String>)
        fun showLoading(show: Boolean)
        fun onError(errorMsg: String)
    }


    fun createWatermark(context: Context, path: String) {
        addWatermark(context, path, path)
    }

    private val mainScope = MainScope()

    private val originalFileList: ArrayList<String> = ArrayList()
    private val jobs: ArrayList<Job> = ArrayList()
    private val jobsAddFailFilePath: ArrayList<String> = ArrayList()
    private val jobsAddSuccessFilePath: ArrayList<String> = ArrayList()
    private val jobsResult: HashMap<String, String> = hashMapOf()
    private var reTryCount = 0

    /**
     * 删除水印缓存
     */
    fun cleanWaterMarkCacheImages() {
        File(getWaterMarkCacheDir()).let {
            if (it.exists()) {
                it.delete()
            }
        }
    }

    /**
     * 水印图片缓存目录
     * @return String
     */
    private fun getWaterMarkCacheDir(): String {
        var headerPath = Ktx.app.externalCacheDir?.absolutePath ?: ""
        if (headerPath.isEmpty()) {
            headerPath = Ktx.app.cacheDir.absolutePath
        }
        return headerPath + File.separator + "watermarkcache"
    }


    /**
     * 多线程异步执行多个图片文件添加水印
     * @param context Context
     * @param fileList List<String>
     * @param markSuffix String
     * @param callback CallBack
     */
    fun startAddWaterMarkForList(
        context: Context,
        fileList: List<String>,
        markSuffix: String,
        callback: WatermarkSettings.CallBack,
    ) {
        val dir = File(getWaterMarkCacheDir())
        if (!dir.exists()) {
            dir.mkdir()
        }
        clearAllData()
        originalFileList.addAll(fileList)

        createWatermarkForList(context, originalFileList, markSuffix, callback)
    }

    private fun clearAllData() {
        originalFileList.clear()

        jobsAddFailFilePath.clear()
        jobsAddSuccessFilePath.clear()

        jobsResult.clear()
        jobs.clear()
    }

    private fun createWatermarkForList(
        context: Context, addFileList: List<String>,
        markSuffix: String, callback: CallBack
    ) {

        addFileList.forEach { picturePath ->
            mainScope.launch(Dispatchers.Main) {
                val addJob = async(Dispatchers.IO) {
                    createWatermarkToCacheFile(context, picturePath, markSuffix)
                }
                jobs.add(addJob)
                val addImagePath = addJob.await()
                jobs.remove(addJob)
                jobsResult[picturePath] = addImagePath
                if (jobs.isEmpty()) {
                    jobsResult.forEach {
                        originalFileList.remove(it.key)
                    }
                    if (originalFileList.isEmpty()) {
                        callback.onSuccess(jobsResult)
                        clearAllData()
                    } else {
                        if (reTryCount > 1) {
                            clearAllData()
                            callback.onError("添加水印失败")
                            return@launch
                        }
                        reTryCount++
                        createWatermarkForList(
                            context,
                            addFileList,
                            markSuffix,
                            callback
                        )
                    }
                }
            }
        }
    }

    private suspend fun createWatermarkToCacheFile(
        context: Context,
        originalPath: String,
        markSuffix: String
    ): String {
        return addWatermark(context, originalPath, getWaterMarkCacheFile(originalPath), markSuffix)
    }

    /**
     * 获取图片的缓存图片路径
     * @param originalPath String
     * @return String
     */
    private fun getWaterMarkCacheFile(originalPath: String): String {
        return "${getWaterMarkCacheDir()}${File.separator}picture_${
            MD5Utils.md5Encrypt32Lower(
                originalPath
            )
        }${
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(
                Date()
            )
        }.jpg"
    }

    /**
     * 添加水印方法
     * @param context Context
     * @param path String 原图片路径
     * @param outPath String 添加水印后的文件路径
     * @return String 添加水印后的文件路径，或""，为空时则出现了异常
     */
    private fun addWatermark(
        context: Context,
        path: String,
        outPath: String,
        markSuffix: String = "现场拍照"
    ): String {
        var resultPath = ""
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
            // 压缩完后便可以将inJustDecodeBounds设置为false
            options.inJustDecodeBounds = false
            // 把流转化为Bitmap图片
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri),
                null,
                options
            )?.run {
                //获取元数据时间
                val t = if (this.width > this.height) {
                    //横向
                    this.height
                } else {
                    //纵向
                    this.width
                }
                val dateTime = getImageExifDateTime(path)
                val size = (10.0 * t / 720).roundToInt()
//                Logger.e("water $size")

                val watermarkText = WatermarkText(
                    "$dateTime ${YmyUserManager.user.realName} $markSuffix".trimIndent()
                ).setPositionX(0.04)
                    .setPositionY(0.92)
                    .setTextShadow(0.1f, 5.0F, 5.0F, Color.BLACK)
                    .setTextColor(Color.WHITE)
                    .setTextAlpha(255)
                    .setTextSize(size.toDouble())
                val createWatermark = WatermarkBuilder
                    .create(Ktx.app, this)
                    .loadWatermarkTexts(mutableListOf(watermarkText))
                    .setTileMode(false)
                    .watermark
                    .outputImage

                BitmapOrientationUtils.saveBitmapToFile(
                    outPath, createWatermark
                )

                this.recycle()
                createWatermark.recycle()

                resultPath = outPath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultPath
    }

    private fun getImageExifDateTime(path: String): String {
        var dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
//        val exif: ExifInterface
//        try {
//            exif = ExifInterface(path)
//            val temp = exif.getAttribute(ExifInterface.TAG_DATETIME)
//            temp?.run {
//                dateTime = this
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
        return dateTime
    }
}
