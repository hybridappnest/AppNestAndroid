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

package net.mikaelzero.mojito.view.sketch.core.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 负责发送 HTTP 请求，并返回响应
 */
public interface HttpStack {
    int DEFAULT_READ_TIMEOUT = 7 * 1000;   // 默认读取超时时间
    int DEFAULT_CONNECT_TIMEOUT = 7 * 1000;    // 默认连接超时时间
    int DEFAULT_MAX_RETRY_COUNT = 0;    // 默认最大重试次数

    /**
     * 获取最大重试次数，默认值是 {@link HttpStack#DEFAULT_MAX_RETRY_COUNT}
     */
    int getMaxRetryCount();

    /**
     * 设置最大重试次数
     *
     * @param maxRetryCount 最大重试次数
     */
    @NonNull
    HttpStack setMaxRetryCount(int maxRetryCount);

    /**
     * 获取连接超时时间，单位毫秒，默认值是 {@link HttpStack#DEFAULT_CONNECT_TIMEOUT}
     */
    int getConnectTimeout();

    /**
     * 设置连接超时时间
     *
     * @param connectTimeout 连接超时时间，单位毫秒
     */
    @NonNull
    HttpStack setConnectTimeout(int connectTimeout);

    /**
     * 获取读取超时时间，单位毫秒，默认值是 {@link HttpStack#DEFAULT_READ_TIMEOUT}
     */
    int getReadTimeout();

    /**
     * 设置读取超时时间
     *
     * @param readTimeout 读取超时时间，单位毫秒
     */
    @NonNull
    HttpStack setReadTimeout(int readTimeout);

    /**
     * 获取自定义请求头中的 User-Agent 属性
     */
    @Nullable
    String getUserAgent();

    /**
     * 设置请求头中的 User-Agent 属性
     *
     * @param userAgent 请求头中的 User-Agent 属性
     */
    @NonNull
    HttpStack setUserAgent(String userAgent);

    /**
     * 获取扩展请求属性
     */
    @Nullable
    Map<String, String> getExtraHeaders();

    /**
     * 设置扩展请求属性集
     *
     * @param extraHeaders 扩展请求属性集
     */
    @NonNull
    HttpStack setExtraHeaders(Map<String, String> extraHeaders);

    /**
     * 获取可存在多个的请求属性
     */
    @Nullable
    Map<String, String> getAddExtraHeaders();

    /**
     * 添加可存在多个的请求属性
     *
     * @param extraHeaders 扩展请求属性集
     */
    @NonNull
    HttpStack addExtraHeaders(Map<String, String> extraHeaders);

    /**
     * 发送请求并获取响应
     *
     * @param uri http uri
     * @return {@link Response}
     */
    @NonNull
    Response getResponse(String uri) throws IOException;

    /**
     * 是否可以重试
     */
    boolean canRetry(@NonNull Throwable throwable);

    /**
     * 统一响应接口
     */
    interface Response {
        /**
         * 获取响应状态码
         *
         * @throws IOException IO
         */
        int getCode() throws IOException;

        /**
         * 获取响应消息
         *
         * @throws IOException IO
         */
        @Nullable
        String getMessage() throws IOException;

        /**
         * 获取内容长度
         */
        long getContentLength();

        /**
         * 获取内容类型
         */
        @Nullable
        String getContentType();

        /**
         * 内容是否是分块的？
         */
        boolean isContentChunked();

        /**
         * 获取内容编码
         */
        @Nullable
        String getContentEncoding();

        /**
         * 获取响应头
         */
        @Nullable
        String getHeaderField(@NonNull String name);

        /**
         * 获取响应头并转换成 int
         */
        int getHeaderFieldInt(@NonNull String name, int defaultValue);

        /**
         * 获取响应头并转换成 long
         */
        long getHeaderFieldLong(@NonNull String name, long defaultValue);

        /**
         * 获取所有的响应头
         */
        @Nullable
        String getHeadersString();

        /**
         * 获取内容输入流
         *
         * @throws IOException IO
         */
        @NonNull
        InputStream getContent() throws IOException;

        /**
         * 释放连接
         */
        void releaseConnection();
    }
}
