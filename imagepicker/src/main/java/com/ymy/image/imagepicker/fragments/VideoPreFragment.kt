package com.ymy.image.imagepicker.fragments

import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.ymy.core.base.RootFragment
import com.ymy.image.databinding.FragmentPreVideoBinding
import com.ymy.image.imagepicker.viewmodel.PickerViewModel
import java.io.File

/**
 * Created on 2020/7/29 09:10.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:视频拍摄
 */
class VideoPreFragment(
    val mPickerViewModel: PickerViewModel,
    val file: File
) : RootFragment() {


    companion object {
        fun newInstance(mPickerViewModel: PickerViewModel, file: File): VideoPreFragment {
            return VideoPreFragment(mPickerViewModel, file)
        }
    }

    val mBinding: FragmentPreVideoBinding by lazy {
        FragmentPreVideoBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    fun initView() {
        val controller = MediaController(context)
        controller.setAnchorView(mBinding.videoView)
        controller.setMediaPlayer(mBinding.videoView)
        mBinding.videoView.setMediaController(controller)
        mBinding.videoView.setVideoURI(Uri.fromFile(file))
        mBinding.videoView.setOnPreparedListener(OnPreparedListener { mp ->
            val lp: ViewGroup.LayoutParams = mBinding.videoView.layoutParams
            val videoWidth = mp.videoWidth.toFloat()
            val videoHeight = mp.videoHeight.toFloat()
            val viewWidth: Float = mBinding.videoView.width.toFloat()
            lp.height = (viewWidth * (videoHeight / videoWidth)).toInt()
            mBinding.videoView.layoutParams = lp
            playVideo()
        })
        mBinding.ibCommit.setOnClickListener {
            onCommitClick()
        }
    }


    private fun onCommitClick() {
        mPickerViewModel.addImageToSelectList(file.absolutePath)
        mPickerViewModel.commitSelection(cancel = false)
    }

    private fun playVideo() {
        if (!mBinding.videoView.isPlaying) {
            mBinding.videoView.start()
        }
    }

    fun initData() {
    }
}