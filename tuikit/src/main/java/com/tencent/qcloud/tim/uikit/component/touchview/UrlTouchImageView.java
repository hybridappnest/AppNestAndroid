/*
 Copyright (c) 2012 Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.tencent.qcloud.tim.uikit.component.touchview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tencent.qcloud.tim.uikit.component.picture.imageEngine.impl.GlideEngine;


public class UrlTouchImageView extends RelativeLayout {
    protected PhotoView mImageView;


    protected Context mContext;

    public UrlTouchImageView(Context ctx) {
        super(ctx);
        mContext = ctx;
        init();

    }

    public UrlTouchImageView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        mContext = ctx;
        init();
    }
    public UrlTouchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }
    public PhotoView getImageView() {
        return mImageView;
    }

    protected void init() {
        mImageView = new PhotoView(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mImageView.setLayoutParams(params);
        this.addView(mImageView);
    }

    public void setBgUrl(String url) {
        if (mImageView != null) {
            setUrl(url);
        }
    }
    public void setOnPhotoTapListener(PhotoViewAttacher.OnPhotoTapListener listener){
        mImageView.setOnPhotoTapListener(listener);
    }
    public void setOnViewTapListener(PhotoViewAttacher.OnViewTapListener listener){
        mImageView.setOnViewTapListener(listener);
    }
    private void setUrl(String imageUrl) {
        /**
         * 控制仅在wifi下加载图片
         */
        GlideEngine.loadImage(mImageView, imageUrl, new RequestListener() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                try {
                    BitmapDrawable bitmap = (BitmapDrawable) resource;
                    hegith = bitmap.getBitmap().getHeight();
                    width = bitmap.getBitmap().getWidth();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });
    }
    int hegith = -1;
    public int  getImageHeight(){
        return hegith;
    }
    int width = -1;
    public int  getImageWidth(){
        return width;
    }
    protected boolean isOverMaxSize(int width, int height, int[] desSize) {
        desSize[0] = Math.min(2048, width);
        desSize[1] = Math.min(2048, height);
        return width > 2048 || height > 2048;
    }

    /**
     * 判断是否开启硬件加速
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected boolean isHardwareAccelate() {
        return VERSION.SDK_INT > 10 && mImageView.isHardwareAccelerated();
    }

    public void recyle() {
        mImageView.recycle();
    }

}
