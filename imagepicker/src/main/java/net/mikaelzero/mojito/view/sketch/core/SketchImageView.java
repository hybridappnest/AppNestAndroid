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

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.request.DisplayCache;
import net.mikaelzero.mojito.view.sketch.core.request.DisplayRequest;
import net.mikaelzero.mojito.view.sketch.core.request.RedisplayListener;
import net.mikaelzero.mojito.view.sketch.core.viewfun.FunctionPropertyView;


/**
 * 用来替代 ImageView，支持手势缩放和分块显示超大图，详细文档请参考 docs/wiki/sketch_image_view.md
 */
public class SketchImageView extends FunctionPropertyView {

    public SketchImageView(@NonNull Context context) {
        super(context);
    }

    public SketchImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SketchImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Nullable
    @Override
    public DisplayRequest displayImage(@Nullable String uri) {
        return Sketch.with(getContext()).display(uri, this).commit();
    }

    @Nullable
    @Override
    public DisplayRequest displayResourceImage(@DrawableRes int drawableResId) {
        return Sketch.with(getContext()).displayFromResource(drawableResId, this).commit();
    }

    @Nullable
    @Override
    public DisplayRequest displayAssetImage(@NonNull String assetFileName) {
        return Sketch.with(getContext()).displayFromAsset(assetFileName, this).commit();
    }

    @Nullable
    @Override
    public DisplayRequest displayContentImage(@NonNull String uri) {
        return Sketch.with(getContext()).displayFromContent(uri, this).commit();
    }

    @Override
    public boolean redisplay(@Nullable RedisplayListener listener) {
        DisplayCache displayCache = getDisplayCache();
        if (displayCache == null || displayCache.uri == null) {
            return false;
        }

        if (listener != null) {
            listener.onPreCommit(displayCache.uri, displayCache.options);
        }
        Sketch.with(getContext())
                .display(displayCache.uri, this)
                .options(displayCache.options)
                .commit();
        return true;
    }

    /**
     * 获取选项 KEY，可用于组装缓存 KEY
     */
    @NonNull
    public String getOptionsKey() {
        DisplayCache displayCache = getDisplayCache();
        if (displayCache != null) {
            return displayCache.options.makeKey();
        } else {
            return getOptions().makeKey();
        }
    }
}
