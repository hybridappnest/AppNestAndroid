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

import net.mikaelzero.mojito.view.sketch.core.ErrorTracker;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPoolUtils;
import net.mikaelzero.mojito.view.sketch.core.process.ImageProcessor;
import net.mikaelzero.mojito.view.sketch.core.request.BaseRequest;
import net.mikaelzero.mojito.view.sketch.core.request.LoadOptions;
import net.mikaelzero.mojito.view.sketch.core.request.LoadRequest;

public class ProcessImageResultProcessor implements ResultProcessor {

    @Override
    public void process(@NonNull LoadRequest request, @NonNull DecodeResult result) throws ProcessException {
        if (result.isBanProcess()) {
            return;
        }

        if (!(result instanceof BitmapDecodeResult)) {
            return;
        }

        BitmapDecodeResult bitmapDecodeResult = (BitmapDecodeResult) result;
        Bitmap bitmap = bitmapDecodeResult.getBitmap();
        if (bitmap == null) {
            return;
        }

        LoadOptions loadOptions = request.getOptions();
        ImageProcessor imageProcessor = loadOptions.getProcessor();
        if (imageProcessor == null) {
            return;
        }

        request.setStatus(BaseRequest.Status.PROCESSING);

        Bitmap newBitmap = null;
        try {
            newBitmap = imageProcessor.process(request.getSketch(), bitmap, loadOptions.getResize(), loadOptions.isLowQualityImage());
        } catch (Throwable e) {
            e.printStackTrace();
            ErrorTracker errorTracker = request.getConfiguration().getErrorTracker();
            errorTracker.onProcessImageError(e, request.getKey(), imageProcessor);
        }

        if (newBitmap != null && !newBitmap.isRecycled()) {
            if (newBitmap != bitmap) {
                BitmapPoolUtils.freeBitmapToPool(bitmap, request.getConfiguration().getBitmapPool());
                bitmapDecodeResult.setBitmap(newBitmap);
            }
            result.setProcessed(true);
        } else {
            throw new ProcessException("Process result bitmap null or recycled");
        }
    }
}
