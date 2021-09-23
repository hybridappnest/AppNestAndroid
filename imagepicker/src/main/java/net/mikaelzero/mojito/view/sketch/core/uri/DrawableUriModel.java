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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.SLog;
import net.mikaelzero.mojito.view.sketch.core.datasource.DataSource;
import net.mikaelzero.mojito.view.sketch.core.datasource.DrawableDataSource;
import net.mikaelzero.mojito.view.sketch.core.request.DownloadResult;

public class DrawableUriModel extends UriModel {

    public static final String SCHEME = "drawable://";
    private static final String NAME = "DrawableUriModel";

    @NonNull
    public static String makeUri(@DrawableRes int drawableResId) {
        return SCHEME + drawableResId;
    }

    @Override
    protected boolean match(@NonNull String uri) {
        return !TextUtils.isEmpty(uri) && uri.startsWith(SCHEME);
    }

    /**
     * 获取 uri 所真正包含的内容部分，例如 "drawable.icon://424214"，就会返回 "424214"
     *
     * @param uri 图片 uri
     * @return uri 所真正包含的内容部分，例如 "drawable.icon://424214"，就会返回 "424214"
     */
    @NonNull
    @Override
    public String getUriContent(@NonNull String uri) {
        return match(uri) ? uri.substring(SCHEME.length()) : uri;
    }

    @NonNull
    @Override
    public DataSource getDataSource(@NonNull Context context, @NonNull String uri, @Nullable DownloadResult downloadResult) throws GetDataSourceException {
        int resId;
        try {
            resId = Integer.valueOf(getUriContent(uri));
        } catch (NumberFormatException e) {
            String cause = String.format("Conversion resId failed. %s", uri);
            SLog.e(NAME, e, cause);
            throw new GetDataSourceException(cause, e);
        }
        return new DrawableDataSource(context, resId);
    }

    public int getResId(String uri) {
        return Integer.parseInt(getUriContent(uri));
    }
}
