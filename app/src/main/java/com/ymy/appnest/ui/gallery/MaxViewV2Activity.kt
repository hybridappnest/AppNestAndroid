package com.ymy.appnest.ui.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ymy.core.base.RootActivity
import com.ymy.core.base.getColorCompat
import com.ymy.core.bean.MaxBean
import com.ymy.core.utils.StatusBarTool
import com.ymy.appnest.R
import com.ymy.appnest.databinding.MaxViewV2LayoutBinding
import com.ymy.appnest.ui.gallery.adapter.IGallerySourceModel
import com.ymy.appnest.ui.gallery.adapter.items.ItemProviderVideo
import com.ymy.appnest.ui.gallery.bean.GalleryImage
import com.ymy.appnest.ui.gallery.bean.GalleryVideo
import com.ymy.player.video.videoview.AlivcVideoView

/**
 * Created on 3/20/21 09:45.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class MaxViewV2Activity : RootActivity() {
    lateinit var mBinding: MaxViewV2LayoutBinding

    companion object {
        var maxBean: MaxBean? = null

        @JvmStatic
        fun invoke(context: Context, data: MaxBean) {
            if (data.data.isEmpty()) {
                return
            }
            maxBean = data
            val intent = Intent(context, MaxViewV2Activity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        mAlivcVideoView.onResume()
        mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                @Px positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                pageNum.text = "${(position + 1)}/${dataList.size}"
                this@MaxViewV2Activity.onPageSelected(position)
            }

            override fun onPageScrollStateChanged(@ViewPager2.ScrollState state: Int) {

            }
        })
        mBinding.viewPager.postDelayed({
            val currentItem = mBinding.viewPager.currentItem
            onPageSelected(currentItem)
        }, 500)
    }

    override fun onPause() {
        super.onPause()
        mAlivcVideoView.onPause()
    }

    override fun onDestroy() {
        maxBean = null
        dataList.clear()
        mAlivcVideoView.onDestroy()
        super.onDestroy()
    }

    override fun onStop() {
        mAlivcVideoView.onStop()
        super.onStop()
    }

    val mAdapter: GalleryAdapter by lazy {
        GalleryAdapter()
    }

    val pageNum: TextView by lazy {
        findViewById<TextView>(R.id.position)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        StatusBarTool.setStatusBarColor(this, getColorCompat(R.color.black))
        mBinding = MaxViewV2LayoutBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initData()
        initView()
    }

    val dataList: MutableList<IGallerySourceModel> = mutableListOf()

    private fun initData() {
        if (maxBean == null) {
            finish()
            return
        }
        var index = 0
        maxBean?.run {
            data.forEach {
                when (it.type) {
                    IGallerySourceModel.video.toString() -> {
                        dataList.add(
                            GalleryVideo(
                                it.url,
                                index++,
                                it.coverUrl
                            )
                        )
                    }
                    IGallerySourceModel.image.toString() -> {
                        dataList.add(
                            GalleryImage(
                                it.url
                            )
                        )
                    }
                }
            }
        }
    }

    var recycle: RecyclerView? = null

    @SuppressLint("SetTextI18n")
    private fun initView() {
        mBinding.photoViewBack.setOnClickListener { finish() }
        initViewPager()
        recycle = mBinding.viewPager.getChildAt(0) as RecyclerView
        maxBean?.run {
            mBinding.viewPager.currentItem = index
            pageNum.text = "${(index + 1)}/${dataList.size}"
        }
    }

    val mAlivcVideoView: AlivcVideoView by lazy {
        AlivcVideoView(this)
    }
    var lastVideoViewHolder: ItemProviderVideo.GalleryVideoHolder? = null
    var currentPosition: Int? = 0

    private fun onPageSelected(position: Int) {
        currentPosition = position
        lastVideoViewHolder?.unSelected()
        val viewholder = recycle?.findViewHolderForAdapterPosition(position)
        if (viewholder is ItemProviderVideo.GalleryVideoHolder) {
            lastVideoViewHolder = viewholder
            lastVideoViewHolder?.onSelected(mAlivcVideoView)
        }
    }

    private fun initViewPager() {
        mBinding.viewPager.run {
            offscreenPageLimit = 2
            adapter = mAdapter
        }
        mAdapter.setList(dataList)
    }
}