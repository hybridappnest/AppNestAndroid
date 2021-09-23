package com.ymy.image.imagepicker.adapter

import androidx.appcompat.widget.AppCompatImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ymy.core.glide.ImageLoader
import com.ymy.image.R

open class PhotoVideoUrlAdapter(
    layoutResId: Int = R.layout.item_selected_photo
) :
    BaseQuickAdapter<String, BaseViewHolder>(layoutResId) {

    override fun convert(
        holder: BaseViewHolder,
        item: String
    ) {
        item.apply {
            val view = holder.getView<AppCompatImageView>(R.id.iv_photo)
            ImageLoader.loadWithPlaceHolder(item, view)
        }
    }

}