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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.SketchView;
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;

import java.lang.ref.WeakReference;


/**
 * Request与ImageView的关系绑定器
 */
@SuppressWarnings("WeakerAccess")
public class RequestAndViewBinder {
    @Nullable
    private DisplayRequest displayRequest;
    @NonNull
    private WeakReference<SketchView> imageViewReference;

    public RequestAndViewBinder(@NonNull SketchView imageView) {
        this.imageViewReference = new WeakReference<>(imageView);
    }

    public void setDisplayRequest(@Nullable DisplayRequest displayRequest) {
        this.displayRequest = displayRequest;
    }

    @Nullable
    public SketchView getView() {
        final SketchView sketchView = imageViewReference.get();
        if (displayRequest != null) {
            DisplayRequest holderDisplayRequest = SketchUtils.findDisplayRequest(sketchView);
            if (holderDisplayRequest != null && holderDisplayRequest == displayRequest) {
                return sketchView;
            } else {
                return null;
            }
        } else {
            return sketchView;
        }
    }

    public boolean isBroken() {
        return getView() == null;
    }
}
