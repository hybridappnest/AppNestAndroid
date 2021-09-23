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

package net.mikaelzero.mojito.view.sketch.core.zoom.block;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import net.mikaelzero.mojito.view.sketch.core.SLog;
import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPool;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPoolUtils;
import net.mikaelzero.mojito.view.sketch.core.util.KeyCounter;

import java.lang.ref.WeakReference;


/**
 * 运行在主线程，负责将执行器的结果发送到主线程
 */
class CallbackHandler extends Handler {
    private static final String NAME = "CallbackHandler";

    private static final int WHAT_RECYCLE_DECODE_THREAD = 2001;
    private static final int WHAT_INIT_COMPLETED = 2002;
    private static final int WHAT_INIT_FAILED = 2003;
    private static final int WHAT_DECODE_COMPLETED = 2004;
    private static final int WHAT_DECODE_FAILED = 2005;

    @NonNull
    private BitmapPool bitmapPool;
    @NonNull
    private WeakReference<BlockExecutor> executorReference;

    CallbackHandler(@NonNull Looper looper, @NonNull BlockExecutor executor) {
        super(looper);
        executorReference = new WeakReference<>(executor);
        bitmapPool = Sketch.with(executor.callback.getContext()).getConfiguration().getBitmapPool();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_RECYCLE_DECODE_THREAD:
                recycleDecodeThread();
                break;
            case WHAT_INIT_COMPLETED:
                InitResult initResult = (InitResult) msg.obj;
                initCompleted(initResult.imageRegionDecoder, initResult.imageUrl, msg.arg1, initResult.keyCounter);
                break;
            case WHAT_INIT_FAILED:
                InitErrorResult initErrorResult = (InitErrorResult) msg.obj;
                initError(initErrorResult.exception, initErrorResult.imageUrl, msg.arg1, initErrorResult.keyCounter);
                break;
            case WHAT_DECODE_COMPLETED:
                DecodeResult decodeResult = (DecodeResult) msg.obj;
                decodeCompleted(msg.arg1, decodeResult.block, decodeResult.bitmap, decodeResult.useTime);
                break;
            case WHAT_DECODE_FAILED:
                DecodeErrorResult decodeErrorResult = (DecodeErrorResult) msg.obj;
                decodeError(msg.arg1, decodeErrorResult.block, decodeErrorResult.exception);
                break;
        }
    }


    /**
     * 延迟三十秒停止解码线程
     */
    void postDelayRecycleDecodeThread() {
        cancelDelayDestroyThread();

        Message destroyMessage = obtainMessage(CallbackHandler.WHAT_RECYCLE_DECODE_THREAD);
        sendMessageDelayed(destroyMessage, 30 * 1000);
    }

    private void recycleDecodeThread() {
        BlockExecutor executor = executorReference.get();
        if (executor != null) {
            executor.recycleDecodeThread();
        }
    }

    /**
     * 取消停止解码线程的延迟任务
     */
    void cancelDelayDestroyThread() {
        removeMessages(CallbackHandler.WHAT_RECYCLE_DECODE_THREAD);
    }


    void postInitCompleted(ImageRegionDecoder decoder, String imageUri, int initKey, KeyCounter keyCounter) {
        Message message = obtainMessage(CallbackHandler.WHAT_INIT_COMPLETED);
        message.arg1 = initKey;
        message.obj = new InitResult(decoder, imageUri, keyCounter);
        message.sendToTarget();
    }

    void postInitError(Exception e, String imageUri, int key, KeyCounter keyCounter) {
        Message message = obtainMessage(CallbackHandler.WHAT_INIT_FAILED);
        message.arg1 = key;
        message.obj = new InitErrorResult(e, imageUri, keyCounter);
        message.sendToTarget();
    }

    void postDecodeCompleted(int key, Block block, Bitmap bitmap, int useTime) {
        Message message = obtainMessage(CallbackHandler.WHAT_DECODE_COMPLETED);
        message.arg1 = key;
        message.obj = new DecodeResult(bitmap, block, useTime);
        message.sendToTarget();
    }

    void postDecodeError(int key, Block block, DecodeHandler.DecodeErrorException exception) {
        Message message = obtainMessage(CallbackHandler.WHAT_DECODE_FAILED);
        message.arg1 = key;
        message.obj = new DecodeErrorResult(block, exception);
        message.sendToTarget();
    }


    private void initCompleted(ImageRegionDecoder decoder, String imageUri, int key, KeyCounter keyCounter) {
        BlockExecutor executor = executorReference.get();
        if (executor == null) {
            SLog.w(NAME, "weak reference break. initCompleted. key: %d, imageUri: %s", key, decoder.getImageUri());
            decoder.recycle();
            return;
        }

        int newKey = keyCounter.getKey();
        if (key != newKey) {
            SLog.w(NAME, "init key expired. initCompleted. key: %d. newKey: %d, imageUri: %s", key, newKey, decoder.getImageUri());
            decoder.recycle();
            return;
        }

        executor.callback.onInitCompleted(imageUri, decoder);
    }

    private void initError(Exception exception, String imageUri, int key, KeyCounter keyCounter) {
        BlockExecutor executor = executorReference.get();
        if (executor == null) {
            SLog.w(NAME, "weak reference break. initError. key: %d, imageUri: %s", key, imageUri);
            return;
        }

        int newKey = keyCounter.getKey();
        if (key != newKey) {
            SLog.w(NAME, "key expire. initError. key: %d. newKey: %d, imageUri: %s", key, newKey, imageUri);
            return;
        }

        executor.callback.onInitError(imageUri, exception);
    }

    private void decodeCompleted(int key, Block block, Bitmap bitmap, int useTime) {
        BlockExecutor executor = executorReference.get();
        if (executor == null) {
            SLog.w(NAME, "weak reference break. decodeCompleted. key: %d, block=%s", key, block.getInfo());
            BitmapPoolUtils.freeBitmapToPoolForRegionDecoder(bitmap, bitmapPool);
            return;
        }

        if (!block.isExpired(key)) {
            executor.callback.onDecodeCompleted(block, bitmap, useTime);
        } else {
            BitmapPoolUtils.freeBitmapToPoolForRegionDecoder(bitmap, bitmapPool);
            executor.callback.onDecodeError(block,
                    new DecodeHandler.DecodeErrorException(DecodeHandler.DecodeErrorException.CAUSE_CALLBACK_KEY_EXPIRED));
        }
    }

    private void decodeError(int key, Block block, DecodeHandler.DecodeErrorException exception) {
        BlockExecutor executor = executorReference.get();
        if (executor == null) {
            SLog.w(NAME, "weak reference break. decodeError. key: %d, block=%s", key, block.getInfo());
            return;
        }

        executor.callback.onDecodeError(block, exception);
    }

    private static final class DecodeResult {
        @NonNull
        public Block block;
        @NonNull
        public Bitmap bitmap;
        int useTime;

        DecodeResult(@NonNull Bitmap bitmap, @NonNull Block block, int useTime) {
            this.bitmap = bitmap;
            this.block = block;
            this.useTime = useTime;
        }
    }

    private static final class DecodeErrorResult {
        @NonNull
        public Block block;
        @NonNull
        public DecodeHandler.DecodeErrorException exception;

        DecodeErrorResult(@NonNull Block block, @NonNull DecodeHandler.DecodeErrorException exception) {
            this.block = block;
            this.exception = exception;
        }
    }

    private static final class InitResult {
        @NonNull
        String imageUrl;
        @NonNull
        ImageRegionDecoder imageRegionDecoder;
        @NonNull
        KeyCounter keyCounter;

        InitResult(@NonNull ImageRegionDecoder imageRegionDecoder, @NonNull String imageUrl, @NonNull KeyCounter keyCounter) {
            this.imageRegionDecoder = imageRegionDecoder;
            this.imageUrl = imageUrl;
            this.keyCounter = keyCounter;
        }
    }

    private static final class InitErrorResult {
        @NonNull
        String imageUrl;
        @NonNull
        public Exception exception;
        @NonNull
        KeyCounter keyCounter;

        InitErrorResult(@NonNull Exception exception, @NonNull String imageUrl, @NonNull KeyCounter keyCounter) {
            this.exception = exception;
            this.imageUrl = imageUrl;
            this.keyCounter = keyCounter;
        }
    }
}
