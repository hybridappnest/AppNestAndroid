package com.ymy.image.imagepicker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ymy.core.base.BaseViewModel
import com.ymy.core.utils.MediaFileUtil

/**
 * Created on 2020/7/29 14:21.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class PickerViewModel : BaseViewModel() {
    //集合上限
    var mMaxCount = 1

    private val pickerMutableLiveData = MutableLiveData<PickerLiveData>()
    val pickerLiveData: LiveData<PickerLiveData> get() = pickerMutableLiveData

    var mSelectImagePaths: ArrayList<String> = mutableListOf<String>() as ArrayList<String>


    fun reSet() {
        mSelectImagePaths.clear()
        pickerMutableLiveData.value =
            PickerLiveData(commitSelection = false, cancel = false, resultList = mSelectImagePaths)
    }
    /**
     * 确认完成选择
     */
    fun commitSelection(cancel: Boolean) {
        pickerMutableLiveData.value =
            PickerLiveData(commitSelection = true, cancel = cancel, resultList = mSelectImagePaths)
    }


    /**
     * 添加/移除图片到选择集合
     *
     * @param imagePath
     * @return
     */
    fun addImageToSelectList(imagePath: String?): Boolean {
        return if (mSelectImagePaths.contains(imagePath)) {
            mSelectImagePaths.remove(imagePath)
        } else {
            if (mSelectImagePaths.size < mMaxCount) {
                mSelectImagePaths.add(imagePath!!)
            } else {
                false
            }
        }
    }

    /**
     * 添加图片到选择集合
     *
     * @param imagePaths
     */
    fun addImagePathsToSelectList(imagePaths: List<String?>?) {
        if (imagePaths != null) {
            for (i in imagePaths.indices) {
                val imagePath = imagePaths[i]
                if (!mSelectImagePaths.contains(imagePath) && mSelectImagePaths.size < mMaxCount) {
                    mSelectImagePaths.add(imagePath!!)
                }
            }
        }
    }


    /**
     * 判断当前图片是否被选择
     *
     * @param imagePath
     * @return
     */
    fun isImageSelect(imagePath: String?): Boolean {
        return mSelectImagePaths.contains(imagePath)
    }

    /**
     * 是否还可以继续选择图片
     *
     * @return
     */
    fun isCanChoose(): Boolean {
        return mSelectImagePaths.size < mMaxCount
    }

    /**
     * 是否可以添加到选择集合（在singleType模式下，图片视频不能一起选）
     *
     * @param currentPath
     * @param filePath
     * @return
     */
    fun isCanAddSelectionPaths(
        currentPath: String?,
        filePath: String?
    ): Boolean {
        return !(MediaFileUtil.isVideoFileType(currentPath) && !MediaFileUtil.isVideoFileType(
            filePath
        ) || !MediaFileUtil.isVideoFileType(
            currentPath
        ) && MediaFileUtil.isVideoFileType(filePath))
    }

    /**
     * 清除已选图片
     */
    fun removeAll() {
        mSelectImagePaths.clear()
    }
}

class PickerLiveData(
    //是否选择完毕
    var commitSelection: Boolean = false,
    //是否放弃选择
    var cancel: Boolean = false,
    //文件地址集合
    var resultList: ArrayList<String> = mutableListOf<String>() as ArrayList<String>
) : BaseViewModel()