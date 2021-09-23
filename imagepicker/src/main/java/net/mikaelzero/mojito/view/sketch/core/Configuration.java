/*
 * Copyright (C) 2019 Peng fei Pan <panpfpanpf@outlook.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mikaelzero.mojito.view.sketch.core;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPool;
import net.mikaelzero.mojito.view.sketch.core.cache.DiskCache;
import net.mikaelzero.mojito.view.sketch.core.cache.LruBitmapPool;
import net.mikaelzero.mojito.view.sketch.core.cache.LruDiskCache;
import net.mikaelzero.mojito.view.sketch.core.cache.LruMemoryCache;
import net.mikaelzero.mojito.view.sketch.core.cache.MemoryCache;
import net.mikaelzero.mojito.view.sketch.core.cache.MemorySizeCalculator;
import net.mikaelzero.mojito.view.sketch.core.decode.ImageDecoder;
import net.mikaelzero.mojito.view.sketch.core.decode.ImageOrientationCorrector;
import net.mikaelzero.mojito.view.sketch.core.decode.ImageSizeCalculator;
import net.mikaelzero.mojito.view.sketch.core.decode.ProcessedImageCache;
import net.mikaelzero.mojito.view.sketch.core.decode.ResizeCalculator;
import net.mikaelzero.mojito.view.sketch.core.display.DefaultImageDisplayer;
import net.mikaelzero.mojito.view.sketch.core.display.ImageDisplayer;
import net.mikaelzero.mojito.view.sketch.core.http.HttpStack;
import net.mikaelzero.mojito.view.sketch.core.http.HurlStack;
import net.mikaelzero.mojito.view.sketch.core.http.ImageDownloader;
import net.mikaelzero.mojito.view.sketch.core.optionsfilter.OptionsFilterManager;
import net.mikaelzero.mojito.view.sketch.core.process.ImageProcessor;
import net.mikaelzero.mojito.view.sketch.core.process.ResizeImageProcessor;
import net.mikaelzero.mojito.view.sketch.core.request.FreeRideManager;
import net.mikaelzero.mojito.view.sketch.core.request.HelperFactory;
import net.mikaelzero.mojito.view.sketch.core.request.RequestExecutor;
import net.mikaelzero.mojito.view.sketch.core.request.RequestFactory;
import net.mikaelzero.mojito.view.sketch.core.uri.UriModelManager;


/**
 * {@link Sketch} 唯一配置类
 */
