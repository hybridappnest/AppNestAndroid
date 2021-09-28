package com.ymy.core.upload

import com.alibaba.sdk.android.oss.*
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
import com.alibaba.sdk.android.oss.model.ListObjectsRequest
import com.alibaba.sdk.android.oss.model.ListObjectsResult
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.orhanobut.logger.Logger
import com.ymy.core.BuildConfig
import com.ymy.core.Ktx
import com.ymy.core.utils.FileUtils
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created on 2020/8/4 12:32.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class OSSManager {
    companion object {

        private val oss: OSS

        //STS 鉴权服务器地址。
        private const val stsServer = BuildConfig.OSS_stsServer

        //节点地址
        private const val endpoint = BuildConfig.OSS_endpoint
        private const val bucketName = BuildConfig.OSS_bucketName

        const val webAssestsFolder = "webAssests/"
        const val imFolder = "im/"
        const val headerFolder = "userheader/"

        init {
            val credentialProvider: OSSCredentialProvider = OSSAuthCredentialsProvider(stsServer)
            //该配置类如果不设置，会有默认配置，具体可看该类
            val conf = ClientConfiguration()
            OSSLog.enableLog() //这个开启会支持写入手机sd卡中的一份日志文件位置在SDCard_path\OSSLog\logs.csv
            oss = OSSClient(Ktx.app, endpoint, credentialProvider, conf)
        }
    }

    private val mainScope = MainScope()

    private val uploadFileList: ArrayList<String> = ArrayList()
    private val jobs: ArrayList<Job> = ArrayList()
    private val jobsUploadSuccessFilePath: ArrayList<String> = ArrayList()
    private val jobsUploadFailFilePath: ArrayList<String> = ArrayList()
    private val jobsResult: ArrayList<String> = ArrayList()
    private var reTryCount = 0

    fun startUploadFileList(
        fileList: ArrayList<String>,
        path: String,
        uploadData: UploadViewModel.CallBack,
    ) {
        if (stsServer.isEmpty() || endpoint.isEmpty()) {
            uploadData.onError("oss鉴权、节点地址未配置")
            return
        }
        if (!checkAllPathFileExists(fileList)) {
            uploadData.onError("上传失败")
            return
        }
        Logger.d(fileList)
        Logger.d(path)
        Logger.d(uploadData)

        uploadFileList.clear()
        uploadFileList.addAll(fileList)

        jobsUploadFailFilePath.clear()
        jobsUploadSuccessFilePath.clear()

        jobsResult.clear()
        jobs.clear()

        uploadFileList(uploadFileList, path, uploadData)
    }

    private fun checkAllPathFileExists(fileList: ArrayList<String>): Boolean {
        fileList.forEach {
            val f = File(it)
            if (!f.exists()) {
                return false
            }
        }
        return true
    }

    private fun uploadFileList(
        fileList: ArrayList<String>,
        path: String,
        uploadData: UploadViewModel.CallBack,
    ) {
        fileList.forEach { filePath ->
            mainScope.launch(Dispatchers.Main) {
                val uploadJob = async(Dispatchers.IO) {
                    val fileSuffix = FileUtils.getFileSuffix(filePath)
                    val randomUUID: UUID = UUID.randomUUID()
                    upload("$path/${randomUUID}$fileSuffix", filePath)
                }
                jobs.add(uploadJob)
                val imageUrl = uploadJob.await()
                jobs.remove(uploadJob)
                jobsResult.add(imageUrl)
                if (jobs.isEmpty()) {
                    jobsUploadSuccessFilePath.forEach {
                        fileList.remove(it)
                    }
                    if (fileList.isEmpty()) {
                        uploadData.onSuccess(path, jobsResult)
                    } else {
                        if (reTryCount > 1) {
                            Logger.e("出现异常，上传失败")
                            uploadData.onError("上传失败")
                            return@launch
                        }
                        reTryCount++
                        Logger.e("出现异常，重试上传 reTryCount：$reTryCount")
                        uploadFileList(
                            fileList,
                            path,
                            uploadData
                        )
                    }
                }
            }
        }
    }

    private fun upload(objectName: String, filePath: String): String {
        Logger.d("$objectName   $filePath")
        // 构造上传请求。
        val put = PutObjectRequest(bucketName, objectName, filePath)
        try {
            val putResult = oss.putObject(put)
            Logger.d("PutObject", "UploadSuccess")
            Logger.d("ETag", putResult.eTag)
            Logger.d("RequestId", putResult.requestId)
            jobsUploadSuccessFilePath.add(filePath)
            return getPublicUrl(objectName)
        } catch (e: ClientException) {
            // 本地异常，如网络异常等。
            e.printStackTrace()
            Logger.e(e, "oss ClientException upload")
        } catch (e: ServiceException) {
            Logger.e(e, "oss ServiceException upload")
            // 服务异常。
            Logger.e("RequestId", e.requestId)
            Logger.e("ErrorCode", e.errorCode)
            Logger.e("HostId", e.hostId)
            Logger.e("RawMessage", e.rawMessage)
        }
        jobsUploadFailFilePath.add(filePath)
        return ""
    }

    private fun getPublicUrl(objectName: String): String {
        return oss.presignPublicObjectURL(bucketName, objectName)
    }

    suspend fun getFilesUrlByAttachments(attachments: String): ArrayList<String> {
        return withContext(Dispatchers.IO) {
            getFilesUrlByAttachments(bucketName, attachments)
        }
    }

    private fun getFilesUrlByAttachments(
        bucketName: String,
        attachments: String,
    ): ArrayList<String> {
        val urlList = arrayListOf<String>()
        val listObjects = ListObjectsRequest(bucketName)
        // 设定marker
        listObjects.marker = attachments
        // 设置成功、失败回调，发送异步列举请求
        try {
            val result: ListObjectsResult = oss.listObjects(listObjects)
            val objectSummaries = result.objectSummaries
            for (i in 0 until objectSummaries.size) {
                Logger.d(
                    "AyncListObjects",
                    "object: " + result.objectSummaries[i].key + " "
                            + result.objectSummaries[i].eTag.toString() + " "
                            + result.objectSummaries[i].lastModified
                )
            }
            val resultList = result.objectSummaries
            resultList.forEach {
                urlList.add(oss.presignPublicObjectURL(bucketName, it.key))
            }
        } catch (e: ClientException) {
            // 本地异常，如网络异常等。
            e.printStackTrace()
        } catch (e: ServiceException) {
            // 服务异常。
            Logger.e("RequestId", e.requestId)
            Logger.e("ErrorCode", e.errorCode)
            Logger.e("HostId", e.hostId)
            Logger.e("RawMessage", e.rawMessage)
        }
        return urlList
    }
}