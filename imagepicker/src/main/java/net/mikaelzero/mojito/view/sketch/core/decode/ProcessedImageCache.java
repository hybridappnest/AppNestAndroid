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

package net.mikaelzero.mojito.view.sketch.core.decode;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.cache.DiskCache;
import net.mikaelzero.mojito.view.sketch.core.datasource.DiskCacheDataSource;
import net.mikaelzero.mojito.view.sketch.core.request.ImageFrom;
import net.mikaelzero.mojito.view.sketch.core.request.LoadOptions;
import net.mikaelzero.mojito.view.sketch.core.request.LoadRequest;
import net.mikaelzero.mojito.view.sketch.core.util.DiskLruCache;
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 对读到内存后又再次处理过的图片进行缓存，下次就不用再处理了，可加快加载速度
 */
public class ProcessedImageCache {

    /**
     * 判断是否可以使用此功能
     */
    public boolean canUse(@NonNull LoadOptions loadOptions) {
        if (!loadOptions.isCacheProcessedImageInDisk()) {
            return false;
        }

        if (loadOptions.getMaxSize() != null || loadOptions.getResize() != null) {
            return true;
        }

        if (loadOptions.getProcessor() != null) {
            return true;
        }

        if (loadOptions.isThumbnailMode() && loadOptions.getResize() != null) {
            return true;
        }

        //noinspection RedundantIfStatement
        if (!loadOptions.isCorrectImageOrientationDisabled()) {
            return true;
        }

        return false;
    }

    /**
     * 此缩放比例是否可以使用缓存到本地磁盘功能
     */
    public boolean canUseCacheProcessedImageInDisk(int inSampleSize) {
        return inSampleSize >= 8;
    }

    public boolean checkDiskCache(@NonNull LoadRequest request) {
        DiskCache diskCache = request.getConfiguration().getDiskCache();
        String processedImageDiskCacheKey = request.getProcessedDiskCacheKey();
        String diskCacheKey = request.getDiskCacheKey();
        if (diskCacheKey.equals(processedImageDiskCacheKey)) {
            return false;
        }

        ReentrantLock editLock = diskCache.getEditLock(processedImageDiskCacheKey);
        editLock.lock();

        try {
            return diskCache.exist(processedImageDiskCacheKey);
        } finally {
            editLock.unlock();
        }
    }

    /**
     * 开启了缓存已处理图片功能，如果磁盘缓存中已经有了缓存就直接读取
     */
    @Nullable
    public DiskCacheDataSource getDiskCache(@NonNull LoadRequest request) {
        DiskCache diskCache = request.getConfiguration().getDiskCache();
        String processedImageDiskCacheKey = request.getProcessedDiskCacheKey();
        String diskCacheKey = request.getDiskCacheKey();
        if (diskCacheKey.equals(processedImageDiskCacheKey)) {
            return null;
        }

        ReentrantLock editLock = diskCache.getEditLock(processedImageDiskCacheKey);
        editLock.lock();

        DiskCache.Entry diskCacheEntry;
        try {
            diskCacheEntry = diskCache.get(processedImageDiskCacheKey);
        } finally {
            editLock.unlock();
        }

        if (diskCacheEntry == null) {
            return null;
        }

        return new DiskCacheDataSource(diskCacheEntry, ImageFrom.DISK_CACHE).setFromProcessedCache(true);
    }

    /**
     * 保存 {@link Bitmap} 到磁盘缓存
     */
    public void saveToDiskCache(@NonNull LoadRequest request, @NonNull Bitmap bitmap) {
        DiskCache diskCache = request.getConfiguration().getDiskCache();
        String processedImageDiskCacheKey = request.getProcessedDiskCacheKey();
        String diskCacheKey = request.getDiskCacheKey();
        if (diskCacheKey.equals(processedImageDiskCacheKey)) {
            return;
        }

        ReentrantLock editLock = diskCache.getEditLock(processedImageDiskCacheKey);
        editLock.lock();

        try {
            DiskCache.Entry diskCacheEntry = diskCache.get(processedImageDiskCacheKey);

            if (diskCacheEntry != null) {
                diskCacheEntry.delete();
            }

            DiskCache.Editor diskCacheEditor = diskCache.edit(processedImageDiskCacheKey);
            if (diskCacheEditor != null) {
                BufferedOutputStream outputStream = null;
                //noinspection TryWithIdenticalCatches
                try {
                    outputStream = new BufferedOutputStream(diskCacheEditor.newOutputStream(), 8 * 1024);
                    bitmap.compress(SketchUtils.bitmapConfigToCompressFormat(bitmap.getConfig()), 100, outputStream);
                    diskCacheEditor.commit();
                } catch (DiskLruCache.EditorChangedException e) {
                    e.printStackTrace();
                    diskCacheEditor.abort();
                } catch (IOException e) {
                    e.printStackTrace();
                    diskCacheEditor.abort();
                } catch (DiskLruCache.ClosedException e) {
                    e.printStackTrace();
                    diskCacheEditor.abort();
                } catch (DiskLruCache.FileNotExistException e) {
                    e.printStackTrace();
                    diskCacheEditor.abort();
                } finally {
                    SketchUtils.close(outputStream);
                }
            }
        } finally {
            editLock.unlock();
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "ProcessedImageCache";
    }
}
