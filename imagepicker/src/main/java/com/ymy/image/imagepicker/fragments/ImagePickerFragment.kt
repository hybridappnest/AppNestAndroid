package com.ymy.image.imagepicker.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lcw.library.imagepicker.ImagePicker
import com.lcw.library.imagepicker.adapter.ImageFoldersAdapter
import com.lcw.library.imagepicker.adapter.ImagePickerAdapter
import com.lcw.library.imagepicker.data.MediaFile
import com.lcw.library.imagepicker.data.MediaFolder
import com.lcw.library.imagepicker.executors.CommonExecutor
import com.lcw.library.imagepicker.listener.MediaLoadCallback
import com.lcw.library.imagepicker.manager.ConfigManager
import com.lcw.library.imagepicker.provider.ImagePickerProvider
import com.lcw.library.imagepicker.task.ImageLoadTask
import com.lcw.library.imagepicker.task.MediaLoadTask
import com.lcw.library.imagepicker.task.VideoLoadTask
import com.lcw.library.imagepicker.utils.Utils
import com.lcw.library.imagepicker.view.ImageFolderPopupWindow
import com.ymy.core.Ktx
import com.ymy.core.base.RootFragment
import com.ymy.core.utils.BitmapOrientationUtils
import com.ymy.core.utils.MediaFileUtil
import com.ymy.core.utils.WatermarkSettings
import com.ymy.image.R
import com.ymy.image.databinding.FragmentImagepickerBinding
import com.ymy.image.imagepicker.activity.ImagePickerScopeId
import com.ymy.image.imagepicker.adapter.PhotoVideoUrlAdapter
import com.ymy.image.imagepicker.cover.SaveActivityCoverLoader
import com.ymy.image.imagepicker.video.ArtLoadFactory
import com.ymy.image.imagepicker.viewmodel.PickerViewModel
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.NumIndicator
import net.mikaelzero.mojito.interfaces.IProgress
import net.mikaelzero.mojito.interfaces.ImageViewLoadFactory
import net.mikaelzero.mojito.loader.InstanceLoader
import net.mikaelzero.mojito.loader.MultiContentLoader
import net.mikaelzero.mojito.view.sketch.SketchImageLoadFactory
import org.koin.android.ext.android.getKoin
import org.koin.core.scope.Scope
import java.io.File

/**
 * Created on 2020/7/29 09:10.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:????????????
 */
