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

package net.mikaelzero.mojito.view.sketch.core.request;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.ErrorTracker;
import net.mikaelzero.mojito.view.sketch.core.SLog;
import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.SketchView;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPool;
import net.mikaelzero.mojito.view.sketch.core.cache.MemoryCache;
import net.mikaelzero.mojito.view.sketch.core.display.ImageDisplayer;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchBitmapDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchGifDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchRefBitmap;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchRefDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchShapeBitmapDrawable;
import net.mikaelzero.mojito.view.sketch.core.state.StateImage;
import net.mikaelzero.mojito.view.sketch.core.uri.UriModel;
/**
 * 显示请求
 */
@SuppressWarnings("WeakerAccess")
public class DisplayRequest extends LoadRequest {

    @Nullable
    protected DisplayResult displayResult;
    @Nullable
    private DisplayListener displayListener;
    @NonNull
    private ViewInfo viewInfo;
    @NonNull
    private RequestAndViewBinder requestAndViewBinder;

    public DisplayRequest(@NonNull Sketch sketch, @NonNull String uri, @NonNull UriModel uriModel, @NonNull String key, @NonNull DisplayOptions displayOptions,
                          @NonNull ViewInfo viewInfo, @NonNull RequestAndViewBinder requestAndViewBinder, @Nullable DisplayListener displayListener,
                          @Nullable DownloadProgressListener downloadProgressListener) {
        super(sketch, uri, uriModel, key, displayOptions, null, downloadProgressListener);

        this.viewInfo = viewInfo;
        this.requestAndViewBinder = requestAndViewBinder;
        this.displayListener = displayListener;

        this.requestAndViewBinder.setDisplayRequest(this);
        setLogName("DisplayRequest");
    }

    /**
     * 获取显示选项
     */
    @NonNull
    @Override
    public DisplayOptions getOptions() {
        return (DisplayOptions) super.getOptions();
    }

    /**
     * 获取内存缓存 key
     */
    @NonNull
    public String getMemoryCacheKey() {
        return getKey();
    }

