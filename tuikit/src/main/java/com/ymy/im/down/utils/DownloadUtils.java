package com.ymy.im.down.utils;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.ymy.im.down.listener.DownloadListener;
import com.ymy.im.down.retrofit.RetrofitApi;
import com.ymy.im.down.retrofit.RetrofitRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 下载文件工具类
 */

public class DownloadUtils {

    private static final String TAG = "DownloadUtils";

    private Context mContext;
    // 视频下载相关
    private RetrofitApi mApi;
    private Handler mHandler;
    private File mFile;
    private Thread mThread;
    // 下载到本地的文件路径
    private String mFilePath;

    public DownloadUtils(Context context) {
        mContext = context;
        if (mApi == null) {
            mApi = RetrofitRequest.getInstance().getRetrofitApi();
        }
        mHandler = new Handler();
    }

    public void downloadFile(String url, String path,String name, final DownloadListener downloadListener) {
        //通过Url得到保存到本地的文件名
        File themeFolder = new File(path);
        if (!themeFolder.exists()) {
            themeFolder.mkdirs();
        }
        mFilePath = path;
        if (TextUtils.isEmpty(mFilePath)) {
            Log.e(TAG, "downloadVideo: 存储路径为空了");
            return;
        }
        //建立一个文件
        mFile = new File(path+name);
        if (mApi == null) {
            Log.e(TAG, "downloadVideo: 下载接口为空了");
            return;
        }
        Call<ResponseBody> call = mApi.downloadFile(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                //下载文件放在子线程
                mThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        //保存到本地
                        writeFile2Disk(response, mFile, downloadListener);
                    }
                };
                mThread.start();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                downloadListener.onFailure("网络错误！");
            }
        });
    }

    private void writeFile2Disk(Response<ResponseBody> response, File file, final DownloadListener downloadListener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                downloadListener.onStart();
            }
        });
        long currentLength = 0;
        OutputStream os = null;

        if (response.body() == null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    downloadListener.onFailure("资源错误！");
                }
            });
            return;
        }
        InputStream is = response.body().byteStream();
        final long totalLength = response.body().contentLength();

        try {
            os = new FileOutputStream(file);
            int len;
            byte[] buff = new byte[1024];
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
                currentLength += len;
                Log.e(TAG, "当前进度: " + currentLength);
                final long finalCurrentLength = currentLength;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadListener.onProgress((int) (100 * finalCurrentLength / totalLength));
                        if ((int) (100 * finalCurrentLength / totalLength) == 100) {
                            downloadListener.onFinish(mFilePath);
                        }
                    }
                });
            }
        } catch (FileNotFoundException e) {
            downloadListener.onFailure("未找到文件！");
            e.printStackTrace();
        } catch (IOException e) {
            downloadListener.onFailure("IO错误！");
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