class ImagePickerFragment : RootFragment(), ImagePickerAdapter.OnItemClickListener,
    ImageFoldersAdapter.OnImageFolderChangeListener {

    private lateinit var mImagePickerScope: Scope
    private lateinit var mPickerViewModel: PickerViewModel
    val mBinding: FragmentImagepickerBinding by lazy {
        FragmentImagepickerBinding.inflate(layoutInflater)
    }

    private fun initVM(): PickerViewModel {
        mImagePickerScope =
            Ktx.app.getKoin().getScope(ImagePickerScopeId)
        return mImagePickerScope.get()
    }

    /**
     * ????????????
     */
    private var isShowImage = false
    private var isShowVideo = false
    private var isSingleType = false
    private var isShowCamera = false
    private var mMaxCount = 0
    private var mImagePaths: List<String>? = null

    //??????????????????
    private var isShowTime = false

    lateinit var mProgressDialog: ProgressDialog
    lateinit var mGridLayoutManager: GridLayoutManager
    lateinit var mImagePickerAdapter: ImagePickerAdapter

    //???????????????
    private var mMediaFileList = ArrayList<MediaFile>()
    private var mMediaPathList = ArrayList<String>()

    //??????????????????
    private var mMediaFolderList = ArrayList<MediaFolder>()
    private lateinit var mImageFolderPopupWindow: ImageFolderPopupWindow

    private val mMyHandler = Handler()
    private val mHideRunnable = Runnable { hideImageTime() }

    /**
     * ????????????
     */
    private var mFilePath: String? = null
    private val REQUEST_CODE_CAPTURE = 0x02 //??????????????????


    private val mMediaLoader: MediaLoadCallback =
        MediaLoadCallback { mediaFolderList ->
            activity?.runOnUiThread {
                if (mediaFolderList.isNotEmpty()) {
                    //????????????????????????
                    mMediaFileList.addAll(mediaFolderList[0].mediaFileList)
                    mMediaPathList.clear()
                    mMediaFileList.forEach {
                        mMediaPathList.add(it.path)
                    }
                    mImagePickerAdapter.notifyDataSetChanged()

                    //?????????????????????
                    mMediaFolderList = ArrayList(mediaFolderList)
                    mImageFolderPopupWindow =
                        ImageFolderPopupWindow(context, mMediaFolderList)
                    mImageFolderPopupWindow.animationStyle = R.style.imageFolderAnimator
                    mImageFolderPopupWindow.adapter
                        .setOnImageFolderChangeListener(this)
                    mImageFolderPopupWindow.setOnDismissListener {
                        setLightMode(LIGHT_ON)
                    }
                    updateCommitButton()
                }
                mProgressDialog.cancel()
            }
        }

    companion object {
        fun newInstance(): ImagePickerFragment {
            return ImagePickerFragment()
        }

        //??????????????????
        const val LIGHT_OFF = 0
        const val LIGHT_ON = 1
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
        initConfig()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    fun initView() {
        mProgressDialog =
            ProgressDialog.show(requireContext(), null, getString(R.string.scanner_image))
        mGridLayoutManager = GridLayoutManager(context, 4)
        mBinding.rvMainImages.run {
            layoutManager = mGridLayoutManager
            //??????????????????Adapter???Item?????????????????????RecyclerView?????????????????????????????????true???RecyclerView???????????????????????????
            setHasFixedSize(true)
            setItemViewCacheSize(60)

            mImagePickerAdapter = ImagePickerAdapter(context, mMediaFileList, mPickerViewModel)
            mImagePickerAdapter.setOnItemClickListener(this@ImagePickerFragment)
            adapter = mImagePickerAdapter
        }
        initListener()

        initSelectImagesRecyclerView()
    }

    private val mPhotoVideoUrlAdapter by lazy { PhotoVideoUrlAdapter() }
    private fun initSelectImagesRecyclerView() {
        val mSelectImagePaths = mPickerViewModel.mSelectImagePaths
        if (mSelectImagePaths.size > 0) {
            mBinding.flSelectedLayout.visibility = View.VISIBLE
        }
        mBinding.rvSelectImages.run {
            adapter = mPhotoVideoUrlAdapter
            mPhotoVideoUrlAdapter.setList(mSelectImagePaths)
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            mPhotoVideoUrlAdapter.setOnItemClickListener { adapter, _, position ->
                val unSelectedImage = adapter.data[position] as String
                mPickerViewModel.addImageToSelectList(unSelectedImage)
                adapter.data.remove(unSelectedImage)
                adapter.notifyDataSetChanged()
                if (adapter.data.size == 0) {
                    mBinding.flSelectedLayout.visibility = View.GONE
                }
                mImagePickerAdapter.notifyDataSetChanged()
                updateCommitButton()
            }
        }
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            updateImageTime()
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            updateImageTime()
        }
    }

    /**
     * ???????????????????????????
     */
    private fun initListener() {
        mBinding.ivActionBarBack.setOnClickListener {
            mPickerViewModel.commitSelection(cancel = true)
        }
        mBinding.tvActionBarCommit.setOnClickListener {
            val filter = mPickerViewModel.mSelectImagePaths.filter {
                MediaFileUtil.isImageFileType(it)
            }
            if (filter.isEmpty()) {
                commitSelection()
            } else {
                WatermarkSettings.startAddWaterMarkForList(
                    Ktx.app,
                    filter,
                    "???????????????",
                    object : WatermarkSettings.CallBack {
                        override fun onSuccess(resultMap: Map<String, String>) {
                            resultMap.forEach{
                                val indexOf = mPickerViewModel.mSelectImagePaths.indexOf(it.key)
                                if (indexOf > -1) {
                                    mPickerViewModel.mSelectImagePaths[indexOf] =
                                        it.value
                                }
                            }
                            commitSelection()
                        }

                        override fun showLoading(show: Boolean) {
                        }

                        override fun onError(errorMsg: String) {
                        }

                    })
            }
        }
        mBinding.tvMainImageFolders.setOnClickListener {
            setLightMode(LIGHT_OFF)
            mImageFolderPopupWindow.showAsDropDown(mBinding.tvMainImageFolders, 0, 0)
        }
    }


    fun initData() {
        getData()
    }

    /**
     * ???????????????
     */
    private fun getData() {
        //?????????????????????
        startScannerTask()
    }

    /**
     * ??????????????????
     */
    private fun startScannerTask() {
        var mediaLoadTask: Runnable? = null

        //???????????????????????????
        if (isShowImage && isShowVideo) {
            mediaLoadTask = MediaLoadTask(
                context, mMediaLoader
            )
        }

        //???????????????
        if (!isShowImage && isShowVideo) {
            mediaLoadTask = VideoLoadTask(
                context, mMediaLoader
            )
        }

        //???????????????
        if (isShowImage && !isShowVideo) {
            mediaLoadTask = ImageLoadTask(
                context, mMediaLoader
            )
        }

        //?????????????????????????????????????????????????????????
        if (mediaLoadTask == null) {
            mediaLoadTask = MediaLoadTask(
                context, mMediaLoader
            )
        }
        CommonExecutor.getInstance().execute(mediaLoadTask)
    }

    /**
     * ????????????
     */
    private fun updateImageTime() {
        val position = mGridLayoutManager.findFirstVisibleItemPosition()
        if (position != RecyclerView.NO_POSITION) {
            val mediaFile = mImagePickerAdapter.getMediaFile(position)
            if (mediaFile != null) {
                if (mBinding.tvImageTime.visibility != View.VISIBLE) {
                    mBinding.tvImageTime.visibility = View.VISIBLE
                }
                val time = Utils.getImageTime(mediaFile.dateToken)
                mBinding.tvImageTime.text = time
                showImageTime()
                mMyHandler.removeCallbacks(mHideRunnable)
                mMyHandler.postDelayed(mHideRunnable, 1500)
            }
        }
    }

    /**
     * ????????????
     */
    private fun hideImageTime() {
        if (isShowTime) {
            isShowTime = false
            ObjectAnimator.ofFloat(mBinding.tvImageTime, "alpha", 1f, 0f).setDuration(300).start()
        }
    }

    /**
     * ????????????
     */
    private fun showImageTime() {
        if (!isShowTime) {
            isShowTime = true
            ObjectAnimator.ofFloat(mBinding.tvImageTime, "alpha", 0f, 1f).setDuration(300).start()
        }
    }


    private fun commitSelection() {
        mPickerViewModel.commitSelection(cancel = false)
    }


    /**
     * ????????????????????????
     */
    @SuppressLint("SetTextI18n")
    private fun updateCommitButton() {
        //??????????????????UI
        val selectCount = mPickerViewModel.mSelectImagePaths.size
        if (selectCount == 0) {
            mBinding.tvActionBarCommit.run {
                isEnabled = false
                text = "??????($selectCount/$mMaxCount)"
            }
            return
        }
        if (selectCount <= mMaxCount) {
            mBinding.tvActionBarCommit.run {
                isEnabled = true
                text = "??????($selectCount/$mMaxCount)"
            }
            return
        }
    }

    /**
     * ???????????????????????????
     *
     * @param lightMode
     */
    private fun setLightMode(lightMode: Int) {
        val layoutParams: WindowManager.LayoutParams? = activity?.window?.attributes
        layoutParams?.let {
            when (lightMode) {
                LIGHT_OFF -> it.alpha =
                    0.7f
                LIGHT_ON -> it.alpha =
                    1.0f
            }
            activity?.window?.attributes = it
        }
    }


    /**
     * ???????????????
     */
    private fun initConfig() {
        isShowImage = ConfigManager.getInstance().isShowImage
        isShowCamera = ConfigManager.getInstance().isShowCamera
        isShowVideo = ConfigManager.getInstance().isShowVideo
        isSingleType = ConfigManager.getInstance().isSingleType
        mMaxCount = ConfigManager.getInstance().maxCount
        needWatermark = ConfigManager.getInstance().isNeedWatermark
        mPickerViewModel.mMaxCount = mMaxCount

        //????????????????????????
        mImagePaths = ConfigManager.getInstance().imagePaths
        if (mImagePaths != null && (mImagePaths as ArrayList<String>).isNotEmpty()) {
            mPickerViewModel.addImagePathsToSelectList(mImagePaths)
        }
    }

    override fun onMediaCheck(view: View?, position: Int) {
        if (isShowCamera) {
            if (position == 0) {
                showCamera()
                return
            }
        }
        //????????????/????????????
        val mediaFile = mImagePickerAdapter.getMediaFile(position)
        if (mediaFile != null) {
            val imagePath = mediaFile.path
            if (isSingleType) {
                //???????????????????????????????????????????????????????????????????????????????????????
                val selectPathList = mPickerViewModel.mSelectImagePaths
                if (!selectPathList.isEmpty()) {
                    //?????????????????????????????????????????????
                    if (!mPickerViewModel.isCanAddSelectionPaths(
                            imagePath,
                            selectPathList[0]
                        )
                    ) {
                        //????????????
                        Toast.makeText(
                            context,
                            getString(R.string.single_type_choose),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
            }
            val addSuccess =
                mPickerViewModel.addImageToSelectList(imagePath)
            if (addSuccess) {
                mImagePickerAdapter.notifyItemChanged(position)
                val list = mPickerViewModel.mSelectImagePaths
                mPhotoVideoUrlAdapter.setList(list)
                if (list.size > 0) {
                    mBinding.flSelectedLayout.visibility = View.VISIBLE
                } else {
                    mBinding.flSelectedLayout.visibility = View.GONE
                }
            } else {
                Toast.makeText(
                    context,
                    String.format(getString(R.string.select_image_max), mMaxCount),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        updateCommitButton()
    }

    override fun onMediaClick(view: View?, position: Int) {
        if (isShowCamera) {
            if (position == 0) {
                showCamera()
                return
            }
        }
        var data = arrayListOf<String>()
        data.addAll(mMediaPathList)
        if (isShowCamera) {
            data.add(0, "")
        }
        Mojito.with(context)
            .urls(data)
            .position(position)
            .views(mBinding.rvMainImages, R.id.iv_item_image)
            .autoLoadTarget(false)
            .setActivityCoverLoader(SaveActivityCoverLoader())
            .setProgressLoader(object : InstanceLoader<IProgress> {
                override fun providerInstance(): IProgress {
                    return DefaultPercentProgress()
                }
            }).setMultiContentLoader(object : MultiContentLoader {
                override fun providerEnableTargetLoad(position: Int): Boolean {
                    return false
                }

                override fun providerLoader(position: Int): ImageViewLoadFactory {
                    return if (MediaFileUtil.isVideoFileType(data[position])) {
                        ArtLoadFactory()
                    } else {
                        SketchImageLoadFactory()
                    }
                }
            })
            .setIndicator(NumIndicator())
            .start()
    }

    private fun showCamera() {
        if (ImagePicker.getInstance().cameraView != null) {
            ImagePicker.getInstance().cameraView.goCameraView(context,
                if (isShowVideo) {
                    ImagePicker.CameraView.type_all
                } else {
                    ImagePicker.CameraView.type_photo
                },
                object : ImagePicker.CameraViewCallBack {
                    override fun onSuccess(type: Int, data: String) {
                        if (type == ImagePicker.CameraViewCallBack.type_photo) {
                            BitmapOrientationUtils.revisePhotoOrientation(requireContext(), data)
                            if (needWatermark) {
                                WatermarkSettings.createWatermark(requireContext(), data)
                            }
                        }
                        //?????????????????????
                        mPickerViewModel.mSelectImagePaths.clear()
                        mPickerViewModel.addImageToSelectList(data)
                        commitSelection()
                    }

                    override fun onError(module: String?, errCode: Int, errMsg: String?) {

                    }

                })
        } else {
            val imageUri = getImageUri()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, REQUEST_CODE_CAPTURE)
        }
    }

    private fun getImageUri(): Uri {
        //??????????????????
        val fileDir =
            File(Environment.getExternalStorageDirectory(), "Pictures")
        if (!fileDir.exists()) {
            fileDir.mkdir()
        }
        mFilePath =
            fileDir.absolutePath + "/IMG_" + System.currentTimeMillis() + ".jpg"
        return if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(
                requireContext(),
                ImagePickerProvider.getFileProviderName(requireContext()),
                File(mFilePath)
            )
        } else {
            Uri.fromFile(File(mFilePath))
        }
    }

    var needWatermark = true

    /**
     * ????????????
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAPTURE) {
                mFilePath?.run {
                    BitmapOrientationUtils.revisePhotoOrientation(requireContext(), this)
                    if (needWatermark) {
                        WatermarkSettings.createWatermark(requireContext(), this)
                    }
                    //?????????????????????
                    mPickerViewModel.addImageToSelectList(this)
                    commitSelection()
                }
            }
        }
    }

    override fun onImageFolderChange(view: View?, position: Int) {
        val mediaFolder = mMediaFolderList[position]
        //????????????????????????
        val folderName = mediaFolder.folderName
        if (!TextUtils.isEmpty(folderName)) {
            mBinding.tvMainImageFolders.text = folderName
        }
        //???????????????????????????
        mMediaFileList.clear()
        mMediaFileList.addAll(mediaFolder.mediaFileList)
        mMediaPathList.clear()
        mMediaFileList.forEach {
            mMediaPathList.add(it.path)
        }
        mImagePickerAdapter.notifyDataSetChanged()

        mImageFolderPopupWindow.dismiss()
    }

    override fun onResume() {
        super.onResume()
        mBinding.rvMainImages.addOnScrollListener(onScrollListener)
        mImagePickerAdapter.notifyDataSetChanged()
        updateCommitButton()
    }

    override fun onPause() {
        mBinding.rvMainImages.removeOnScrollListener(onScrollListener)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            ConfigManager.getInstance().imageLoader.clearMemoryCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}