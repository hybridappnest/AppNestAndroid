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

package net.mikaelzero.mojito.view.sketch.core.uri;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import net.mikaelzero.mojito.view.sketch.core.datasource.DataSource;
import net.mikaelzero.mojito.view.sketch.core.datasource.FileDataSource;
import net.mikaelzero.mojito.view.sketch.core.request.DownloadResult;

import java.io.File;

public class FileVariantUriModel extends FileUriModel {

    public static final String SCHEME = "file://";

    @NonNull
    public static String makeUri(@NonNull String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Param filePath is null or empty");
        }
        return !filePath.startsWith(SCHEME) ? SCHEME + filePath : filePath;
    }

    @Override
    protected boolean match(@NonNull String uri) {
        return !TextUtils.isEmpty(uri) && uri.startsWith(SCHEME);
    }

    /**
     * 获取 uri 所真正包含的内容部分，例如 "file:///sdcard/test.png"，就会返回 "/sdcard/test.png"
     *
     * @param uri 图片 uri
     * @return uri 所真正包含的内容部分，例如 "file:///sdcard/test.png"，就会返回 "/sdcard/test.png"
     */
    @NonNull
    @Override
    public String getUriContent(@NonNull String uri) {
        return match(uri) ? uri.substring(SCHEME.length()) : uri;
    }

    @NonNull
    @Override
    public String getDiskCacheKey(@NonNull String uri) {
        return getUriContent(uri);
    }

    @NonNull
    @Override
    public DataSource getDataSource(@NonNull Context context, @NonNull String uri, DownloadResult downloadResult) {
        return new FileDataSource(new File(getUriContent(uri)));
    }
}
