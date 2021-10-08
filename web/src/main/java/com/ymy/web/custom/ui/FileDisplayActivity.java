package com.ymy.web.custom.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsReaderView;
import com.tencent.smtt.sdk.TbsReaderView.ReaderCallback;
import com.tencent.smtt.sdk.ValueCallback;
import com.ymy.web.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.HashMap;

public class FileDisplayActivity extends Activity implements ReaderCallback,
        OnClickListener {
    private TextView tv_title;
    private TbsReaderView mTbsReaderView;
    private TextView tv_download;
    //rl_tbsView为装载TbsReaderView的视图
    private RelativeLayout rl_tbsView;
    private ProgressBar progressBar_download;
    private DownloadManager mDownloadManager;
    private long mRequestId;
    private DownloadObserver mDownloadObserver;
    //文件url 由文件url截取的文件名 上个页面传过来用于显示的文件名
    private String mFileUrl = "", mFileName, fileName;

    /**
     * 跳转页面
     *
     * @param context
     * @param fileUrl  文件url
     * @param fileName 文件名
     */
    public static void actionStart(Context context, String fileUrl, String fileName) {
        Intent intent = new Intent(context, FileDisplayActivity.class);
        intent.putExtra("fileUrl", fileUrl);
        intent.putExtra("fileName", fileName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_file_display);
        findViewById();
        getFileUrlByIntent();
        mTbsReaderView = new TbsReaderView(this, this);
        rl_tbsView.addView(mTbsReaderView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        if ((mFileUrl == null) || (mFileUrl.length() <= 0)) {
            Toast.makeText(FileDisplayActivity.this, "获取文件url出错了",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mFileName = parseName(mFileUrl);
        if (isLocalExist()) {
            tv_download.setText("打开文件");
            tv_download.setVisibility(View.GONE);
            displayFile();
        } else {
            if (!mFileUrl.contains("http")) {
                new AlertDialog.Builder(FileDisplayActivity.this)
                        .setTitle("温馨提示:")
                        .setMessage("文件的url地址不合法哟，无法进行下载")
                        .setCancelable(false)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                return;
                            }
                        }).create().show();
            }
            startDownload();
        }
    }

    /**
     * 将url进行encode，解决部分手机无法下载含有中文url的文件的问题（如OPPO R9）
     *
     * @param url
     * @return
     * @author xch
     */
    private String toUtf8String(String url) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = String.valueOf(c).getBytes("utf-8");
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0) {
                        k += 256;
                    }
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

    private void findViewById() {
        tv_download = (TextView) findViewById(R.id.tv_download);
        ImageView back_icon = (ImageView) findViewById(R.id.btn_back);
        tv_title = (TextView) findViewById(R.id.tv_titlebar_title);
        progressBar_download = (ProgressBar) findViewById(R.id.progressBar_download);
        rl_tbsView = (RelativeLayout) findViewById(R.id.rl_tbsView);
        back_icon.setOnClickListener(this);
    }

    /**
     * 获取传过来的文件url和文件名
     */
    private void getFileUrlByIntent() {
        Intent intent = getIntent();
        mFileUrl = intent.getStringExtra("fileUrl");
        fileName = intent.getStringExtra("fileName");
        tv_title.setText("文件预览");
    }
    private String tbsReaderTemp = Environment.getExternalStorageDirectory() + "/TbsReaderTemp";
    /**
     * 加载显示文件内容
     */
    private void displayFile() {
        String bsReaderTemp = tbsReaderTemp;
        File bsReaderTempFile =new File(bsReaderTemp);
        if (!bsReaderTempFile.exists()) {
            Logger.d("print","准备创建/TbsReaderTemp！！");
            boolean mkdir = bsReaderTempFile.mkdir();
            if(!mkdir){
                Logger.d("print","创建/TbsReaderTemp失败！！！！！");
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString("filePath", getLocalFile().getPath());
        bundle.putString("tempPath", bsReaderTemp);
        boolean result = mTbsReaderView.preOpen(getFileType(mFileName), false);
        if(!result){
            QbSdk.clearAllWebViewCache(this, true);
            result = mTbsReaderView.preOpen(getFileType(mFileName), false);
        }
        if (result) {
            //App页面内部自行渲染，如果不行走QB流程
            mTbsReaderView.openFile(bundle);
        } else {
            //QB流程,现场时用Qb mini打开，如果不行用QQ浏览器打开，如果没有安装QQ浏览器会引导下载
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("local", "true");
            JSONObject Object = new JSONObject();
            try
            {
                Object.put("pkgName",this.getApplicationContext().getPackageName());
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            params.put("menuData",Object.toString());
            QbSdk.getMiniQBVersion(this);
            int ret = QbSdk.openFileReader(this, getLocalFile().getAbsolutePath(), params, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String val) {
                    Logger.d("onReceiveValue,val ="+val);
                    finish();
                }
            });
        }
    }

    private String getFileType(String paramString) {
        String str = "";

        if (TextUtils.isEmpty(paramString)) {
            Log.d("print", "paramString---->null");
            return str;
        }
        Log.d("print", "paramString:" + paramString);
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
            Log.d("print", "i <= -1");
            return str;
        }

        str = paramString.substring(i + 1);
        Log.d("print", "paramString.substring(i + 1)------>" + str);
        return str;
    }
    /**
     * 利用文件url转换出文件名
     *
     * @param url
     * @return
     */
    private String parseName(String url) {
        String fileName = null;
        try {
            fileName = url.substring(url.lastIndexOf("/") + 1);
            fileName = URLDecoder.decode(fileName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (TextUtils.isEmpty(fileName)) {
                fileName = String.valueOf(System.currentTimeMillis());
            }
        }
        return fileName;
    }

    private boolean isLocalExist() {
        return getLocalFile().exists();
    }

    private File getLocalFile() {
        return new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                mFileName);
    }

    /**
     * 下载文件
     */
    @SuppressLint("NewApi")
    private void startDownload() {
        mDownloadObserver = new DownloadObserver(new Handler());
        getContentResolver().registerContentObserver(
                Uri.parse("content://downloads/my_downloads"), true,
                mDownloadObserver);

        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //将含有中文的url进行encode
        String fileUrl = toUtf8String(mFileUrl);
        try {

            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(fileUrl));
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, mFileName);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            mRequestId = mDownloadManager.enqueue(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query()
                .setFilterById(mRequestId);
        Cursor cursor = null;
        try {
            cursor = mDownloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                // 已经下载的字节数
                long currentBytes = cursor
                        .getLong(cursor
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                // 总需下载的字节数
                long totalBytes = cursor
                        .getLong(cursor
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                // 状态所在的列索引
                int status = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_STATUS));
                tv_download.setText("下载中...(" + formatKMGByBytes(currentBytes)
                        + "/" + formatKMGByBytes(totalBytes) + ")");
                // 将当前下载的字节数转化为进度位置
                int progress = (int) ((currentBytes * 1.0) / totalBytes * 100);
                progressBar_download.setProgress(progress);

                Log.i("downloadUpdate: ", currentBytes + " " + totalBytes + " "
                        + status + " " + progress);
                if (DownloadManager.STATUS_SUCCESSFUL == status
                        && tv_download.getVisibility() == View.VISIBLE) {
                    tv_download.setVisibility(View.GONE);
                    tv_download.performClick();
                    if (isLocalExist()) {
                        tv_download.setVisibility(View.GONE);
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        displayFile();
                                    }
                                }
                        );
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTbsReaderView.onStop();
        if (mDownloadObserver != null) {
            getContentResolver().unregisterContentObserver(mDownloadObserver);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            finish();
        }
    }

    /**
     * 将字节数转换为KB、MB、GB
     *
     * @param size 字节大小
     * @return
     */
    private String formatKMGByBytes(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.00");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    private class DownloadObserver extends ContentObserver {

        private DownloadObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            queryDownloadStatus();
        }
    }
}
