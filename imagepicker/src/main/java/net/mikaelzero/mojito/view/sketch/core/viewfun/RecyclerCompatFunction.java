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

package net.mikaelzero.mojito.view.sketch.core.viewfun;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.SLog;
import net.mikaelzero.mojito.view.sketch.core.SketchView;
import net.mikaelzero.mojito.view.sketch.core.request.DisplayOptions;
import net.mikaelzero.mojito.view.sketch.core.request.RedisplayListener;
import net.mikaelzero.mojito.view.sketch.core.uri.UriModel;


/**
 * 由于 RecyclerView 在往回滚动的时候遇到可以直接使用的 Item（位置没有变）会不走 onBindViewHolder 而直接走 onAttachedToWindow 然后显示，
 * <br>可是 RequestFunction 在 onDetachedFromWindow 的时候会主动清空 Drawable 导致没有重新走 onBindViewHolder 的 Item 会没有 Drawable 而显示空白
 * <br>因此 RecyclerCompatFunction 就判断了如果在 onAttachedToWindow 之前没有调用相关显示图片的方法就会根据 DisplayCache 恢复之前的图片
 */
// todo 尝试不再主动清空图片
@SuppressWarnings("WeakerAccess")
public class RecyclerCompatFunction extends ViewFunction {
    private static final String NAME = "RecyclerCompatFunction";

    @NonNull
    private SketchView sketchView;

    private boolean isSetImage;
    @Nullable
    private RedisplayListener redisplayListener;

    public RecyclerCompatFunction(@NonNull SketchView sketchView) {
        this.sketchView = sketchView;
    }

    @Override
    public void onAttachedToWindow() {
        if (isSetImage) {
            return;
        }

        if (redisplayListener == null) {
            redisplayListener = new RecyclerRedisplayListener();
        }
        sketchView.redisplay(redisplayListener);
    }

    @Override
    public boolean onReadyDisplay(@Nullable UriModel uriModel) {
        isSetImage = true;
        return false;
    }

    @Override
    public boolean onDetachedFromWindow() {
        this.isSetImage = false;
        return false;
    }

    private static class RecyclerRedisplayListener implements RedisplayListener {

        @Override
        public void onPreCommit(@NonNull String cacheUri, @NonNull DisplayOptions cacheOptions) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_FLOW)) {
                SLog.d(NAME, "restore image on attached to window. %s", cacheUri);
            }
        }
    }
}
