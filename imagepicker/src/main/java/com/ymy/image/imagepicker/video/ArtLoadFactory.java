package com.ymy.image.imagepicker.video;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.lcw.library.imagepicker.manager.ConfigManager;

import net.mikaelzero.mojito.interfaces.ImageViewLoadFactory;
import net.mikaelzero.mojito.loader.ContentLoader;

import org.jetbrains.annotations.NotNull;


public class ArtLoadFactory implements ImageViewLoadFactory {


    @Override
    public void loadSillContent(@NotNull View view, @NotNull Uri uri) {
        if (view instanceof ImageView) {
            try {
                ConfigManager.getInstance().getImageLoader().loadImage((ImageView) view, uri.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @NotNull
    @Override
    public ContentLoader newContentLoader() {
        return new TXPlayerLoadImpl();
    }

    @Override
    public void loadContentFail(@NotNull View view, int drawableResId) {

    }
}
