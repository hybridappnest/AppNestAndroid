package com.ymy.image.imagepicker.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Preview
import com.ymy.core.Ktx
import com.ymy.core.base.RootFragment
import com.ymy.core.utils.GalleryFileSaver
import com.ymy.image.imagepicker.activity.ImagePickerScopeId
import com.ymy.image.imagepicker.viewmodel.PickerViewModel
import com.ymy.image.databinding.FragmentImagephotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.core.scope.Scope


/**
 * Created on 2020/7/29 09:10.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:照片拍摄
 */
class ImagePhotoFragment : RootFragment() {

    private lateinit var mImagePickerScope: Scope
    private lateinit var mPickerViewModel: PickerViewModel
    private var mCaptureTime: Long = 0

    val mBinding: FragmentImagephotoBinding by lazy {
        FragmentImagephotoBinding.inflate(layoutInflater)
    }

    private fun initVM(): PickerViewModel {
        mImagePickerScope =
            Ktx.app.getKoin().getScope(ImagePickerScopeId)
        return mImagePickerScope.get()
    }

    companion object {
        fun newInstance(): ImagePhotoFragment {
            return ImagePhotoFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPickerViewModel = initVM()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    fun initView() {
        mBinding.camera.setLifecycleOwner(this)
        mBinding.camera.addCameraListener(mCameraListener)
        mBinding.takePicture.setOnClickListener {
            capturePictureSnapshot()
        }
        mBinding.toggleCamera.setOnClickListener {
            toggleCamera()
        }
    }

    private fun capturePictureSnapshot() {
        if (mBinding.camera.isTakingPicture) return
        if (mBinding.camera.preview != Preview.GL_SURFACE) {
            return
        }
        mCaptureTime = System.currentTimeMillis()
        mBinding.camera.takePicture()
    }

    private fun toggleCamera() {
        if (mBinding.camera.isTakingPicture || mBinding.camera.isTakingVideo) return
        mBinding.camera.toggleFacing()
    }

    private val mCameraListener = object : CameraListener() {

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            if (mBinding.camera.isTakingVideo) {
                return
            }
            showPreImage(result)
        }
    }
    private var tempBitmap: Bitmap? = null

    private fun showPreImage(result: PictureResult) {
        mBinding.camera.close()
        mBinding.flPreImage.visibility = View.VISIBLE
        mBinding.ibCommit.setOnClickListener {
            onCommitClick()
        }
        try {
            result.toBitmap(
                2000, 3000
            ) { bitmap ->
                mBinding.tvPreImage.setImageBitmap(bitmap)
                tempBitmap = bitmap
            }
        } catch (e: UnsupportedOperationException) {
            mBinding.tvPreImage.setImageDrawable(ColorDrawable(Color.GREEN))
        }
    }

    private fun onCommitClick() {
        lifecycleScope.launch(Dispatchers.Main) {
            val result = async(Dispatchers.IO) {
                saveImage(tempBitmap)
            }
            val saveImageFilePath = result.await()
            if (saveImageFilePath.isEmpty()) {
                toast("保存失败")
            } else {
                mPickerViewModel.addImageToSelectList(saveImageFilePath)
                mPickerViewModel.commitSelection(cancel = false)
            }
        }
    }

    private fun saveImage(bmp: Bitmap?): String {
        var filePath = ""
        if (bmp == null) return filePath
        filePath = GalleryFileSaver().saveBitmapToGallery(
            "image_${System.currentTimeMillis()}.jpeg",
            bmp
        )
        context?.let {
            GalleryFileSaver.insertImageToMediaStore(
                it,
                filePath,
                System.currentTimeMillis(),
                bmp.width,
                bmp.height
            )
        }
        return filePath
    }


    fun initData() {
    }

}