package com.ymy.image.imagepicker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Mode
import com.ymy.core.Ktx
import com.ymy.core.base.RootFragment
import com.ymy.core.utils.GalleryFileSaver
import com.ymy.core.utils.mVideoDir
import com.ymy.image.R
import com.ymy.image.databinding.FragmentImagevideoBinding
import com.ymy.image.imagepicker.activity.ImagePickerScopeId
import com.ymy.image.imagepicker.viewmodel.PickerViewModel
import org.koin.android.ext.android.getKoin
import org.koin.core.scope.Scope
import java.io.File

/**
 * Created on 2020/7/29 09:10.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:视频拍摄
 */
class ImageVideoFragment : RootFragment() {

    private lateinit var mImagePickerScope: Scope
    private lateinit var mPickerViewModel: PickerViewModel

    private fun initVM(): PickerViewModel {
        mImagePickerScope =
            Ktx.app.getKoin().getScope(ImagePickerScopeId)
        return mImagePickerScope.get()
    }

    companion object {
        fun newInstance(): ImageVideoFragment {
            return ImageVideoFragment()
        }
    }

    val mBinding: FragmentImagevideoBinding by lazy {
        FragmentImagevideoBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return mBinding.root
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
        mBinding.camera.run {
            setLifecycleOwner(this@ImageVideoFragment)
            addCameraListener(mCameraListener)
            mode = Mode.VIDEO
        }
        mBinding.takeVideo.setOnClickListener {
            captureVideo()
        }
        mBinding.toggleCamera.setOnClickListener {
            toggleCamera()
        }
    }

    private fun captureVideo() {
        if (!mVideoDir.exists()) {
            mVideoDir.mkdir()
        }
        mBinding.camera.takeVideoSnapshot(
            File(
                mVideoDir,
                "image_${System.currentTimeMillis()}_video.mp4"
            ), 5000
        )
    }

    private fun toggleCamera() {
        if (mBinding.camera.isTakingVideo) return
        mBinding.camera.toggleFacing()
    }

    private val mCameraListener = object : CameraListener() {
        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            mBinding.camera.close()
            mBinding.flPreVideo.visibility = View.VISIBLE
            showPreVideo(result)
        }
    }


    private fun showPreVideo(videoResult: VideoResult) {
        requireContext().let {
            videoResult.run {
                GalleryFileSaver.insertVideoToMediaStore(
                    it,
                    file.absolutePath,
                    System.currentTimeMillis(),
                    size.width,
                    size.height,
                    5000
                )
            }
        }
        val transaction = childFragmentManager.beginTransaction()
        val videoPreFragment =
            VideoPreFragment(mPickerViewModel = mPickerViewModel, file = videoResult.file)
        transaction.replace(R.id.fl_pre_video, videoPreFragment)
        transaction.commit()
    }

    fun initData() {
    }
}