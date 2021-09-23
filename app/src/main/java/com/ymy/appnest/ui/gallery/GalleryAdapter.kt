package com.ymy.appnest.ui.gallery

import com.chad.library.adapter.base.BaseProviderMultiAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.ymy.appnest.ui.gallery.adapter.IGallerySourceModel
import com.ymy.appnest.ui.gallery.adapter.items.ItemProviderImage
import com.ymy.appnest.ui.gallery.adapter.items.ItemProviderVideo

/**
 * Created on 3/22/21 16:10.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class GalleryAdapter() :
    BaseProviderMultiAdapter<IGallerySourceModel>(), LoadMoreModule {

    init {
        addItemProvider(ItemProviderVideo())
        addItemProvider(ItemProviderImage())
    }

    override fun getItemType(data: List<IGallerySourceModel>, position: Int) =
        data[position].getViewType()

    /**
     * 刷新数据
     * @param list
     */
    fun refreshData(list: MutableList<IGallerySourceModel>) {
        setList(list)
    }

    /**
     * 添加数据
     * @param list
     */
    fun addMoreData(list: MutableList<IGallerySourceModel>) {
        addData(list)
    }

    fun getDataList(): MutableList<IGallerySourceModel> {
        return data
    }
}