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

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.SLog;
import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.cache.MemoryCache;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchBitmapDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchRefBitmap;
import net.mikaelzero.mojito.view.sketch.core.uri.UriModel;
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 支持显示顺风车的请求
 */
@SuppressWarnings("WeakerAccess")
public class FreeRideDisplayRequest extends DisplayRequest implements FreeRideManager.DisplayFreeRide {

    @Nullable
    private Set<FreeRideManager.DisplayFreeRide> displayFreeRideSet;

    public FreeRideDisplayRequest(@NonNull Sketch sketch, @NonNull String uri, @NonNull UriModel uriModel, @NonNull String key, @NonNull DisplayOptions displayOptions,
                                  @NonNull ViewInfo viewInfo, @NonNull RequestAndViewBinder requestAndViewBinder,
                                  @Nullable DisplayListener displayListener, @Nullable DownloadProgressListener downloadProgressListener) {
        super(sketch, uri, uriModel, key, displayOptions, viewInfo, requestAndViewBinder, displayListener, downloadProgressListener);
    }

    @NonNull
    @Override
    public String getDisplayFreeRideLog() {
        return String.format("%s@%s", SketchUtils.toHexString(this), getKey());
    }

    @NonNull
    @Override
    public String getDisplayFreeRideKey() {
        return getKey();
    }

    /**
     * 可以坐顺风车？条件是内存缓存 key 一样并且内存缓存可以用，没有单独关闭内存缓存，不解码 gif 图片，没有开同步执行，请求执行器可以用
     */
    @Override
    public boolean canByDisplayFreeRide() {
        MemoryCache memoryCache = getConfiguration().getMemoryCache();
        return !memoryCache.isClosed() && !memoryCache.isDisabled()
                && !getOptions().isCacheInMemoryDisabled()
                && !getOptions().isDecodeGifImage()
                && !isSync() && !getConfiguration().getExecutor().isShutdown();
    }

    @Override
    protected void submitRunLoad() {
        // 可以坐顺风车的话，就先尝试坐别人的，坐不上就自己成为顺风车主让别人坐
        if (canByDisplayFreeRide()) {
            FreeRideManager freeRideManager = getConfiguration().getFreeRideManager();
            if (freeRideManager.byDisplayFreeRide(this)) {
                return;
            } else {
                freeRideManager.registerDisplayFreeRideProvider(this);
            }
        }

        super.submitRunLoad();
    }

    @Override
    protected void runLoad() {
        super.runLoad();

        // 由于在submitRunLoad中会将自己注册成为顺风车主，因此一定要保证在这里取消注册
        if (canByDisplayFreeRide()) {
            FreeRideManager freeRideManager = getConfiguration().getFreeRideManager();
            freeRideManager.unregisterDisplayFreeRideProvider(this);
        }
    }

    @Override
    public synchronized void byDisplayFreeRide(FreeRideManager.DisplayFreeRide request) {
        if (displayFreeRideSet == null) {
            synchronized (this) {
                if (displayFreeRideSet == null) {
                    displayFreeRideSet = new HashSet<>();
                }
            }
        }

        displayFreeRideSet.add(request);
    }

    @Nullable
    @Override
    public Set<FreeRideManager.DisplayFreeRide> getDisplayFreeRideSet() {
        return displayFreeRideSet;
    }

    @Override
    public synchronized boolean processDisplayFreeRide() {
        if (!getOptions().isCacheInDiskDisabled()) {
            MemoryCache memoryCache = getConfiguration().getMemoryCache();
            SketchRefBitmap cachedRefBitmap = memoryCache.get(getMemoryCacheKey());
            if (cachedRefBitmap != null && cachedRefBitmap.isRecycled()) {
                memoryCache.remove(getMemoryCacheKey());
                SLog.e(getLogName(), "memory cache drawable recycled. processFreeRideRequests. bitmap=%s. %s. %s",
                        cachedRefBitmap.getInfo(), getThreadName(), getKey());
                cachedRefBitmap = null;
            }

            if (cachedRefBitmap != null) {
                // 当 isDecodeGifImage 为 true 时是要播放 gif 的，而内存缓存里的 gif 图都是第一帧静态图片，所以不能用
                if (!(getOptions().isDecodeGifImage() && "image/gif".equalsIgnoreCase(cachedRefBitmap.getAttrs().getMimeType()))) {
                    // 立马标记等待使用，防止被回收
                    cachedRefBitmap.setIsWaitingUse(String.format("%s:waitingUse:fromMemory", getLogName()), true);

                    Drawable drawable = new SketchBitmapDrawable(cachedRefBitmap, ImageFrom.MEMORY_CACHE);
                    displayResult = new DisplayResult(drawable, ImageFrom.MEMORY_CACHE, cachedRefBitmap.getAttrs());
                    displayCompleted();
                    return true;
                }
            }
        }

        submitRunLoad();
        return false;
    }
}