package com.ymy.core.glide

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.module.AppGlideModule

/**
 * Created on 2020/7/9 20:28.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
@GlideModule
class DiskCacheModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        var calculator = MemorySizeCalculator.Builder(context.applicationContext)
            .setMemoryCacheScreens(2f)
            .build()
        val calculatorBitmap = MemorySizeCalculator.Builder(context)
            .setBitmapPoolScreens(3f)
            .build()
        val diskCacheSizeBytes = 1024 * 1024 * 100 // 100 MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context.applicationContext, "glideImageCache", diskCacheSizeBytes.toLong()))
            .setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))
            .setBitmapPool(LruBitmapPool(calculatorBitmap.bitmapPoolSize.toLong()))
    }
}