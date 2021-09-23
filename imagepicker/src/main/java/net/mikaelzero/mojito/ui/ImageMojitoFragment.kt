package net.mikaelzero.mojito.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ymy.image.databinding.FragmentImageBinding
import net.mikaelzero.mojito.Mojito.Companion.imageLoader
import net.mikaelzero.mojito.Mojito.Companion.imageViewFactory
import net.mikaelzero.mojito.Mojito.Companion.mojitoConfig
import net.mikaelzero.mojito.MojitoView
import net.mikaelzero.mojito.bean.FragmentConfig
import net.mikaelzero.mojito.interfaces.IMojitoFragment
import net.mikaelzero.mojito.interfaces.IProgress
import net.mikaelzero.mojito.interfaces.ImageViewLoadFactory
import net.mikaelzero.mojito.interfaces.OnMojitoViewCallback
import net.mikaelzero.mojito.loader.*
import net.mikaelzero.mojito.tools.BitmapUtil
import net.mikaelzero.mojito.tools.MojitoConstant
import net.mikaelzero.mojito.tools.ScreenUtils
import java.io.File


class ImageMojitoFragment : Fragment(), IMojitoFragment, OnMojitoViewCallback {
    lateinit var fragmentConfig: FragmentConfig
    var showView: View? = null
    private var mImageLoader: ImageLoader? = null
    private var mViewLoadFactory: ImageViewLoadFactory? = null
    private var contentLoader: ContentLoader? = null
    private var mainHandler = Handler(Looper.getMainLooper())
    private var iProgress: IProgress? = null
    private var fragmentCoverLoader: FragmentCoverLoader? = null

    val mBinding: FragmentImageBinding by lazy {
        FragmentImageBinding.inflate(layoutInflater)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context == null || activity == null) {
            return
        }
        if (arguments != null) {
            fragmentConfig = requireArguments().getParcelable(MojitoConstant.KEY_FRAGMENT_PARAMS)!!
        }
        mImageLoader = imageLoader()
        mViewLoadFactory = if (ImageMojitoActivity.multiContentLoader != null) {
            ImageMojitoActivity.multiContentLoader?.providerLoader(fragmentConfig.position)
        } else {
            imageViewFactory()
        }
        fragmentCoverLoader = ImageMojitoActivity.fragmentCoverLoader?.providerInstance()
        mBinding.imageCoverLayout.removeAllViews()
        val fragmentCoverAttachView = fragmentCoverLoader?.attach(this, fragmentConfig.targetUrl == null || fragmentConfig.autoLoadTarget)
        if (fragmentCoverAttachView != null) {
            mBinding.imageCoverLayout.visibility = View.VISIBLE
            mBinding.imageCoverLayout.addView(fragmentCoverAttachView)
        } else {
            mBinding.imageCoverLayout.visibility = View.GONE
        }

        iProgress = ImageMojitoActivity.progressLoader?.providerInstance()
        iProgress?.attach(fragmentConfig.position, mBinding.loadingLayout)
        contentLoader = mViewLoadFactory?.newContentLoader()
        mBinding.mojitoView.setOnMojitoViewCallback(this)
        mBinding.mojitoView.setContentLoader(contentLoader, fragmentConfig.originUrl, fragmentConfig.targetUrl)
        showView = contentLoader?.providerRealView()


