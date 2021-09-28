package com.ymy.web.custom.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.alibaba.fastjson.JSONArray
import com.google.gson.Gson
import com.lcw.library.imagepicker.ImagePicker
import com.ymy.core.base.RootActivity
import com.ymy.core.exts.fromJson
import com.ymy.core.manager.MediaObjectForWeb
import com.ymy.core.manager.WebUploadMediaHelper
import com.ymy.core.ok3.GsonUtils
import com.ymy.core.upload.OSSManager
import com.ymy.core.upload.UploadUtils
import com.ymy.core.upload.UploadViewModel
import com.ymy.core.utils.FileUtil
import com.ymy.image.imagepicker.startPickerImage
import com.ymy.appnest.web.custom.JSCallBack
import com.ymy.appnest.web.custom.DEFAULT_JS_SUFFIX
import com.ymy.appnest.web.custom.JSNotificationAction
import org.koin.android.ext.android.inject
import java.io.*


/**
 * Created on 2020/7/11 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:透明背景
 */
class JSBridgeActivity : RootActivity() {
    private val uploadViewModel: UploadViewModel by inject()

    companion object {
        const val ACTION = "ACTION"
        const val ACTION_ANNOUNCE = "ANNOUNCE"
        const val PARAMS = "PARAMS"
        const val PARAMS_JSON_DATA = "PARAMS_JSON_DATA"

        /**
         * 上传文件
         */
        const val jsUpload = "upload"

        /**
         * 上传文件callback
         */
        const val jsCallBackUploadCallback = jsUpload + JSNotificationAction.CALLBACK_SUFFIX

        /**
         * 选择图片视频并上传
         */
        const val jschoseImgWithUpload = "choseImg"

        /**
         * 选择图片视频并上传callback
         */
        const val jsCallBackChoseImgWithUpload = jschoseImgWithUpload + JSNotificationAction.CALLBACK_SUFFIX

        /**
         * 选择文件
         */
        const val jsChoseFile = "choseFile"

        /**
         * 文件选择callback
         */
        const val jsCallBackChoseFileCallBack = jsChoseFile + JSNotificationAction.CALLBACK_SUFFIX


        var mJsCallBack: JSCallBack? = null

        fun invokeNew(
            context: Context,
            jsCallBack: JSCallBack,
            action: String,
            params: String = "",
            announce: Int = 0,
        ) {
            mJsCallBack = jsCallBack
            val bundle = Bundle()
            bundle.putString(ACTION, action)
            bundle.putString(PARAMS, params)
            bundle.putInt(ACTION_ANNOUNCE, announce)
            val intent = Intent(context, JSBridgeActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }


        private const val REQUEST_SELECT_IMAGES_CODE = 0x01
        private const val REQUEST_SELECT_FILE_CODE = 0x02

        private const val maxCountDefault = 9
        private var maxCount = maxCountDefault

        /**
         * 拍照相关
         */
        private var mFilePath: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentExtra()
    }

    private var mAction = ""
    private var mActionAnnounce = 0
    private var mParams = ""
    private var needvideo = true
    private fun getIntentExtra() {
        intent.extras?.run {
            mAction = getString(ACTION, "")
            mActionAnnounce = getInt(ACTION_ANNOUNCE, 0)
            mParams = getString(PARAMS, "")
            when (mAction) {
                jschoseImgWithUpload + DEFAULT_JS_SUFFIX -> {
                    val chosePictureParams: ChosePictureParams = GsonUtils.mGson.fromJson(mParams)
                    needvideo = chosePictureParams.needVideo == 1
                    maxCount = chosePictureParams.maxnum
                    chooseImg()
                }
                jsUpload + DEFAULT_JS_SUFFIX ->
                    upload(mParams)
                jsChoseFile + DEFAULT_JS_SUFFIX ->
                    choseFile()
            }
        }
        if (mAction.isEmpty()) {
            toast("传入参数异常")
            finish()
        }
    }

    private fun upload(param: String?) {
        param?.run {
            if (param.isEmpty()) {
                finish()
                return@run
            }
            try {
                val paths: ArrayList<String> = Gson().fromJson(param)
                if (paths.isNotEmpty()) {
                    uploadViewModel.uploadFile(paths,
                        UploadUtils.getUploadFilePath(OSSManager.webAssestsFolder, "webAssests"),
                        object : UploadViewModel.CallBack {
                            override fun onSuccess(ossTag: String, resultList: ArrayList<String>) {
                                callWebFunction(jsCallBackUploadCallback, resultList)
                            }

                            override fun showLoading(show: Boolean) {
                            }

                            override fun onError(errorMsg: String) {
                                callWebFunction(
                                    jsCallBackUploadCallback,
                                    JSONArray()
                                )
                                toast(errorMsg)
                            }
                        })
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }
    }

    private fun chooseImg() {
        startPickerImage(
            this,
            maxCount,
            REQUEST_SELECT_IMAGES_CODE,
            showVideo = needvideo,
            actionDeniedAfter = { finish() })
    }

    private fun callWebFunction(functionName: String, params: Any) {
        mJsCallBack?.run {
            sendResult(functionName, params, mActionAnnounce)
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SELECT_IMAGES_CODE) {
                val selectedPhotoPath =
                    data?.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES)
                if (selectedPhotoPath != null) {
                    if (selectedPhotoPath.isEmpty()) {
                        finish()
                    }
                    WebUploadMediaHelper().uploadMediaFile(selectedPhotoPath,
                        object : WebUploadMediaHelper.WebUploadMediaCallBack {
                            override fun uploadResult(
                                result: Boolean,
                                resultData: MutableList<MediaObjectForWeb>
                            ) {
                                callWebFunction(jsCallBackChoseImgWithUpload, resultData)
                            }
                        })
                }
            } else if (requestCode == REQUEST_SELECT_FILE_CODE) {
                val uri = data?.data //得到uri，后面就是将uri转化成file的过程。
                val filePath = FileUtil.getPathFromUri(uri)
                if (File(filePath).exists()) {
                    val fileName = filePath.split("/").last()
                    callWebFunction(
                        jsCallBackChoseFileCallBack,
                        arrayListOf(ChoseFileForWeb(filePath, fileName))
                    )
                }
            }
        }
        finish()
    }

    private fun choseFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, REQUEST_SELECT_FILE_CODE)
    }
}

data class ChosePictureParams(
    val needVideo: Int = 1,
    val maxnum: Int = 9,
) : Serializable

data class ChoseFileForWeb(
    val filePath: String = "",
    val fileName: String = "",
) : Serializable