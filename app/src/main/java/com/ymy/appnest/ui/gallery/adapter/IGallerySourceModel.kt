package com.ymy.appnest.ui.gallery.adapter

/**
 * Created on 3/22/21 16:16.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
interface IGallerySourceModel {
    companion object{
        const val video = 1
        const val image = 2
    }

    /**
     * 数据样式
     * @return {VideoViewType.typeQuestion等}
     */
    fun getViewType(): Int

}