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

package net.mikaelzero.mojito.view.sketch.core.drawable;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.request.ImageFrom;

public interface SketchDrawable {
    /**
     * 获取图片 ID
     */
    @Nullable
    String getKey();

    /**
     * 获取图片 uri
     */
    @Nullable
    String getUri();

    /**
     * 获取图片原始宽
     */
    int getOriginWidth();

    /**
     * 获取图片原始高
     */
    int getOriginHeight();

    /**
     * 获取图片类型
     */
    @Nullable
    String getMimeType();

    /**
     * 获取图片方向
     */
    int getExifOrientation();

    /**
     * 获取占用内存，单位字节
     */
    int getByteCount();

    /**
     * 获取 {@link Bitmap} 配置
     */
    @Nullable
    Bitmap.Config getBitmapConfig();

    /**
     * 获取图片来源
     */
    @Nullable
    ImageFrom getImageFrom();

    /**
     * 获取一些信息
     */
    @Nullable
    String getInfo();
}
