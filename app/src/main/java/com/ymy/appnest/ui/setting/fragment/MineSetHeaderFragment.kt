package com.ymy.appnest.ui.setting.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Matrix
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.lcw.library.imagepicker.ImagePicker
import com.ymy.appnest.R
import com.ymy.appnest.databinding.FragmentSetHeaderBinding
import com.ymy.appnest.view.CustomDialog
import com.ymy.core.base.BaseFragment
import com.ymy.core.glide.ImageLoader
import com.ymy.core.user.YmyUserManager
import com.ymy.core.utils.GalleryFileSaver
import com.ymy.image.imagepicker.startPickerImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File


/**
 * Created on 2020/8/8 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class MineSetHeaderFragment : BaseFragment(true) {

    companion object {
        fun newInstance(): MineSetHeaderFragment {
            return MineSetHeaderFragment()
        }
    }

    val mBinding: FragmentSetHeaderBinding by lazy {
        FragmentSetHeaderBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View? = mBinding.root

    override fun initView() {
        initTitleBar()
        val matrix = Matrix()
        mBinding.photoView.setDisplayMatrix(matrix)
    }

    private fun initTitleBar() {
        mBinding.titleBar.btnBack.setOnClickListener { back() }
        mBinding.titleBar.tvTitlebarTitle.text = ""
        mBinding.titleBar.btnRightLayout.visibility = View.VISIBLE
        mBinding.titleBar.tvBtnRight.run {
            visibility = View.VISIBLE
            text = "选择"
            setOnClickListener {
                showSelectDialog()
            }
        }
    }

    private fun back() {
        view?.let { Navigation.findNavController(it).popBackStack() }
    }

    private fun showSelectDialog() {
        activity?.let {
            CustomDialog(
                it,
                R.layout.dialog_take_photo,
                scale = 1F,
                gravity = Gravity.BOTTOM,
                mIsDismissTouchOut = true
            ).apply {
                initView = {
                    val selectedByAlbum = findViewById<TextView>(R.id.tv_selected_by_album)
                    val cancel = findViewById<TextView>(R.id.tv_cancel)
                    selectedByAlbum?.setOnClickListener {
                        startPickerImage(
                            this@MineSetHeaderFragment,
                            1,
                            REQUEST_SELECT_IMAGES_CODE,
                            actionGrantedAfter = {
                                dismiss()
                            },
                            needWatermark = false,
                            showVideo = false,
                            filterGif = true,
                        )
                    }
                    cancel?.setOnClickListener {
                        dismiss()
                    }
                }
            }.show()
        }
    }

    private fun saveItToLocal() {
        context?.let {
            lifecycleScope.launch {
                val job = async(Dispatchers.IO) {
                    ImageLoader.getUrlBitmap(it, avatarUrl)
                }
                val result = job.await()
                if (result == null) {
                    toast("保存失败")
                } else {
                    GalleryFileSaver().saveBitmapToGallery(
                        "dingbaoxin_${System.currentTimeMillis()}.jpeg", result
                    )
                    toast("以为您保存到图片库")
                }
            }
        }
    }

    private val REQUEST_SELECT_IMAGES_CODE = 0x01

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGES_CODE && resultCode == Activity.RESULT_OK) {
            val selectedPhotoPath = data?.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES)
            selectedPhotoPath?.get(0)?.let { cutImage(it) }
        }
    }

    private fun getFileUri(filePath: String): Uri {
        return Uri.fromFile(File(filePath))
    }

    private fun cutImage(imagePath: String) {
        val inImageUri = getFileUri(imagePath)
        val args = MineHeaderCropperFragmentArgs
            .Builder()
            .apply {
                imageUri = inImageUri.toString()
            }
            .build()
            .toBundle()
        Navigation.findNavController(mBinding.photoView).navigate(
            R.id.action_mineSetHeaderFragment_to_mineHeaderCropperFragment,
            args
        )
    }

    override fun onResume() {
        super.onResume()
        refreshImage()
    }

    private var avatarUrl = YmyUserManager.user.avatarUrl
    private fun refreshImage() {
        avatarUrl = YmyUserManager.user.avatarUrl
        ImageLoader.loadOriginalImage(avatarUrl, mBinding.photoView, R.mipmap.icon_default_header)
    }

    override fun initData() {}
}