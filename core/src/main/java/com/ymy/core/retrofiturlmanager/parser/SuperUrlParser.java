/*
 * Copyright 2018 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ymy.core.retrofiturlmanager.parser;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import com.ymy.core.retrofiturlmanager.RetrofitUrlManager;
import com.ymy.core.retrofiturlmanager.cache.Cache;
import com.ymy.core.retrofiturlmanager.cache.LruCache;
import okhttp3.HttpUrl;

import static com.ymy.core.retrofiturlmanager.RetrofitUrlManager.IDENTIFICATION_PATH_SIZE;

/**
 * ================================================
 * 超级解析器
 * 超级模式属于高级模式的加强版, 优先级高于高级模式, 在高级模式中, 需要传入一个 BaseUrl (您传入 Retrofit 的 BaseUrl) 作为被替换的基准
 * 如这个传入的 BaseUrl 为 "https://www.github.com/wiki/part" (PathSize = 2), 那框架会将所有需要被替换的 Url 中的 域名 以及 域名 后面的前两个 pathSegments
 * 使用您传入 {@link RetrofitUrlManager#putDomain(String, String)} 方法的 Url 替换掉
 * 但如果突然有一小部分的 Url 只想将 "https://www.github.com/wiki" (PathSize = 1) 替换掉, 后面的 pathSegment '/part' 想被保留下来
 * 这时项目中就出现了多个 PathSize 不同的需要被替换的 BaseUrl
 * <p>
 * 使用高级模式实现这种需求略显麻烦, 所以我创建了超级模式, 让每一个 Url 都可以随意指定不同的 BaseUrl (PathSize 自己定) 作为被替换的基准
 * 使 RetrofitUrlManager 可以从容应对各种复杂的需求
 * <p>
 * 超级模式也需要手动开启, 但与高级模式不同的是, 开启超级模式并不需要调用 API, 只需要在 Url 中加入 {@link RetrofitUrlManager#IDENTIFICATION_PATH_SIZE} + PathSize
 * <p>
 * 替换规则如下:
 * 1.
 * 旧 URL 地址为 https://www.github.com/wiki/part#baseurl_path_size=1, #baseurl_path_size=1 表示其中 BaseUrl 为 https://www.github.com/wiki
 * 您调用 {@link RetrofitUrlManager#putDomain(String, String)}方法传入的 URL 地址是 https://www.google.com/api
 * 经过本解析器解析后生成的新 URL 地址为 http://www.google.com/api/part
 * <p>
 * 2.
 * 旧 URL 地址为 https://www.github.com/wiki/part#baseurl_path_size=1, #baseurl_path_size=1 表示其中 BaseUrl 为 https://www.github.com/wiki
 * 您调用 {@link RetrofitUrlManager#putDomain(String, String)}方法传入的 URL 地址是 https://www.google.com
 * 经过本解析器解析后生成的新 URL 地址为 http://www.google.com/part
 * <p>
 * 3.
 * 旧 URL 地址为 https://www.github.com/wiki/part#baseurl_path_size=0, #baseurl_path_size=0 表示其中 BaseUrl 为 https://www.github.com
 * 您调用 {@link RetrofitUrlManager#putDomain(String, String)}方法传入的 URL 地址是 https://www.google.com/api
 * 经过本解析器解析后生成的新 URL 地址为 http://www.google.com/api/wiki/part
 * <p>
 * 4.
 * 旧 URL 地址为 https://www.github.com/wiki/part/issues/1#baseurl_path_size=3, #baseurl_path_size=3 表示其中 BaseUrl 为 https://www.github.com/wiki/part/issues
 * 您调用 {@link RetrofitUrlManager#putDomain(String, String)}方法传入的 URL 地址是 https://www.google.com/api
 * 经过本解析器解析后生成的新 URL 地址为 http://www.google.com/api/1
 *
 * @see UrlParser
 * Created by JessYan on 2018/6/21 16:41
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class SuperUrlParser implements UrlParser {
    private RetrofitUrlManager mRetrofitUrlManager;
    private Cache<String, String> mCache;

    @Override
    public void init(RetrofitUrlManager retrofitUrlManager) {
        this.mRetrofitUrlManager = retrofitUrlManager;
        this.mCache = new LruCache<>(100);
    }

    @Override
    public HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl url) {
        if (null == domainUrl) return url;

        HttpUrl.Builder builder = url.newBuilder();

        int pathSize = resolvePathSize(url, builder);

        if (TextUtils.isEmpty(mCache.get(getKey(domainUrl, url, pathSize)))) {
            for (int i = 0; i < url.pathSize(); i++) {
                //当删除了上一个 index, PathSegment 的 item 会自动前进一位, 所以 remove(0) 就好
                builder.removePathSegment(0);
            }

            List<String> newPathSegments = new ArrayList<>();
            newPathSegments.addAll(domainUrl.encodedPathSegments());


            if (url.pathSize() > pathSize) {
                List<String> encodedPathSegments = url.encodedPathSegments();
                for (int i = pathSize; i < encodedPathSegments.size(); i++) {
                    newPathSegments.add(encodedPathSegments.get(i));
                }
            } else if (url.pathSize() < pathSize) {
                throw new IllegalArgumentException(String.format(
                        "Your final path is %s, the pathSize = %d, but the #baseurl_path_size = %d, #baseurl_path_size must be less than or equal to pathSize of the final path",
                        url.scheme() + "://" + url.host() + url.encodedPath(), url.pathSize(), pathSize));
            }

            for (String PathSegment : newPathSegments) {
                builder.addEncodedPathSegment(PathSegment);
            }
        } else {
            builder.encodedPath(mCache.get(getKey(domainUrl, url, pathSize)));
        }

        HttpUrl httpUrl = builder
                .scheme(domainUrl.scheme())
                .host(domainUrl.host())
                .port(domainUrl.port())
                .build();

        if (TextUtils.isEmpty(mCache.get(getKey(domainUrl, url, pathSize)))) {
            mCache.put(getKey(domainUrl, url, pathSize), httpUrl.encodedPath());
        }
        return httpUrl;
    }

    private String getKey(HttpUrl domainUrl, HttpUrl url, int PathSize) {
        return domainUrl.encodedPath() + url.encodedPath()
                + PathSize;
    }

    private int resolvePathSize(HttpUrl httpUrl, HttpUrl.Builder builder) {
        String fragment = httpUrl.fragment();

        int pathSize = 0;
        StringBuffer newFragment = new StringBuffer();

        if (fragment.indexOf("#") == -1) {
            String[] split = fragment.split("=");
            if (split.length > 1) {
                pathSize = Integer.parseInt(split[1]);
            }
        } else {
            if (fragment.indexOf(IDENTIFICATION_PATH_SIZE) == -1) {
                int index = fragment.indexOf("#");
                newFragment.append(fragment.substring(index + 1, fragment.length()));
                String[] split = fragment.substring(0, index).split("=");
                if (split.length > 1) {
                    pathSize = Integer.parseInt(split[1]);
                }
            } else {
                String[] split = fragment.split(IDENTIFICATION_PATH_SIZE);
                newFragment.append(split[0]);
                if (split.length > 1) {
                    int index = split[1].indexOf("#");
                    if (index != -1) {
                        newFragment.append(split[1].substring(index, split[1].length()));
                        String substring = split[1].substring(0, index);
                        if (!TextUtils.isEmpty(substring)) {
                            pathSize = Integer.parseInt(substring);
                        }
                    } else {
                        pathSize = Integer.parseInt(split[1]);
                    }
                }
            }
        }
        if (TextUtils.isEmpty(newFragment.toString())) {
            builder.fragment(null);
        } else {
            builder.fragment(newFragment.toString());
        }
        return pathSize;
    }
}