@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public final class Configuration {
    private static final String NAME = "Configuration";

    @NonNull
    private Context context;

    @NonNull
    private UriModelManager uriModelManager;
    @NonNull
    private OptionsFilterManager optionsFilterManager;

    @NonNull
    private DiskCache diskCache;
    @NonNull
    private BitmapPool bitmapPool;
    @NonNull
    private MemoryCache memoryCache;
    @NonNull
    private ProcessedImageCache processedImageCache;

    @NonNull
    private HttpStack httpStack;
    @NonNull
    private ImageDecoder decoder;
    @NonNull
    private ImageDownloader downloader;
    @NonNull
    private ImageOrientationCorrector orientationCorrector;

    @NonNull
    private ImageDisplayer defaultDisplayer;
    @NonNull
    private ImageProcessor resizeProcessor;
    @NonNull
    private ResizeCalculator resizeCalculator;
    @NonNull
    private ImageSizeCalculator sizeCalculator;

    @NonNull
    private RequestExecutor executor;
    @NonNull
    private FreeRideManager freeRideManager;
    @NonNull
    private HelperFactory helperFactory;
    @NonNull
    private RequestFactory requestFactory;
    @NonNull
    private ErrorTracker errorTracker;

    Configuration(@NonNull Context context) {
        context = context.getApplicationContext();
        this.context = context;

        this.uriModelManager = new UriModelManager();
        this.optionsFilterManager = new OptionsFilterManager();

        // 由于默认的缓存文件名称从 URLEncoder 加密变成了 MD5 所以这里要升级一下版本号，好清除旧的缓存
        this.diskCache = new LruDiskCache(context, this, 2, DiskCache.DISK_CACHE_MAX_SIZE);
        MemorySizeCalculator memorySizeCalculator = new MemorySizeCalculator(context);
        this.bitmapPool = new LruBitmapPool(context, memorySizeCalculator.getBitmapPoolSize());
        this.memoryCache = new LruMemoryCache(context, memorySizeCalculator.getMemoryCacheSize());

        this.decoder = new ImageDecoder();
        this.executor = new RequestExecutor();
        this.httpStack = new HurlStack();
        this.downloader = new ImageDownloader();
        this.sizeCalculator = new ImageSizeCalculator();
        this.freeRideManager = new FreeRideManager();
        this.resizeProcessor = new ResizeImageProcessor();
        this.resizeCalculator = new ResizeCalculator();
        this.defaultDisplayer = new DefaultImageDisplayer();
        this.processedImageCache = new ProcessedImageCache();
        this.orientationCorrector = new ImageOrientationCorrector();

        this.helperFactory = new HelperFactory();
        this.requestFactory = new RequestFactory();
        this.errorTracker = new ErrorTracker(context);

        context.getApplicationContext().registerComponentCallbacks(new MemoryChangedListener(context));
    }

    /**
     * 获取 {@link Context}
     *
     * @return {@link Context}
     */
    @NonNull
    public Context getContext() {
        return context;
    }


    @NonNull
    public UriModelManager getUriModelManager() {
        return uriModelManager;
    }


    @NonNull
    public OptionsFilterManager getOptionsFilterManager() {
        return optionsFilterManager;
    }

    /**
     * 获取磁盘缓存管理器
     *
     * @return {@link DiskCache}. 磁盘缓存管理器
     */
    @NonNull
    public DiskCache getDiskCache() {
        return diskCache;
    }

    /**
     * 设置磁盘缓存管理器
     *
     * @param newDiskCache {@link DiskCache}. 磁盘缓存管理器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setDiskCache(@NonNull DiskCache newDiskCache) {
        //noinspection ConstantConditions
        if (newDiskCache != null) {
            DiskCache oldDiskCache = diskCache;
            diskCache = newDiskCache;
            oldDiskCache.close();
            SLog.w(NAME, "diskCache=%s", diskCache.toString());
        }
        return this;
    }

    /**
     * 获取 {@link Bitmap} 复用管理器
     *
     * @return {@link BitmapPool}. {@link Bitmap} 复用管理器
     */
    @NonNull
    public BitmapPool getBitmapPool() {
        return bitmapPool;
    }

    /**
     * 设置 {@link Bitmap} 复用管理器
     *
     * @param newBitmapPool {@link BitmapPool}. {@link Bitmap} 复用管理器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setBitmapPool(@NonNull BitmapPool newBitmapPool) {
        //noinspection ConstantConditions
        if (newBitmapPool != null) {
            BitmapPool oldBitmapPool = this.bitmapPool;
            this.bitmapPool = newBitmapPool;
            oldBitmapPool.close();
            SLog.w(NAME, "bitmapPool=%s", bitmapPool.toString());
        }
        return this;
    }

    /**
     * 获取内存缓存管理器
     *
     * @return {@link MemoryCache}. 内存缓存管理器
     */
    @NonNull
    public MemoryCache getMemoryCache() {
        return memoryCache;
    }

    /**
     * 设置内存缓存管理器
     *
     * @param memoryCache {@link MemoryCache}. 内存缓存管理器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setMemoryCache(@NonNull MemoryCache memoryCache) {
        //noinspection ConstantConditions
        if (memoryCache != null) {
            MemoryCache oldMemoryCache = this.memoryCache;
            this.memoryCache = memoryCache;
            oldMemoryCache.close();
            SLog.w(NAME, "memoryCache=", memoryCache.toString());
        }
        return this;
    }

    /**
     * 获取已处理图片缓存器
     *
     * @return {@link ProcessedImageCache}. 已处理图片缓存器
     */
    @NonNull
    public ProcessedImageCache getProcessedImageCache() {
        return processedImageCache;
    }

    /**
     * 设置已处理图片缓存器
     *
     * @param processedImageCache {@link ProcessedImageCache}. 已处理图片缓存器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setProcessedImageCache(@NonNull ProcessedImageCache processedImageCache) {
        //noinspection ConstantConditions
        if (processedImageCache != null) {
            this.processedImageCache = processedImageCache;
            SLog.w(NAME, "processedCache=", processedImageCache.toString());
        }
        return this;
    }


    /**
     * 获取 HTTP 请求执行器
     *
     * @return {@link HttpStack} HTTP 请求执行器
     */
    @NonNull
    public HttpStack getHttpStack() {
        return httpStack;
    }

    /**
     * 设置 HTTP 请求执行器
     *
     * @param httpStack {@link HttpStack} HTTP 请求执行器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setHttpStack(@NonNull HttpStack httpStack) {
        //noinspection ConstantConditions
        if (httpStack != null) {
            this.httpStack = httpStack;
            SLog.w(NAME, "httpStack=", httpStack.toString());
        }
        return this;
    }

    /**
     * 获取图片解码器
     *
     * @return {@link ImageDecoder}. 图片解码器
     */
    @NonNull
    public ImageDecoder getDecoder() {
        return decoder;
    }

    /**
     * 设置图片解码器
     *
     * @param decoder {@link ImageDecoder}. 图片解码器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setDecoder(@NonNull ImageDecoder decoder) {
        //noinspection ConstantConditions
        if (decoder != null) {
            this.decoder = decoder;
            SLog.w(NAME, "decoder=%s", decoder.toString());
        }
        return this;
    }

    /**
     * 获取图片下载器
     *
     * @return {@link ImageDownloader}. 图片下载器
     */
    @NonNull
    public ImageDownloader getDownloader() {
        return downloader;
    }

    /**
     * 设置图片下载器
     *
     * @param downloader {@link ImageDownloader}. 图片下载器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setDownloader(@NonNull ImageDownloader downloader) {
        //noinspection ConstantConditions
        if (downloader != null) {
            this.downloader = downloader;
            SLog.w(NAME, "downloader=%s", downloader.toString());
        }
        return this;
    }

    /**
     * 获取图片方向纠正器
     *
     * @return {@link ImageOrientationCorrector}. 图片方向纠正器
     */
    @NonNull
    public ImageOrientationCorrector getOrientationCorrector() {
        return orientationCorrector;
    }

    /**
     * 设置图片方向纠正器
     *
     * @param orientationCorrector {@link ImageOrientationCorrector}. 图片方向纠正器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setOrientationCorrector(@NonNull ImageOrientationCorrector orientationCorrector) {
        //noinspection ConstantConditions
        if (orientationCorrector != null) {
            this.orientationCorrector = orientationCorrector;
            SLog.w(NAME, "orientationCorrector=%s", orientationCorrector.toString());
        }
        return this;
    }


    /**
     * 获取默认的图片显示器
     *
     * @return {@link ImageDisplayer}. 默认的图片显示器
     */
    @NonNull
    public ImageDisplayer getDefaultDisplayer() {
        return defaultDisplayer;
    }

    /**
     * 设置默认的图片显示器
     *
     * @param defaultDisplayer {@link ImageDisplayer}. 默认的图片显示器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setDefaultDisplayer(@NonNull ImageDisplayer defaultDisplayer) {
        //noinspection ConstantConditions
        if (defaultDisplayer != null) {
            this.defaultDisplayer = defaultDisplayer;
            SLog.w(NAME, "defaultDisplayer=%s", defaultDisplayer.toString());
        }
        return this;
    }

    /**
     * 获取 {@link } 属性处理器
     *
     * @return {@link ImageProcessor}. {@link } 属性处理器
     */
    @NonNull
    public ImageProcessor getResizeProcessor() {
        return resizeProcessor;
    }

    /**
     * 设置 {@link } 属性处理器
     *
     * @param resizeProcessor {@link ImageProcessor}. {@link } 属性处理器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setResizeProcessor(@NonNull ImageProcessor resizeProcessor) {
        //noinspection ConstantConditions
        if (resizeProcessor != null) {
            this.resizeProcessor = resizeProcessor;
            SLog.w(NAME, "resizeProcessor=%s", resizeProcessor.toString());
        }
        return this;
    }

    /**
     * 获取 {@link } 计算器
     *
     * @return {@link ResizeCalculator}. {@link } 计算器
     */
    @NonNull
    public ResizeCalculator getResizeCalculator() {
        return resizeCalculator;
    }

    /**
     * 设置 {@link } 计算器
     *
     * @param resizeCalculator {@link ResizeCalculator}. {@link } 计算器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setResizeCalculator(@NonNull ResizeCalculator resizeCalculator) {
        //noinspection ConstantConditions
        if (resizeCalculator != null) {
            this.resizeCalculator = resizeCalculator;
            SLog.w(NAME, "resizeCalculator=%s", resizeCalculator.toString());
        }
        return this;
    }

    /**
     * 获取和图片尺寸相关的需求的计算器
     *
     * @return {@link ImageSizeCalculator}. 和图片尺寸相关的需求的计算器
     */
    @NonNull
    public ImageSizeCalculator getSizeCalculator() {
        return sizeCalculator;
    }

    /**
     * 设置和图片尺寸相关的需求的计算器
     *
     * @param sizeCalculator {@link ImageSizeCalculator}. 和图片尺寸相关的需求的计算器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setSizeCalculator(@NonNull ImageSizeCalculator sizeCalculator) {
        //noinspection ConstantConditions
        if (sizeCalculator != null) {
            this.sizeCalculator = sizeCalculator;
            SLog.w(NAME, "sizeCalculator=%s", sizeCalculator.toString());
        }
        return this;
    }


    /**
     * 获取顺风车管理器
     *
     * @return {@link FreeRideManager}. 顺风车管理器
     */
    @NonNull
    public FreeRideManager getFreeRideManager() {
        return freeRideManager;
    }

    /**
     * 设置顺风车管理器
     *
     * @param freeRideManager {@link FreeRideManager}. 顺风车管理器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setFreeRideManager(@NonNull FreeRideManager freeRideManager) {
        //noinspection ConstantConditions
        if (freeRideManager != null) {
            this.freeRideManager = freeRideManager;
            SLog.w(NAME, "freeRideManager=%s", freeRideManager.toString());
        }
        return this;
    }

    /**
     * 获取请求执行器
     *
     * @return {@link RequestExecutor}. 请求执行器
     */
    @NonNull
    public RequestExecutor getExecutor() {
        return executor;
    }

    /**
     * 设置请求执行器
     *
     * @param newRequestExecutor {@link RequestExecutor}. 请求执行器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setExecutor(@NonNull RequestExecutor newRequestExecutor) {
        //noinspection ConstantConditions
        if (newRequestExecutor != null) {
            RequestExecutor oldRequestExecutor = executor;
            executor = newRequestExecutor;
            oldRequestExecutor.shutdown();
            SLog.w(NAME, "executor=%s", executor.toString());
        }
        return this;
    }

    /**
     * 获取协助器创建工厂
     *
     * @return {@link HelperFactory}. 协助器创建工厂
     */
    @NonNull
    public HelperFactory getHelperFactory() {
        return helperFactory;
    }

    /**
     * 设置协助器创建工厂
     *
     * @param helperFactory {@link HelperFactory}. 协助器创建工厂
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setHelperFactory(@NonNull HelperFactory helperFactory) {
        //noinspection ConstantConditions
        if (helperFactory != null) {
            this.helperFactory = helperFactory;
            SLog.w(NAME, "helperFactory=%s", helperFactory.toString());
        }
        return this;
    }

    /**
     * 获取请求创建工厂
     *
     * @return {@link RequestFactory}. 请求创建工厂
     */
    @NonNull
    public RequestFactory getRequestFactory() {
        return requestFactory;
    }

    /**
     * 设置请求创建工厂
     *
     * @param requestFactory {@link RequestFactory}. 请求创建工厂
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setRequestFactory(@NonNull RequestFactory requestFactory) {
        //noinspection ConstantConditions
        if (requestFactory != null) {
            this.requestFactory = requestFactory;
            SLog.w(NAME, "requestFactory=%s", requestFactory.toString());
        }
        return this;
    }

    /**
     * 获取错误跟踪器
     *
     * @return {@link ErrorTracker}. 错误跟踪器
     */
    @NonNull
    public ErrorTracker getErrorTracker() {
        return errorTracker;
    }

    /**
     * 设置错误跟踪器
     *
     * @param errorTracker {@link ErrorTracker}. 错误跟踪器
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setErrorTracker(@NonNull ErrorTracker errorTracker) {
        //noinspection ConstantConditions
        if (errorTracker != null) {
            this.errorTracker = errorTracker;
            SLog.w(NAME, "errorTracker=%s", errorTracker.toString());
        }
        return this;
    }


    /**
     * 全局暂停下载新图片？
     */
    public boolean isPauseDownloadEnabled() {
        return optionsFilterManager.isPauseDownloadEnabled();
    }

    /**
     * 设置全局暂停下载新图片，开启后将不再从网络下载图片，只影响 {@link Sketch#display(String, SketchView)} 方法
     *
     * @param pauseDownloadEnabled 全局暂停下载新图片
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setPauseDownloadEnabled(boolean pauseDownloadEnabled) {
        if (optionsFilterManager.isPauseDownloadEnabled() != pauseDownloadEnabled) {
            optionsFilterManager.setPauseDownloadEnabled(pauseDownloadEnabled);
            SLog.w(NAME, "pauseDownload=%s", pauseDownloadEnabled);
        }
        return this;
    }

    /**
     * 全局暂停加载新图片？
     */
    public boolean isPauseLoadEnabled() {
        return optionsFilterManager.isPauseLoadEnabled();
    }

    /**
     * 设置全局暂停加载新图片，开启后将只从内存缓存中找寻图片，只影响 {@link Sketch#display(String, SketchView)} 方法
     *
     * @param pauseLoadEnabled 全局暂停加载新图片
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setPauseLoadEnabled(boolean pauseLoadEnabled) {
        if (optionsFilterManager.isPauseLoadEnabled() != pauseLoadEnabled) {
            optionsFilterManager.setPauseLoadEnabled(pauseLoadEnabled);
            SLog.w(NAME, "pauseLoad=%s", pauseLoadEnabled);
        }
        return this;
    }

    /**
     * 全局使用低质量的图片？
     */
    public boolean isLowQualityImageEnabled() {
        return optionsFilterManager.isLowQualityImageEnabled();
    }

    /**
     * 设置全局使用低质量图片
     *
     * @param lowQualityImageEnabled 全局使用低质量图片
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setLowQualityImageEnabled(boolean lowQualityImageEnabled) {
        if (optionsFilterManager.isLowQualityImageEnabled() != lowQualityImageEnabled) {
            optionsFilterManager.setLowQualityImageEnabled(lowQualityImageEnabled);
            SLog.w(NAME, "lowQualityImage=%s", lowQualityImageEnabled);
        }
        return this;
    }

    /**
     * 全局解码时优先考虑速度还是质量 (默认优先考虑速度)
     *
     * @return true：质量优先；false：速度优先
     */
    public boolean isInPreferQualityOverSpeedEnabled() {
        return optionsFilterManager.isInPreferQualityOverSpeedEnabled();
    }

    /**
     * 全局解码时优先考虑速度还是质量 (默认优先考虑速度)
     *
     * @param inPreferQualityOverSpeedEnabled true：质量优先；false：速度优先
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setInPreferQualityOverSpeedEnabled(boolean inPreferQualityOverSpeedEnabled) {
        if (optionsFilterManager.isInPreferQualityOverSpeedEnabled() != inPreferQualityOverSpeedEnabled) {
            optionsFilterManager.setInPreferQualityOverSpeedEnabled(inPreferQualityOverSpeedEnabled);
            SLog.w(NAME, "inPreferQualityOverSpeed=%s", inPreferQualityOverSpeedEnabled);
        }
        return this;
    }

    /**
     * 全局移动数据下暂停下载？
     */
    public boolean isMobileDataPauseDownloadEnabled() {
        return optionsFilterManager.isMobileDataPauseDownloadEnabled();
    }

    /**
     * 设置全局移动数据下暂停下载，只影响 {@link Sketch#display(String, SketchView)} 方法
     *
     * @param mobileDataPauseDownloadEnabled 全局移动数据下暂停下载
     * @return {@link Configuration}. 为了支持链式调用
     */
    @NonNull
    public Configuration setMobileDataPauseDownloadEnabled(boolean mobileDataPauseDownloadEnabled) {
        if (isMobileDataPauseDownloadEnabled() != mobileDataPauseDownloadEnabled) {
            optionsFilterManager.setMobileDataPauseDownloadEnabled(this, mobileDataPauseDownloadEnabled);
            SLog.w(NAME, "mobileDataPauseDownload=%s", isMobileDataPauseDownloadEnabled());
        }
        return this;
    }

    @NonNull
    public String toString() {
        return NAME + ": " +
                "\n" + "uriModelManager：" + uriModelManager.toString() +
                "\n" + "optionsFilterManager：" + optionsFilterManager.toString() +

                "\n" + "diskCache：" + diskCache.toString() +
                "\n" + "bitmapPool：" + bitmapPool.toString() +
                "\n" + "memoryCache：" + memoryCache.toString() +
                "\n" + "processedImageCache：" + processedImageCache.toString() +

                "\n" + "httpStack：" + httpStack.toString() +
                "\n" + "decoder：" + decoder.toString() +
                "\n" + "downloader：" + downloader.toString() +
                "\n" + "orientationCorrector：" + orientationCorrector.toString() +

                "\n" + "defaultDisplayer：" + defaultDisplayer.toString() +
                "\n" + "resizeProcessor：" + resizeProcessor.toString() +
                "\n" + "resizeCalculator：" + resizeCalculator.toString() +
                "\n" + "sizeCalculator：" + sizeCalculator.toString() +

                "\n" + "freeRideManager：" + freeRideManager.toString() +
                "\n" + "executor：" + executor.toString() +
                "\n" + "helperFactory：" + helperFactory.toString() +
                "\n" + "requestFactory：" + requestFactory.toString() +
                "\n" + "errorTracker：" + errorTracker.toString() +

                "\n" + "pauseDownload：" + optionsFilterManager.isPauseDownloadEnabled() +
                "\n" + "pauseLoad：" + optionsFilterManager.isPauseLoadEnabled() +
                "\n" + "lowQualityImage：" + optionsFilterManager.isLowQualityImageEnabled() +
                "\n" + "inPreferQualityOverSpeed：" + optionsFilterManager.isInPreferQualityOverSpeedEnabled() +
                "\n" + "mobileDataPauseDownload：" + isMobileDataPauseDownloadEnabled();
    }

    private static class MemoryChangedListener implements ComponentCallbacks2 {
        @NonNull
        private Context context;

        private MemoryChangedListener(@NonNull Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        public void onTrimMemory(int level) {
            Sketch.with(context).onTrimMemory(level);
        }

        @Override
        public void onConfigurationChanged(android.content.res.Configuration newConfig) {

        }

        @Override
        public void onLowMemory() {
            Sketch.with(context).onLowMemory();
        }
    }
}
