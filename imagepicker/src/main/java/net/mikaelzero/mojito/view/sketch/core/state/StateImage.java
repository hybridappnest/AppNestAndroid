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

package net.mikaelzero.mojito.view.sketch.core.state;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.SketchView;
import net.mikaelzero.mojito.view.sketch.core.request.DisplayOptions;

/**
 * 用于为加载中、失败、暂停状态提供占位图片
 */
public interface StateImage {

    /**
     * 获取图片
     *
     * @param context        {@link Context}
     * @param sketchView     {@link SketchView}
     * @param displayOptions {@link DisplayOptions}
     * @return {@link Drawable}
     */
    @Nullable
    Drawable getDrawable(@NonNull Context context, @NonNull SketchView sketchView, @NonNull DisplayOptions displayOptions);
}
