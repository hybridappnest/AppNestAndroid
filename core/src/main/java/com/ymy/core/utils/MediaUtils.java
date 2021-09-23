package com.ymy.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import androidx.core.content.FileProvider;

public class MediaUtils {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    public static Uri getOutputMediaFileUri(Context context, int type)
    {
        Uri uri = null;
        //适配Android N
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", getOutputMediaFile(type));
        } else
        {
            return Uri.fromFile(getOutputMediaFile(type));
        }
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(int type)
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "image");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = System.currentTimeMillis()+"";
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else
        {
            return null;
        }
        return mediaFile;
    }

    /**
     * 获取视频的第一帧图片
     */
    public static void getImageForVideo(String videoPath, OnLoadVideoImageListener listener)
    {

        LoadVideoImageTask task = new LoadVideoImageTask(listener,videoPath);
        task.execute(videoPath);
    }

    public static class LoadVideoImageTask extends AsyncTask<String, Integer, File>
    {
        private OnLoadVideoImageListener listener;
        private  String width = "0";
        private  String height = "0";
        private String  videoPath;
        public LoadVideoImageTask(OnLoadVideoImageListener listener,String  videoPath)
        {
            Log.e("MediaUtils111","imagePath = "+videoPath);
            this.videoPath = videoPath;
            this.listener = listener;
        }

        @Override
        protected File doInBackground(String... params)
        {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            String path = params[0];
            if (path.startsWith("http"))
                //获取网络视频第一帧图片
                mmr.setDataSource(path, new HashMap());
            else
                //本地视频
                mmr.setDataSource(path);
            Bitmap bitmap = mmr.getFrameAtTime();
            width = bitmap.getWidth()+"";
            height=  bitmap.getHeight()+"";
//            bitmap = zoomImg(bitmap,width,height);
            //保存图片
            File f = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (f.exists())
            {
                f.delete();
            }
            try
            {
                FileOutputStream out = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (FileNotFoundException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            mmr.release();
            return f;
        }

        @Override
        protected void onPostExecute(File file)
        {
            super.onPostExecute(file);
            if (listener != null)
            {
                Log.e("MediaUtils222","imagePath = "+file.getPath()+"  videoPath = "+videoPath);
                listener.onLoadImage(file,videoPath,width,height);
            }
        }
    }

    /**
     *  处理图片
     * @param bm 所要转换的bitmap
     * @param newWidth 新的宽
     * @param newHeight 新的高
     * @return 指定宽高的bitmap
     */
    public static Bitmap zoomImg(Bitmap bm, String newWidth ,String newHeight) {
        try {
            // 获得图片的宽高
            int width = bm.getWidth();
            int height = bm.getHeight();
            // 计算缩放比例
            float scaleWidth = (Float.parseFloat(newWidth)) / width;
            float scaleHeight =(Float.parseFloat(newHeight)) / height;
            // 取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 得到新的图片   www.2cto.com
            Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
            return newbm;
        }catch (Exception e){
            e.printStackTrace();
        }
        return bm;
    }

        public interface OnLoadVideoImageListener
    {
        void onLoadImage(File file,String  videoPath,String width,String height);
    }
}
