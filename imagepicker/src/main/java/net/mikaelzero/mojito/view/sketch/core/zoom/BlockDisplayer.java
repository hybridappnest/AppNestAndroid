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

package net.mikaelzero.mojito.view.sketch.core.zoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.SLog;
import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPoolUtils;
import net.mikaelzero.mojito.view.sketch.core.decode.ImageType;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchLoadingDrawable;
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;
import net.mikaelzero.mojito.view.sketch.core.viewfun.FunctionPropertyView;
import net.mikaelzero.mojito.view.sketch.core.zoom.block.Block;
import net.mikaelzero.mojito.view.sketch.core.zoom.block.BlockDecoder;
import net.mikaelzero.mojito.view.sketch.core.zoom.block.BlockExecutor;
import net.mikaelzero.mojito.view.sketch.core.zoom.block.BlockManager;
import net.mikaelzero.mojito.view.sketch.core.zoom.block.DecodeHandler;
import net.mikaelzero.mojito.view.sketch.core.zoom.block.ImageRegionDecoder;

import java.util.List;

/**
 * ?????????????????????????????????????????????
 */
// TODO: 2017/5/8 ????????????????????????????????????????????????????????????????????????????????????????????????????????????
@SuppressWarnings("WeakerAccess")
public class BlockDisplayer {
    private static final String NAME = "BlockDisplayer";

    @NonNull
    private Context context;
    @NonNull
    private ImageZoomer imageZoomer;

    private Matrix tempDrawMatrix;
    private Rect tempVisibleRect;

    @NonNull
    private BlockExecutor blockExecutor;
    @NonNull
    private BlockDecoder blockDecoder;
    @NonNull
    private BlockManager blockManager;

    private float zoomScale;
    private float lastZoomScale;
    @NonNull
    private Paint drawBlockPaint;
    @Nullable
    private Paint drawBlockRectPaint;
    @Nullable
    private Paint drawLoadingBlockRectPaint;
    @NonNull
    private Matrix matrix;

    private boolean running;
    private boolean paused;
    @Nullable
    private String imageUri;

    private boolean showBlockBounds;
    @Nullable
    private OnBlockChangedListener onBlockChangedListener;

    public BlockDisplayer(@NonNull Context context, @NonNull ImageZoomer imageZoomer) {
        this.context = context.getApplicationContext();
        this.imageZoomer = imageZoomer;

        this.blockExecutor = new BlockExecutor(new ExecutorCallback());
        this.blockManager = new BlockManager(context, this);
        this.blockDecoder = new BlockDecoder(this);

        this.matrix = new Matrix();
        this.drawBlockPaint = new Paint();
    }


    /* -----------????????????----------- */


