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

package net.mikaelzero.mojito.view.sketch.core.datasource;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPool;
import net.mikaelzero.mojito.view.sketch.core.decode.ImageAttrs;
import net.mikaelzero.mojito.view.sketch.core.decode.NotFoundGifLibraryException;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchGifDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchGifFactory;
import net.mikaelzero.mojito.view.sketch.core.request.ImageFrom;
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * 用于读取来自 drawable 资源的图片
 */
public class DrawableDataSource implements DataSource {
    @NonNull
    private Context context;
    private int drawableId;
    private long length = -1;

    public DrawableDataSource(@NonNull Context context, int drawableId) {
        this.context = context;
        this.drawableId = drawableId;
    }

    @NonNull
    @Override
    public InputStream getInputStream() throws IOException {
        return context.getResources().openRawResource(drawableId);
    }

    @Override
    public long getLength() throws IOException {
        if (length >= 0) {
            return length;
        }

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getResources().openRawResourceFd(drawableId);
            length = fileDescriptor != null ? fileDescriptor.getLength() : 0;
        } finally {
            SketchUtils.close(fileDescriptor);
        }
        return length;
    }

    @Override
    public File getFile(@Nullable File outDir, @Nullable String outName) throws IOException {
        if (outDir == null) {
            return null;
        }

        if (!outDir.exists() && !outDir.getParentFile().mkdirs()) {
            return null;
        }

        File outFile;
        if (!TextUtils.isEmpty(outName)) {
            outFile = new File(outDir, outName);
        } else {
            outFile = new File(outDir, SketchUtils.generatorTempFileName(this, String.valueOf(drawableId)));
        }

        InputStream inputStream = getInputStream();

        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outFile);
        } catch (IOException e) {
            SketchUtils.close(inputStream);
            throw e;
        }

        byte[] data = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, length);
            }
        } finally {
            SketchUtils.close(outputStream);
            SketchUtils.close(inputStream);
        }

        return outFile;
    }

    @NonNull
    @Override
    public ImageFrom getImageFrom() {
        return ImageFrom.LOCAL;
    }

    @NonNull
    @Override
    public SketchGifDrawable makeGifDrawable(@NonNull String key, @NonNull String uri, @NonNull ImageAttrs imageAttrs,
                                             @NonNull BitmapPool bitmapPool) throws IOException, NotFoundGifLibraryException {
        Resources resources = context.getResources();
        return SketchGifFactory.createGifDrawable(key, uri, imageAttrs, getImageFrom(), bitmapPool, resources, drawableId);
    }
}
