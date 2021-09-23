package com.ymy.appnest.ui.setting.fragment

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.ymy.core.base.BaseFragment
import com.ymy.core.upload.OSSManager
import com.ymy.core.upload.UploadUtils
import com.ymy.core.upload.UploadViewModel
import com.ymy.core.user.YmyUserManager
import com.ymy.core.utils.GalleryFileSaver
import com.ymy.core.view.DBXLoadingView
import com.ymy.appnest.databinding.FragmentHeaderCropperBinding
import com.ymy.appnest.ui.setting.viewmodel.UserModifyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Created on 2020/8/8 14:17.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class MineHeaderCropperFragment : BaseFragment(true) {

    val mBinding: FragmentHeaderCropperBinding by lazy {
        FragmentHeaderCropperBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View  = mBinding.root

    private val uploadViewModel: UploadViewModel by viewModel()
    private val userModifyViewModel: UserModifyViewModel by viewModel()
    private lateinit var loadingView: DBXLoadingView
    private var isUpload = false

    override fun initView() {
        mBinding.titleBar.btnBack.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
        initTitleBar()
        loadingView = DBXLoadingView(mBinding.rootLayout)
        loadingView.setLoadingText("图片上传中。。。")
    }

    override fun initData() {
        var imageUri: String = ""
        arguments?.run {
            imageUri = MineHeaderCropperFragmentArgs.fromBundle(this).imageUri
        }
        mBinding.cropImageView.run {
            setImageUriAsync(Uri.parse(imageUri))
            setAspectRatio(1, 1)
            setOnCropImageCompleteListener { _, result ->
                saveBitmapToFile(result.bitmap)
            }
            mBinding.ivRotateImage.setOnClickListener {
                rotateImage(90)
            }
        }
    }

    private fun back() {
        view?.let { Navigation.findNavController(it).popBackStack() }
    }

    private fun saveBitmapToFile(bitmap: Bitmap?) {
        bitmap?.let {
            val saveBitmapToGallery = GalleryFileSaver().saveBitmapToGallery(
                "ymy_${System.currentTimeMillis()}.jpeg", it
            )
            if (saveBitmapToGallery.isEmpty()) {
                toast("保存失败", false)
                return
            }
            context?.let { con ->
                GalleryFileSaver.insertImageToMediaStore(
                    con,
                    saveBitmapToGallery,
                    System.currentTimeMillis(),
                    it.width,
                    it.height
                )
            }

            uploadViewModel.uploadFile(
                arrayListOf(saveBitmapToGallery),
                UploadUtils.getUploadFilePath(OSSManager.headerFolder, "avatar"),
                object : UploadViewModel.CallBack {
                    override fun onSuccess(ossTag: String, resultList: ArrayList<String>) {
                        isUpload = false
                        changeAvatar(resultList[0])
                    }

                    override fun showLoading(show: Boolean) {
                        if (show) {
                            loadingView.show()
                        } else {
                            loadingView.hide()
                        }
                    }

                    override fun onError(errorMsg: String) {
                        isUpload = false
                        loadingView.hide()
                        toast(errorMsg)
                    }
                }
            )
        }
    }

    private fun changeAvatar(avatar: String) {
        userModifyViewModel.netUiLiveData.observe(this, Observer {
            it?.run {
                if (isLoading) {
                    loadingView.show()
                } else {
                    loadingView.hide()
                }
                isSuccess?.run {
                    toast("头像修改成功")
                    YmyUserManager.setUserAvatarUrl(avatar)
                    back()
                }
                isError?.run {
                    isUpload = false
                    toast(this)
                }
            }
        })
        userModifyViewModel.modifyUserInfo(avatarUrl = avatar)
    }

    private fun initTitleBar() {
        mBinding.titleBar.btnBack.setOnClickListener {
            back()
        }
        mBinding.titleBar.tvTitlebarTitle.text = ""
        mBinding.titleBar.btnRightLayout.visibility = View.VISIBLE
        mBinding.titleBar.tvBtnRight.visibility = View.VISIBLE
        mBinding.titleBar.tvBtnRight.text = "完成"
        mBinding.titleBar.tvBtnRight.setOnClickListener {
            if (!isUpload) {
                isUpload = true
                mBinding.cropImageView.getCroppedImageAsync()
            }
        }
    }
}