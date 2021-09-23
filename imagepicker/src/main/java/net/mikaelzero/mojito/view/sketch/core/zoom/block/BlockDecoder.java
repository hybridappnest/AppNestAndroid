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

package net.mikaelzero.mojito.view.sketch.core.zoom.block;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.SLog;
import net.mikaelzero.mojito.view.sketch.core.util.KeyCounter;
import net.mikaelzero.mojito.view.sketch.core.zoom.BlockDisplayer;

/**
 * 碎片解码器
 */
public class BlockDecoder {

    private static final String NAME = "BlockDecoder";

    @NonNull
    private KeyCounter initKeyCounter;
    @Nullable
    private ImageRegionDecoder decoder;
    @NonNull
    private BlockDisplayer blockDisplayer;
    private boolean running;
    private boolean initializing;

    public BlockDecoder(@NonNull BlockDisplayer blockDisplayer) {
        this.blockDisplayer = blockDisplayer;
        this.initKeyCounter = new KeyCounter();
    }

    /**
     * 设置新的图片
     */
    public void setImage(@Nullable String imageUri, boolean correctImageOrientationDisabled) {
        clean("setImage");

        if (decoder != null) {
            decoder.recycle();
            decoder = null;
        }

        if (!TextUtils.isEmpty(imageUri)) {
            running = initializing = true;
            blockDisplayer.getBlockExecutor().submitInit(imageUri, initKeyCounter, correctImageOrientationDisabled);
        } else {
            running = initializing = false;
        }
    }

    /**
     * 解码
     */
    void decodeBlock(@NonNull Block block) {
        if (!isReady()) {
            SLog.w(NAME, "not ready. decodeBlock. %s", block.getInfo());
            return;
        }

        block.decoder = decoder;
        blockDisplayer.getBlockExecutor().submitDecodeBlock(block.getKey(), block);
    }

    void clean(@NonNull String why) {
        if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
            SLog.d(NAME, "clean. %s", why);
        }

        initKeyCounter.refresh();
    }

    public void recycle(@NonNull String why) {
        if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
            SLog.d(NAME, "recycle. %s", why);
        }

        if (decoder != null) {
            decoder.recycle();
        }
    }

    public void initCompleted(@NonNull String imageUri, @NonNull ImageRegionDecoder decoder) {
        if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
            SLog.d(NAME, "init completed. %s", imageUri);
        }

        initializing = false;
        this.decoder = decoder;
    }

    public void initError(@NonNull String imageUri, @NonNull Exception e) {
        if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
            SLog.d(NAME, "init failed. %s. %s", e.getMessage(), imageUri);
        }

        initializing = false;
    }

    public boolean isReady() {
        return running && decoder != null && decoder.isReady();
    }

    public boolean isInitializing() {
        return running && initializing;
    }

    @Nullable
    public ImageRegionDecoder getDecoder() {
        return decoder;
    }
}