        contentLoader?.onTapCallback(object : OnTapCallback {
            override fun onTap(view: View, x: Float, y: Float) {
                mBinding.mojitoView.backToMin()
                ImageMojitoActivity.onMojitoListener?.onClick(view, x, y, fragmentConfig.position)
            }
        })
        contentLoader?.onLongTapCallback(object : OnLongTapCallback {
            override fun onLongTap(view: View, x: Float, y: Float) {
                if (!mBinding.mojitoView.isDrag) {
                    ImageMojitoActivity.onMojitoListener?.onLongClick(activity, view, x, y, fragmentConfig.position)
                }
            }
        })
        val isFile: Boolean = File(fragmentConfig.originUrl).isFile
        val uri = if (isFile) {
            Uri.fromFile(File(fragmentConfig.originUrl))
        } else {
            Uri.parse(fragmentConfig.originUrl)
        }
        mImageLoader?.loadImage(showView.hashCode(), uri, !isFile, object : DefaultImageCallback() {
            override fun onSuccess(image: File) {
                mainHandler.post {
                    mViewLoadFactory?.loadSillContent(showView!!, Uri.fromFile(image))
                    startAnim(image)
                }
            }

            override fun onFail(error: Exception) {
                mainHandler.post {
                    startAnim(ScreenUtils.getScreenWidth(context), ScreenUtils.getScreenHeight(context), fragmentConfig.originUrl)
                }
            }
        })
    }


    private fun startAnim(image: File) {
        val realSizes = getRealSizeFromFile(image)
        startAnim(realSizes[0], realSizes[1])
    }


    private fun startAnim(w: Int, h: Int, needLoadImageUrl: String = "") {
        if (fragmentConfig.viewParams == null) {
            mBinding.mojitoView.showWithoutView(w, h, if (ImageMojitoActivity.hasShowedAnim) true else fragmentConfig.showImmediately)
        } else {
            mBinding.mojitoView.putData(
                fragmentConfig.viewParams!!.getLeft(), fragmentConfig.viewParams!!.getTop(),
                fragmentConfig.viewParams!!.getWidth(), fragmentConfig.viewParams!!.getHeight(),
                w, h
            )
            mBinding.mojitoView.show(if (ImageMojitoActivity.hasShowedAnim) true else fragmentConfig.showImmediately)
        }
        ImageMojitoActivity.hasShowedAnim = true

        val targetEnable = if (ImageMojitoActivity.multiContentLoader == null) {
            true
        } else {
            ImageMojitoActivity.multiContentLoader!!.providerEnableTargetLoad(fragmentConfig.position)
        }
        if (fragmentConfig.targetUrl != null && targetEnable) {
            replaceImageUrl(fragmentConfig.targetUrl!!)
        } else if (needLoadImageUrl.isNotEmpty()) {
            loadImageWithoutCache(needLoadImageUrl)
        }
    }


    private fun replaceImageUrl(url: String, forceLoadTarget: Boolean = false) {
        /**
         * forceLoadTarget 查看原图功能
         * 如果打开了自动加载原图  则隐藏查看原图
         * 如果关闭了自动加载原图:
         * 1. 需要用户点击按钮 才进行加载  forceLoadTarget 为true  强制加载目标图
         * 2. 默认进入的时候 判断是否有缓存  有的话直接加载 隐藏查看原图按钮
         */
        val onlyRetrieveFromCache: Boolean = if (forceLoadTarget) {
            !forceLoadTarget
        } else {
            !fragmentConfig.autoLoadTarget
        }
        mImageLoader?.loadImage(showView.hashCode(), Uri.parse(url), onlyRetrieveFromCache, object : DefaultImageCallback() {
            override fun onStart() {
                handleImageOnStart()
            }

            override fun onProgress(progress: Int) {
                handleImageOnProgress(progress)
            }

            override fun onFail(error: Exception?) {
                loadImageFail(onlyRetrieveFromCache)
            }

            override fun onSuccess(image: File) {
                mainHandler.post {
                    handleImageOnSuccess(image)
                }
            }
        })
    }

    /**
     *  如果图片还未加载出来  则加载图片  最后通知修改宽高
     */
    private fun loadImageWithoutCache(url: String) {
        mImageLoader?.loadImage(showView.hashCode(), Uri.parse(url), false, object : DefaultImageCallback() {
            override fun onStart() {
                handleImageOnStart()
            }

            override fun onProgress(progress: Int) {
                handleImageOnProgress(progress)
            }

            override fun onFail(error: Exception?) {
                loadImageFail(false)
            }

            override fun onSuccess(image: File) {
                mainHandler.post {
                    handleImageOnSuccess(image)
                    val realSizes = getRealSizeFromFile(image)
                    mBinding.mojitoView.resetSize(realSizes[0], realSizes[1])
                }
            }
        })
    }

    private fun handleImageOnStart() {
        mainHandler.post {
            if (mBinding.loadingLayout.visibility == View.GONE) {
                mBinding.loadingLayout.visibility = View.VISIBLE
            }
            iProgress?.onStart(fragmentConfig.position)
        }
    }

    private fun handleImageOnProgress(progress: Int) {
        mainHandler.post {
            if (mBinding.loadingLayout.visibility == View.GONE) {
                mBinding.loadingLayout.visibility = View.VISIBLE
            }
            iProgress?.onProgress(fragmentConfig.position, progress)
        }
    }

    private fun handleImageOnSuccess(image: File) {
        if (mBinding.loadingLayout.visibility == View.VISIBLE) {
            mBinding.loadingLayout.visibility = View.GONE
        }
        fragmentCoverLoader?.imageCacheHandle(isCache = true, hasTargetUrl = true)
        mViewLoadFactory?.loadSillContent(showView!!, Uri.fromFile(image))
    }

    override fun loadTargetUrl() {
        if (fragmentConfig.targetUrl == null) {
            fragmentCoverLoader?.imageCacheHandle(isCache = false, hasTargetUrl = false)
        } else {
            replaceImageUrl(fragmentConfig.targetUrl!!, true)
        }
    }

    private fun getRealSizeFromFile(image: File): Array<Int> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(image.absolutePath, options)
        val arr = BitmapUtil.getAdjustSize(image.path, options)
        var w = arr[0]
        var h = arr[1]
        val isLongImage = contentLoader?.isLongImage(w, h)
        if (isLongImage != null && isLongImage) {
            w = ScreenUtils.getScreenWidth(context)
            h = ScreenUtils.getScreenHeight(context)
        }
        return arrayOf(w, h)
    }

    private fun loadImageFail(onlyRetrieveFromCache: Boolean) {
        if (!onlyRetrieveFromCache) {
            val errorDrawableResId = mojitoConfig().errorDrawableResId()
            if (errorDrawableResId != 0) {
                mViewLoadFactory?.loadContentFail(showView!!, errorDrawableResId)
            }
        }
        mainHandler.post {
            if (mBinding.loadingLayout.visibility == View.GONE) {
                mBinding.loadingLayout.visibility = View.VISIBLE
            }
            iProgress?.onFailed(fragmentConfig.position)
            fragmentCoverLoader?.imageCacheHandle(isCache = false, hasTargetUrl = true)
        }
    }

    override fun backToMin() {
        mBinding.mojitoView.backToMin()
    }

    override fun providerContext(): Fragment? {
        return this
    }

    override fun onResume() {
        contentLoader?.pageChange(false)
        super.onResume()
    }

    override fun onPause() {
        contentLoader?.pageChange(true)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mImageLoader?.cancel(showView.hashCode())
    }

    companion object {
        fun newInstance(fragmentConfig: FragmentConfig): ImageMojitoFragment {
            val args = Bundle()
            args.putParcelable(MojitoConstant.KEY_FRAGMENT_PARAMS, fragmentConfig)
            val fragment = ImageMojitoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onMojitoViewFinish() {
        ImageMojitoActivity.onMojitoListener?.onMojitoViewFinish()
        if (context is ImageMojitoActivity) {
            (context as ImageMojitoActivity).finishView()
        }
    }

    override fun onDrag(view: MojitoView, moveX: Float, moveY: Float) {
        ImageMojitoActivity.iIndicator?.move(moveX, moveY)
        ImageMojitoActivity.activityCoverLoader?.move(moveX, moveY)
        fragmentCoverLoader?.move(moveX, moveY)
        ImageMojitoActivity.onMojitoListener?.onDrag(view, moveX, moveY)
    }

    override fun onRelease(isToMax: Boolean, isToMin: Boolean) {
        ImageMojitoActivity.iIndicator?.fingerRelease(isToMax, isToMin)
        fragmentCoverLoader?.fingerRelease(isToMax, isToMin)
        ImageMojitoActivity.activityCoverLoader?.fingerRelease(isToMax, isToMin)
    }

    override fun showFinish(mojitoView: MojitoView, showImmediately: Boolean) {
        ImageMojitoActivity.onMojitoListener?.onShowFinish(mojitoView, showImmediately)
    }

    override fun onLongImageMove(ratio: Float) {
        ImageMojitoActivity.onMojitoListener?.onLongImageMove(ratio)
    }

    override fun onLock(isLock: Boolean) {
        if (context is ImageMojitoActivity) {
            (context as ImageMojitoActivity).setViewPagerLock(isLock)
        }
    }

}