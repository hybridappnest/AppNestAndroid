package com.ymy.core.manager

import com.ymy.core.Ktx
import com.ymy.core.upload.OSSManager
import com.ymy.core.upload.UploadUtils
import com.ymy.core.upload.UploadViewModel
import com.ymy.core.utils.MediaFileUtil
import com.ymy.core.utils.MediaUtils
import com.ymy.core.utils.ToastUtils
import java.io.File
import java.io.Serializable

/**
 * Created on 3/4/21 09:03.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

const val MEDIA_UPLOAD_TYPE_IMAGE = "2"
const val MEDIA_UPLOAD_TYPE_VIDEO = "1"

class WebUploadMediaHelper {
    companion object {
        var mVideoUploadCallback: VideoUploadCallBack? = null
    }

    private val uploadViewModel: UploadViewModel by lazy {
        UploadViewModel()
    }
    var callBack: WebUploadMediaCallBack? = null

    interface WebUploadMediaCallBack {
        fun uploadResult(result: Boolean, resultData: MutableList<MediaObjectForWeb>)
    }

    interface VideoUploadCallBack {
        fun addWaterMarkToVideoInOss(videoUrlList: MutableList<String>)
    }

    val uploadMediaFileObject = mutableListOf<MediaObject>()

    fun uploadMediaFile(selectedPhotoPath: MutableList<String>, callBack: WebUploadMediaCallBack) {
        this.callBack = callBack
        var videoCount = 0
        var videoGetCoverCount = 0
        val uploadMediaVideoList = mutableListOf<MediaObject>()
        selectedPhotoPath.forEach {
            if (MediaFileUtil.isVideoFileType(it)) {
                val element = MediaObject(MEDIA_UPLOAD_TYPE_VIDEO, path = it)
                uploadMediaFileObject.add(element)
                uploadMediaVideoList.add(element)
                videoCount++
            } else {
                uploadMediaFileObject.add(
                    MediaObject(
                        MEDIA_UPLOAD_TYPE_IMAGE,
                        path = it
                    )
                )
            }
        }
        if (uploadMediaVideoList.isNotEmpty()) {
            uploadMediaVideoList.forEach {
                MediaUtils.getImageForVideo(it.path) { coverFile: File, _: String, _: String, _: String ->
                    if (coverFile.exists()) {
                        it.coverPath = coverFile.absolutePath
                        videoGetCoverCount++
                        if (videoCount == videoGetCoverCount) {
                            upLoadMediaObject()
                        }
                    } else {
                        callBack.uploadResult(false, mutableListOf())
                    }
                }
            }
        } else {
            upLoadMediaObject()
        }
    }

    val upLoadMediaTempList = mutableListOf<String>()

    private fun upLoadMediaObject() {
        uploadMediaFileObject.forEach {
            upLoadMediaTempList.add(it.path)
            it.urlIndex = upLoadMediaTempList.size - 1
            if (it.coverPath.isNotEmpty()) {
                upLoadMediaTempList.add(it.coverPath)
                it.coverUrlIndex = upLoadMediaTempList.size - 1
            }
        }
        loopUploadMediaTempList()
    }

    var uploadIndex = 0
    private fun loopUploadMediaTempList() {
        if (uploadIndex < upLoadMediaTempList.size) {
            val path = upLoadMediaTempList[uploadIndex]
            uploadViewModel.uploadFile(
                arrayListOf(path),
                UploadUtils.getUploadFilePath(OSSManager.webAssestsFolder, "webAssests"),
                object : UploadViewModel.CallBack {
                    override fun onSuccess(ossTag: String, resultList: ArrayList<String>) {
                        upLoadMediaTempList[uploadIndex] = resultList[0]
                        uploadIndex++
                        loopUploadMediaTempList()
                    }

                    override fun showLoading(show: Boolean) {
                    }

                    override fun onError(errorMsg: String) {
                        callBack?.uploadResult(false, mutableListOf())
                        ToastUtils.showImageToast(Ktx.app, errorMsg, false)
                    }
                })
        } else {
            changeUploadMediaVideoList()
        }
    }

    private fun changeUploadMediaVideoList() {
        val forWeb = mutableListOf<MediaObjectForWeb>()
        val videoUrlList = mutableListOf<String>()
        uploadMediaFileObject.forEach {
            if (it.urlIndex != -1) {
                it.url = upLoadMediaTempList[it.urlIndex]
            }
            if (it.coverUrlIndex != -1) {
                it.coverUrl = upLoadMediaTempList[it.coverUrlIndex]
            }
            if(it.type == MEDIA_UPLOAD_TYPE_VIDEO){
                videoUrlList.add(it.url)
            }
            forWeb.add(MediaObjectForWeb(it.type, it.url, it.coverUrl))
        }
        callBack?.uploadResult(true, forWeb)
        mVideoUploadCallback?.addWaterMarkToVideoInOss(videoUrlList)
    }

}

data class MediaObjectForWeb(
    val type: String = "",
    var url: String = "",
    var coverUrl: String = "",
) : Serializable


data class MediaObject(
    val type: String = "",
    var path: String = "",
    var urlIndex: Int = -1,
    var url: String = "",
    var coverPath: String = "",
    var coverUrlIndex: Int = -1,
    var coverUrl: String = "",
) : Serializable
