package com.ymy.image.imagepicker.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.lcw.library.imagepicker.ImagePicker
import com.ymy.core.Ktx
import com.ymy.core.base.RootActivity
import com.ymy.core.utils.MediaFileUtil
import com.ymy.image.imagepicker.fragments.ImagePickerFragment
import com.ymy.image.imagepicker.viewmodel.PickerViewModel
import com.ymy.image.databinding.ActivityDbxImagepickerBinding
import org.koin.android.ext.android.getKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import java.util.*

const val PICKER_IMAGE_SCOPE = "PICKER_IMAGE_SCOPE"
const val ImagePickerScopeId = "ImagePickerScope"

/**
 * Created on 2020/7/29 09:05.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class ImagePickerActivity : RootActivity() {
    private lateinit var mImagePickerScope: Scope

    private val titleList = arrayOf("相册", "拍照", "拍视频")
    private val fragmentList = arrayListOf<Fragment>()
    private val imagePickerFragment by lazy { ImagePickerFragment.newInstance() }

    //    private val imagePhotoFragment by lazy { ImagePhotoFragment.newInstance() }
//    private val imageVideoFragment by lazy { ImageVideoFragment.newInstance() }
    private var mOnPageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    private lateinit var mPickerViewModel: PickerViewModel

    private fun initVM(): PickerViewModel {
        mImagePickerScope =
            Ktx.app.getKoin().getOrCreateScope(ImagePickerScopeId, named(PICKER_IMAGE_SCOPE))
        return mImagePickerScope.get()
    }

    init {
        fragmentList.add(imagePickerFragment)
//        fragmentList.add(imagePhotoFragment)
//        fragmentList.add(imageVideoFragment)
    }

    val mBinding: ActivityDbxImagepickerBinding by lazy {
        ActivityDbxImagepickerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initView()
        initData()
    }

    fun initView() {
        mPickerViewModel = initVM()
        mPickerViewModel.reSet()
        startObserve()

        initViewPager()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    private fun startObserve() {
        mPickerViewModel.pickerLiveData.observe(this@ImagePickerActivity, Observer {
            if (it == null) {
                return@Observer
            }
            if (it.commitSelection) {
                if (it.cancel || it.resultList.isEmpty()) {
                    setResult(Activity.RESULT_CANCELED)
                } else {
                    val filter = it.resultList.filter { filePath ->
                        val mediaFileType = MediaFileUtil.getFileType(filePath)
                        if (mediaFileType != null
                            && MediaFileUtil.isVideoFileType(mediaFileType.fileType)
                        ) {
                            if (mediaFileType.fileType == MediaFileUtil.FILE_TYPE_MP4) {
                                true
                            } else {
                                toast("视频格式错误，只能上传MP4格式的视频")
                                false
                            }
                        } else {
                            true
                        }
                    }
                    val list = ArrayList(filter)
                    val intent = Intent()
                    intent.putStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES, list)
                    setResult(Activity.RESULT_OK, intent)
                }
                finish()
            }
        })
    }

    override fun onDestroy() {
        mPickerViewModel.removeAll()
        mImagePickerScope.close()
        super.onDestroy()
    }


    private fun initViewPager() {
        mBinding.viewPager.run {
            offscreenPageLimit = 1
            isUserInputEnabled = false
            adapter = object : FragmentStateAdapter(this@ImagePickerActivity) {
                override fun createFragment(position: Int) = fragmentList[position]

                override fun getItemCount() = fragmentList.size
            }
        }


        TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager) { tab, position ->
            tab.text = titleList[position]
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        mOnPageChangeCallback?.let { mBinding.viewPager.registerOnPageChangeCallback(it) }
    }

    override fun onStop() {
        super.onStop()
        mOnPageChangeCallback?.let { mBinding.viewPager.unregisterOnPageChangeCallback(it) }
    }

    fun initData() {

    }

}