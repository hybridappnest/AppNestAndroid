package com.ymy.appnest.ui.gallery.bean

import com.ymy.appnest.ui.gallery.adapter.IGallerySourceModel

/**
 * Created on 3/26/21 10:12.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
data class GalleryVideo(
    val url: String? = "",
    val uuid: Int = 0,
    val firstFrame: String? = "",
) : IGallerySourceModel {
    /**
     * 数据样式
     * @return {VideoViewType.typeQuestion等}
     */
    override fun getViewType() = IGallerySourceModel.video

}