package com.ymy.image.imagepicker

import android.content.Context
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.loader.glide.GlideImageLoader
import net.mikaelzero.mojito.view.sketch.SketchImageLoadFactory

/**
 * Created on 2020/8/10 11:06.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:初始化mojito图片视频浏览
 */
object ImagePreManager {
    fun init(context: Context) {
        Mojito.initialize(
            GlideImageLoader.with(context),
            SketchImageLoadFactory()
        )
    }
}