    public void reset() {
        ImageView imageView = imageZoomer.getImageView();

        Drawable previewDrawable = SketchUtils.getLastDrawable(imageZoomer.getImageView().getDrawable());
        SketchDrawable sketchDrawable = null;
        boolean drawableQualified = false;
        if (previewDrawable instanceof SketchDrawable && !(previewDrawable instanceof SketchLoadingDrawable)) {
            sketchDrawable = (SketchDrawable) previewDrawable;
            final int previewWidth = previewDrawable.getIntrinsicWidth();
            final int previewHeight = previewDrawable.getIntrinsicHeight();
            final int imageWidth = sketchDrawable.getOriginWidth();
            final int imageHeight = sketchDrawable.getOriginHeight();

            drawableQualified = previewWidth < imageWidth || previewHeight < imageHeight;
            drawableQualified &= SketchUtils.formatSupportBitmapRegionDecoder(ImageType.valueOfMimeType(sketchDrawable.getMimeType()));

            if (drawableQualified) {
                if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
                    SLog.d(NAME, "Use BlockDisplayer. previewDrawableSize: %dx%d, imageSize: %dx%d, mimeType: %s. %s",
                            previewWidth, previewHeight, imageWidth, imageHeight, sketchDrawable.getMimeType(), sketchDrawable.getKey());
                }
            } else {
                if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
                    SLog.d(NAME, "Don't need to use BlockDisplayer. previewDrawableSize: %dx%d, imageSize: %dx%d, mimeType: %s. %s",
                            previewWidth, previewHeight, imageWidth, imageHeight, sketchDrawable.getMimeType(), sketchDrawable.getKey());
                }
            }
        }

        boolean correctImageOrientationDisabled = !(imageView instanceof FunctionPropertyView)
                || ((FunctionPropertyView) imageView).getOptions().isCorrectImageOrientationDisabled();
        if (drawableQualified) {
            clean("setImage");

            this.imageUri = sketchDrawable.getUri();
            this.running = !TextUtils.isEmpty(imageUri);
            this.blockDecoder.setImage(imageUri, correctImageOrientationDisabled);
        } else {
            clean("setImage");

            this.imageUri = null;
            this.running = false;
            this.blockDecoder.setImage(null, correctImageOrientationDisabled);
        }
    }

    /**
     * ?????????????????????????????????????????? {@link #reset()} ????????????
     */
    public void recycle(@NonNull String why) {
        running = false;
        clean(why);
        blockExecutor.recycle(why);
        blockManager.recycle(why);
        blockDecoder.recycle(why);
    }

    /**
     * ????????????????????????????????????
     */
    private void clean(@NonNull String why) {
        blockExecutor.cleanDecode(why);

        matrix.reset();
        lastZoomScale = 0;
        zoomScale = 0;

        blockManager.clean(why);

        invalidateView();
    }


    /* -----------????????????----------- */


    public void onDraw(Canvas canvas) {
        if (blockManager.blockList.size() > 0) {
            int saveCount = canvas.save();
            canvas.concat(matrix);

            for (Block block : blockManager.blockList) {
                if (!block.isEmpty() && block.bitmap != null) {
                    canvas.drawBitmap(block.bitmap, block.bitmapDrawSrcRect, block.drawRect, drawBlockPaint);
                    if (showBlockBounds) {
                        if (drawBlockRectPaint == null) {
                            drawBlockRectPaint = new Paint();
                            drawBlockRectPaint.setColor(Color.parseColor("#88FF0000"));
                        }
                        canvas.drawRect(block.drawRect, drawBlockRectPaint);
                    }
                } else if (!block.isDecodeParamEmpty()) {
                    if (showBlockBounds) {
                        if (drawLoadingBlockRectPaint == null) {
                            drawLoadingBlockRectPaint = new Paint();
                            drawLoadingBlockRectPaint.setColor(Color.parseColor("#880000FF"));
                        }
                        canvas.drawRect(block.drawRect, drawLoadingBlockRectPaint);
                    }
                }
            }

            canvas.restoreToCount(saveCount);
        }
    }

    public void onMatrixChanged() {
        if (!isReady() && !isInitializing()) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
                SLog.d(NAME, "BlockDisplayer not available. onMatrixChanged. %s", imageUri);
            }
            return;
        }

        if (imageZoomer.getRotateDegrees() % 90 != 0) {
            SLog.w(NAME, "rotate degrees must be in multiples of 90. %s", imageUri);
            return;
        }

        if (tempDrawMatrix == null) {
            tempDrawMatrix = new Matrix();
            tempVisibleRect = new Rect();
        }

        tempDrawMatrix.reset();
        tempVisibleRect.setEmpty();

        imageZoomer.getDrawMatrix(tempDrawMatrix);
        imageZoomer.getVisibleRect(tempVisibleRect);

        Matrix drawMatrix = tempDrawMatrix;
        Rect newVisibleRect = tempVisibleRect;
        Size drawableSize = imageZoomer.getDrawableSize();
        Size viewSize = imageZoomer.getViewSize();
        boolean zooming = imageZoomer.isZooming();

        // ?????????????????????????????????
        if (!isReady()) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
                SLog.d(NAME, "not ready. %s", imageUri);
            }
            return;
        }

        // ?????????????????????
        if (paused) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
                SLog.d(NAME, "paused. %s", imageUri);
            }
            return;
        }

        // ????????????????????????????????????????????????
        if (newVisibleRect.isEmpty() || drawableSize.isEmpty() || viewSize.isEmpty()) {
            SLog.w(NAME, "update params is empty. update. newVisibleRect=%s, drawableSize=%s, viewSize=%s. %s",
                    newVisibleRect.toShortString(), drawableSize.toString(), viewSize.toString(), imageUri);
            clean("update param is empty");
            return;
        }

        // ??????????????????????????????????????????????????????????????????
        if (newVisibleRect.width() == drawableSize.getWidth() && newVisibleRect.height() == drawableSize.getHeight()) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
                SLog.d(NAME, "full display. update. newVisibleRect=%s. %s",
                        newVisibleRect.toShortString(), imageUri);
            }
            clean("full display");
            return;
        }

        // ??????Matrix
        lastZoomScale = zoomScale;
        matrix.set(drawMatrix);
        zoomScale = SketchUtils.formatFloat(SketchUtils.getMatrixScale(matrix), 2);

        invalidateView();

        blockManager.update(newVisibleRect, drawableSize, viewSize, getImageSize(), zooming);
    }


    /* -----------????????????----------- */


    public void invalidateView() {
        imageZoomer.getImageView().invalidate();
    }

    @NonNull
    public BlockDecoder getBlockDecoder() {
        return blockDecoder;
    }

    @NonNull
    public BlockExecutor getBlockExecutor() {
        return blockExecutor;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     */
    public void setPause(boolean pause) {
        if (pause == paused) {
            return;
        }
        paused = pause;

        if (paused) {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
                SLog.d(NAME, "pause. %s", imageUri);
            }

            if (running) {
                clean("pause");
            }
        } else {
            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_ZOOM_BLOCK_DISPLAY)) {
                SLog.d(NAME, "resume. %s", imageUri);
            }

            if (running) {
                onMatrixChanged();
            }
        }
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * ????????????
     */
    public boolean isWorking() {
        return !TextUtils.isEmpty(imageUri);
    }

    /**
     * ???????????????
     */
    public boolean isReady() {
        return running && blockDecoder.isReady();
    }

    /**
     * ???????????????
     */
    public boolean isInitializing() {
        return running && blockDecoder.isInitializing();
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????
     */
    public boolean isShowBlockBounds() {
        return showBlockBounds;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     */
    public void setShowBlockBounds(boolean showBlockBounds) {
        this.showBlockBounds = showBlockBounds;
        invalidateView();
    }

    /**
     * ????????????????????????
     */
    public float getZoomScale() {
        return zoomScale;
    }

    /**
     * ???????????????????????????
     */
    public float getLastZoomScale() {
        return lastZoomScale;
    }

    /**
     * ?????????????????????
     */
    public Point getImageSize() {
        return blockDecoder.isReady() ? blockDecoder.getDecoder().getImageSize() : null;
    }

    /**
     * ?????????????????????
     */
    public ImageType getImageType() {
        return blockDecoder.isReady() ? blockDecoder.getDecoder().getImageType() : null;
    }

    /**
     * ????????????URI
     */
    @Nullable
    public String getImageUri() {
        return imageUri;
    }

    /**
     * ??????????????????
     */
    public Rect getDrawRect() {
        return blockManager.drawRect;
    }

    /**
     * ?????????????????????????????????????????????
     */
    public Rect getDrawSrcRect() {
        return blockManager.drawSrcRect;
    }

    /**
     * ??????????????????
     */
    public Rect getDecodeRect() {
        return blockManager.decodeRect;
    }

    /**
     * ?????????????????????????????????????????????
     */
    public Rect getDecodeSrcRect() {
        return blockManager.decodeSrcRect;
    }

    /**
     * ??????????????????
     */
    public List<Block> getBlockList() {
        return blockManager.blockList;
    }

    /**
     * ??????????????????
     */
    public int getBlockSize() {
        return blockManager.blockList.size();
    }

    /**
     * ??????????????????????????????????????????
     */
    public long getAllocationByteCount() {
        return blockManager.getAllocationByteCount();
    }

    /**
     * ??????????????????????????????????????????3??????????????????????????????????????? (3+1)x(3+1)=16 ?????????
     */
    public int getBlockBaseNumber() {
        return blockManager.blockBaseNumber;
    }

    /**
     * ???????????????????????????
     */
    @Nullable
    public OnBlockChangedListener getOnBlockChangedListener() {
        return onBlockChangedListener;
    }

    /**
     * ???????????????????????????
     */
    public void setOnBlockChangedListener(@Nullable OnBlockChangedListener onBlockChangedListener) {
        this.onBlockChangedListener = onBlockChangedListener;
    }

    @Nullable
    public Block getBlockByDrawablePoint(int drawableX, int drawableY) {
        for (Block block : blockManager.blockList) {
            if(block.drawRect.contains(drawableX, drawableY)){
                return block;
            }
        }
        return null;
    }

    @Nullable
    public Block getBlockByImagePoint(int imageX, int imageY) {
        for (Block block : blockManager.blockList) {
            if(block.srcRect.contains(imageX, imageY)){
                return block;
            }
        }
        return null;
    }

    public interface OnBlockChangedListener {
        void onBlockChanged(@NonNull BlockDisplayer blockDisplayer);
    }

    private class ExecutorCallback implements BlockExecutor.Callback {

        @NonNull
        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public void onInitCompleted(@NonNull String imageUri, @NonNull ImageRegionDecoder decoder) {
            if (!running) {
                SLog.w(NAME, "stop running. initCompleted. %s", imageUri);
                return;
            }

            blockDecoder.initCompleted(imageUri, decoder);

            onMatrixChanged();
        }

        @Override
        public void onInitError(@NonNull String imageUri, @NonNull Exception e) {
            if (!running) {
                SLog.w(NAME, "stop running. initError. %s", imageUri);
                return;
            }

            blockDecoder.initError(imageUri, e);
        }

        @Override
        public void onDecodeCompleted(@NonNull Block block, @NonNull Bitmap bitmap, int useTime) {
            if (!running) {
                SLog.w(NAME, "stop running. decodeCompleted. block=%s", block.getInfo());
                BitmapPoolUtils.freeBitmapToPoolForRegionDecoder(bitmap, Sketch.with(context).getConfiguration().getBitmapPool());
                return;
            }

            blockManager.decodeCompleted(block, bitmap, useTime);
        }

        @Override
        public void onDecodeError(@NonNull Block block, @NonNull DecodeHandler.DecodeErrorException exception) {
            if (!running) {
                SLog.w(NAME, "stop running. decodeError. block=%s", block.getInfo());
                return;
            }

            blockManager.decodeError(block, exception);
        }
    }
}