    /**
     * 获取 View 信息
     */
    @NonNull
    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    @Override
    public boolean isCanceled() {
        if (super.isCanceled()) {
            return true;
        }

        // 绑定关系已经断了就直接取消请求
        if (requestAndViewBinder.isBroken()) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG)) {
                SLog.d(getLogName(), "The request and the connection to the view are interrupted. %s. %s", getThreadName(), getKey());
            }
            doCancel(CancelCause.BIND_DISCONNECT);
            return true;
        }

        return false;
    }

    @Override
    protected void doError(@NonNull ErrorCause errorCause) {
        if (displayListener != null || getOptions().getErrorImage() != null) {
            setErrorCause(errorCause);
            postRunError();
        } else {
            super.doError(errorCause);
        }
    }

    @Override
    protected void doCancel(@NonNull CancelCause cancelCause) {
        super.doCancel(cancelCause);

        if (displayListener != null) {
            postRunCanceled();
        }
    }

    @Override
    protected void postRunError() {
        setStatus(Status.WAIT_DISPLAY);
        super.postRunError();
    }

    @Override
    protected void postRunCompleted() {
        setStatus(Status.WAIT_DISPLAY);
        super.postRunCompleted();
    }

    @Override
    protected void runLoad() {
        if (isCanceled()) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_FLOW)) {
                SLog.d(getLogName(), "Request end before decode. %s. %s", getThreadName(), getKey());
            }
            return;
        }

        // Check memory cache
        DisplayOptions displayOptions = getOptions();
        if (!displayOptions.isCacheInDiskDisabled()) {
            setStatus(Status.CHECK_MEMORY_CACHE);
            MemoryCache memoryCache = getConfiguration().getMemoryCache();
            SketchRefBitmap cachedRefBitmap = memoryCache.get(getMemoryCacheKey());
            if (cachedRefBitmap != null) {
                // 当 isDecodeGifImage 为 true 时是要播放 gif 的，而内存缓存里的 gif 图都是第一帧静态图片，所以不能用
                if (!(getOptions().isDecodeGifImage() && "image/gif".equalsIgnoreCase(cachedRefBitmap.getAttrs().getMimeType()))) {
                    if (!cachedRefBitmap.isRecycled()) {
                        if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_FLOW)) {
                            SLog.d(getLogName(), "From memory get drawable. bitmap=%s. %s. %s",
                                    cachedRefBitmap.getInfo(), getThreadName(), getKey());
                        }

                        // 立马标记等待使用，防止被回收
                        cachedRefBitmap.setIsWaitingUse(String.format("%s:waitingUse:fromMemory", getLogName()), true);

                        Drawable drawable = new SketchBitmapDrawable(cachedRefBitmap, ImageFrom.MEMORY_CACHE);
                        displayResult = new DisplayResult(drawable, ImageFrom.MEMORY_CACHE, cachedRefBitmap.getAttrs());
                        displayCompleted();
                        return;
                    } else {
                        memoryCache.remove(getMemoryCacheKey());
                        SLog.e(getLogName(), "Memory cache drawable recycled. bitmap=%s. %s. %s", cachedRefBitmap.getInfo(), getThreadName(), getKey());
                    }
                }
            }
        }

        super.runLoad();
    }

    @Override
    protected void loadCompleted() {
        LoadResult loadResult = getLoadResult();
        DisplayOptions displayOptions = getOptions();
        if (loadResult != null && loadResult.getBitmap() != null) {
            Bitmap bitmap = loadResult.getBitmap();

            BitmapPool bitmapPool = getConfiguration().getBitmapPool();
            SketchRefBitmap refBitmap = new SketchRefBitmap(bitmap, getKey(), getUri(), loadResult.getImageAttrs(), bitmapPool);

            // 立马标记等待使用，防止刚放入内存缓存就被挤出去回收掉
            refBitmap.setIsWaitingUse(String.format("%s:waitingUse:new", getLogName()), true);

            // 放入内存缓存中
            if (!displayOptions.isCacheInMemoryDisabled() && getMemoryCacheKey() != null) {
                getConfiguration().getMemoryCache().put(getMemoryCacheKey(), refBitmap);
            }

            Drawable drawable = new SketchBitmapDrawable(refBitmap, loadResult.getImageFrom());
            displayResult = new DisplayResult(drawable, loadResult.getImageFrom(), loadResult.getImageAttrs());
            displayCompleted();
        } else if (loadResult != null && loadResult.getGifDrawable() != null) {
            SketchGifDrawable gifDrawable = loadResult.getGifDrawable();

            // GifDrawable不能放入内存缓存中，因为GifDrawable需要依赖Callback才能播放，
            // 如果缓存的话就会出现一个GifDrawable被显示在多个ImageView上的情况，这时候就只有最后一个能正常播放

            displayResult = new DisplayResult((Drawable) gifDrawable, loadResult.getImageFrom(), loadResult.getImageAttrs());
            displayCompleted();
        } else {
            SLog.e(getLogName(), "Not found data after load completed. %s. %s", getThreadName(), getKey());
            doError(ErrorCause.DATA_LOST_AFTER_LOAD_COMPLETED);
        }
    }

    protected void displayCompleted() {
        postRunCompleted();
    }

    @Override
    protected void runCompletedInMainThread() {
        Drawable drawable = displayResult.getDrawable();
        if (drawable == null) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_FLOW)) {
                SLog.d(getLogName(), "Drawable is null before call completed. %s. %s", getThreadName(), getKey());
            }
            return;
        }

        displayImage(drawable);

        // 使用完毕更新等待使用的引用计数
        if (drawable instanceof SketchRefDrawable) {
            ((SketchRefDrawable) drawable).setIsWaitingUse(String.format("%s:waitingUse:finish", getLogName()), false);
        }
    }

    private void displayImage(Drawable drawable) {
        SketchView sketchView = requestAndViewBinder.getView();
        if (isCanceled() || sketchView == null) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_FLOW)) {
                SLog.d(getLogName(), "Request end before call completed. %s. %s", getThreadName(), getKey());
            }
            return;
        }

        // 过滤可能已回收的图片
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap().isRecycled()) {
                // 这里应该不会再出问题了
                ErrorTracker errorTracker = getConfiguration().getErrorTracker();
                errorTracker.onBitmapRecycledOnDisplay(this, (SketchDrawable) drawable);

                // 图片不可用
                if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_FLOW)) {
                    SLog.d(getLogName(), "Display image exception. bitmap recycled. %s. %s. %s. %s",
                            ((SketchDrawable) drawable).getInfo(), displayResult.getImageFrom(), getThreadName(), getKey());
                }

                runErrorInMainThread();
                return;
            }
        }

        // 显示图片
        DisplayOptions displayOptions = getOptions();
        if ((displayOptions.getShapeSize() != null || displayOptions.getShaper() != null) && drawable instanceof BitmapDrawable) {
            drawable = new SketchShapeBitmapDrawable(getConfiguration().getContext(), (BitmapDrawable) drawable,
                    displayOptions.getShapeSize(), displayOptions.getShaper());
        }

        if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_FLOW)) {
            String drawableInfo = "unknown";
            if (drawable instanceof SketchRefDrawable) {
                drawableInfo = ((SketchRefDrawable) drawable).getInfo();
            }
            SLog.d(getLogName(), "Display image completed. %s. %s. view(%s). %s. %s",
                    displayResult.getImageFrom().name(), drawableInfo, Integer.toHexString(sketchView.hashCode()), getThreadName(), getKey());
        }

        // 一定要在 ImageDisplayer().display 之前执行
        setStatus(Status.COMPLETED);

        displayOptions.getDisplayer().display(sketchView, drawable);

        if (displayListener != null) {
            displayListener.onCompleted(displayResult.getDrawable(), displayResult.getImageFrom(), displayResult.getImageAttrs());
        }
    }

    @Override
    protected void runErrorInMainThread() {
        SketchView sketchView = requestAndViewBinder.getView();
        if (isCanceled() || sketchView == null) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_FLOW)) {
                SLog.d(getLogName(), "Request end before call error. %s. %s", getThreadName(), getKey());
            }
            return;
        }

        setStatus(Status.FAILED);

        DisplayOptions displayOptions = getOptions();
        ImageDisplayer displayer = displayOptions.getDisplayer();
        StateImage errorImage = displayOptions.getErrorImage();
        if (displayer != null && errorImage != null) {
            Drawable errorDrawable = errorImage.getDrawable(getContext(), sketchView, displayOptions);
            if (errorDrawable != null) {
                displayer.display(sketchView, errorDrawable);
            }
        }

        if (displayListener != null && getErrorCause() != null) {
            displayListener.onError(getErrorCause());
        }
    }

    @Override
    protected void runCanceledInMainThread() {
        if (displayListener != null && getCancelCause() != null) {
            displayListener.onCanceled(getCancelCause());
        }
    }
}