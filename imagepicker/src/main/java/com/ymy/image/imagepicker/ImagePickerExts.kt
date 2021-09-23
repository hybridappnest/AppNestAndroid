package com.ymy.image.imagepicker

import android.app.Activity
import androidx.fragment.app.Fragment
import com.lcw.library.imagepicker.ImagePicker
import com.ymy.core.permission.DBXPermissionUtils
import com.ymy.core.permission.requestPermission
import com.ymy.core.utils.ToastUtils
import com.ymy.image.imagepicker.loader.GlideLoader

/**
 * Created on 2021/5/26 10:06.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
/**
 * Activity调起选择图片
 * @param act Activity
 * @param maxCount Int
 * @param requestCode Int
 * @param mPhotoVideoPathList ArrayList<String>
 * @param actionGrantedAfter Function0<Unit>?
 * @param showCamera Boolean
 * @param showImage Boolean
 * @param showVideo Boolean
 * @param filterGif Boolean
 * @param setSingleType Boolean
 * @param needWatermark Boolean
 * @param actionDeniedAfter Function0<Unit>?
 */
fun startPickerImage(
    act: Activity,
    maxCount: Int,
    requestCode: Int,
    mPhotoVideoPathList: ArrayList<String> = arrayListOf(),
    actionGrantedAfter: (() -> Unit)? = null,
    showCamera: Boolean = true,
    showImage: Boolean = true,
    showVideo: Boolean = true,
    filterGif: Boolean = false,
    setSingleType: Boolean = false,
    needWatermark: Boolean = true,
    actionDeniedAfter: (() -> Unit)? = null,
) {
    requestPermission(act,
        "你好:\n" +
                "     该功能需要访问您的相册及拍照、录制视频，需要您的授权:\n" +
                "1、【储存】\n" +
                "2、【相机】\n" +
                "3、【麦克风】\n",
        arrayOf(
            DBXPermissionUtils.WRITE_EXTERNAL_STORAGE,
            DBXPermissionUtils.CAMERA,
            DBXPermissionUtils.RECORD_AUDIO,
        ),
        actionGranted = {
            doStartPickerImage(
                act,
                maxCount,
                requestCode,
                mPhotoVideoPathList,
                showCamera,
                showImage,
                showVideo,
                filterGif,
                setSingleType,
                needWatermark
            )
            actionGrantedAfter?.invoke()
        },
        actionDenied = {
            ToastUtils.showImageToast(act, "无权限无法使用该功能", false)
            actionDeniedAfter?.invoke()
        })
}

fun doStartPickerImage(
    act: Activity,
    maxCount: Int,
    requestCode: Int,
    mPhotoVideoPathList: ArrayList<String>,
    showCamera: Boolean,
    showImage: Boolean,
    showVideo: Boolean,
    filterGif: Boolean,
    setSingleType: Boolean,
    needWatermark: Boolean,
) {
    ImagePicker.getInstance()
        .showImage(showImage) //设置是否展示图片
        .showVideo(showVideo) //设置是否展示视频
        .showCamera(showCamera)
        .needWatermark(needWatermark)
        .filterGif(filterGif) //设置是否过滤gif图片
        .setMaxCount(maxCount) //设置最大选择图片数目(默认为1，单选)
        .setSingleType(setSingleType) //设置图片视频不能同时选择
        .setImagePaths(mPhotoVideoPathList) //设置历史选择记录
        .setImageLoader(GlideLoader()) //设置自定义图片加载器
        .start(
            act, requestCode
        )
}

/**
 * fragment调起选择图片
 * @param fragment Fragment
 * @param maxCount Int
 * @param requestCode Int
 * @param mPhotoVideoPathList ArrayList<String>
 * @param actionGrantedAfter Function0<Unit>?
 * @param showCamera Boolean
 * @param showImage Boolean
 * @param showVideo Boolean
 * @param filterGif Boolean
 * @param setSingleType Boolean
 * @param needWatermark Boolean
 * @param actionDeniedAfter Function0<Unit>?
 */
fun startPickerImage(
    fragment: Fragment,
    maxCount: Int,
    requestCode: Int,
    mPhotoVideoPathList: ArrayList<String> = arrayListOf(),
    actionGrantedAfter: (() -> Unit)? = null,
    showCamera: Boolean = true,
    showImage: Boolean = true,
    showVideo: Boolean = true,
    filterGif: Boolean = false,
    setSingleType: Boolean = false,
    needWatermark: Boolean = true,
    actionDeniedAfter: (() -> Unit)? = null,
) {
    val context = fragment.context
    if (context != null) {
        requestPermission(context,
            "你好:\n" +
                    "     该功能需要访问您的相册及拍照、录制视频，需要您的授权:\n" +
                    "1、【储存】\n" +
                    "2、【相机】\n" +
                    "3、【麦克风】\n",
            arrayOf(
                DBXPermissionUtils.WRITE_EXTERNAL_STORAGE,
                DBXPermissionUtils.CAMERA,
                DBXPermissionUtils.RECORD_AUDIO,
            ),
            actionGranted = {
                doStartPickerImage(
                    fragment,
                    maxCount,
                    requestCode,
                    mPhotoVideoPathList,
                    showCamera,
                    showImage,
                    showVideo,
                    filterGif,
                    setSingleType,
                    needWatermark
                )
                actionGrantedAfter?.invoke()
            },
            actionDenied = {
                ToastUtils.showImageToast(context, "无权限无法使用该功能", false)
                actionDeniedAfter?.invoke()
            })
    }
}

fun doStartPickerImage(
    fragment: Fragment,
    maxCount: Int,
    requestCode: Int,
    mPhotoVideoPathList: ArrayList<String>,
    showCamera: Boolean,
    showImage: Boolean,
    showVideo: Boolean,
    filterGif: Boolean,
    setSingleType: Boolean,
    needWatermark: Boolean,
) {
    ImagePicker.getInstance()
        .showImage(showImage) //设置是否展示图片
        .needWatermark(needWatermark)
        .showCamera(showCamera)
        .showVideo(showVideo) //设置是否展示视频
        .filterGif(filterGif) //设置是否过滤gif图片
        .setMaxCount(maxCount) //设置最大选择图片数目(默认为1，单选)
        .setSingleType(setSingleType) //设置图片视频不能同时选择
        .setImagePaths(mPhotoVideoPathList) //设置历史选择记录
        .setImageLoader(GlideLoader()) //设置自定义图片加载器
        .start(
            fragment, requestCode
        )
}