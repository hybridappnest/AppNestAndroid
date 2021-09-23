package com.ymy.appnest.ui.gallery.adapter.items

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.tencent.qcloud.tim.uikit.R
import com.ymy.appnest.ui.gallery.adapter.BaseGalleryItemProvider
import com.ymy.appnest.ui.gallery.adapter.IGallerySourceModel
import com.ymy.appnest.ui.gallery.bean.GalleryImage
import net.mikaelzero.mojito.view.sketch.core.Sketch
import net.mikaelzero.mojito.view.sketch.core.SketchImageView
import net.mikaelzero.mojito.view.sketch.core.SketchView
import net.mikaelzero.mojito.view.sketch.core.request.DisplayOptions

/**
 * Created on 3/22/21 16:41.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class ItemProviderImage(
    override val itemViewType: Int = IGallerySourceModel.image,
    override val layoutId: Int = R.layout.item_gallery_image
) : BaseGalleryItemProvider() {
    override fun getBaseVideoHolder(view: View) = GalleryImageHolder(view)

    override fun convert(helper: BaseViewHolder, item: IGallerySourceModel) {
        if (helper is GalleryImageHolder) {
            helper.data = item as GalleryImage
        }
    }

    class GalleryImageHolder(
        val view: View
    ) : BaseGalleryItemProvider.BaseVideoHolder(view) {
        init {
            view.tag = this
        }

        private val flImageRoot: FrameLayout = getView(R.id.root_view)
        private val sketchImageView: SketchImageView by lazy {
            SketchImageView(view.context)
        }

        /**
         * 根据数据初始化视图
         */
        override fun initViewByData() {
            sketchImageView.run {
                isZoomEnabled = true
                options.isDecodeGifImage = true
            }
            val childAt = flImageRoot.getChildAt(0)
            if (childAt != sketchImageView) {
                flImageRoot.addView(sketchImageView)
            }
            data?.run {
                val galleryImage = this as GalleryImage
                Sketch.with(view.context).display(galleryImage.imageUrl, sketchImageView)
                    .loadingImage { _: Context?, _: SketchView?, _: DisplayOptions? ->
                        (sketchImageView).drawable // 解决缩略图切换到原图显示的时候会闪烁的问题
                    }.commit()
            }
        }
    }
}