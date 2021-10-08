package com.tencent.qcloud.tim.uikit.component.photoview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ymy.im.down.listener.DownloadListener;
import com.ymy.im.down.utils.DownloadUtils;
import com.ymy.im.ijk.AliVideoDetailActivity;
import com.ymy.im.ijk.VideoDetailActivity;
import com.ymy.im.module.MaxBean;
import com.ymy.core.utils.MediaFileUtil;
import com.tencent.imsdk.v2.V2TIMDownloadCallback;
import com.tencent.imsdk.v2.V2TIMElem;
import com.tencent.imsdk.v2.V2TIMVideoElem;
import com.tencent.qcloud.tim.uikit.R;
import com.tencent.qcloud.tim.uikit.component.touchview.PhotoViewAttacher;
import com.tencent.qcloud.tim.uikit.component.touchview.UrlTouchImageView;
import com.tencent.qcloud.tim.uikit.utils.FileUtil;
import com.tencent.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.qcloud.tim.uikit.utils.TUIKitLog;
import com.tencent.qcloud.tim.uikit.utils.ToastUtil;
import com.ymy.core.utils.StringUtils;

import java.io.File;
import java.util.HashMap;


public class PhotoViewFragment extends Fragment {

    MaxBean.Data mData;
    HashMap<String, String> hasSaveHp = new HashMap<>();
    private UrlTouchImageView mPhotoView;
    private View mBaseView;
    private ImageView save_btn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_photo_view, container, false);
        onCreate();
        return mBaseView;
    }

    public void setData(MaxBean.Data data) {
        mData = data;
        onCreate();
    }

    public void onCreate() {
        if (mBaseView == null) {
            return;
        }
        if (mData == null) {
            return;
        }
        if ("1".equals(mData.getType())) {
            mBaseView.findViewById(R.id.video_play_btn).setVisibility(View.VISIBLE);
        } else {
            mBaseView.findViewById(R.id.video_play_btn).setVisibility(View.GONE);
        }
        save_btn = mBaseView.findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasSaveHp.containsKey(mData.getId())) {//防止重复处理
                    return;
                }
                hasSaveHp.put(mData.getId(), mData.getId());
                if (mData.getUrl().startsWith("http")) {
                    DownloadUtils downloadUtils = new DownloadUtils(getContext());
                    downloadUtils.downloadFile(mData.getUrl(), getPath(), getFileName(mData.getUrl()), new DownloadListener() {
                        @Override
                        public void onStart() {
                            Log.e("downloadUtils", "onStart");
                        }

                        @Override
                        public void onProgress(int currentLength) {
                            Log.e("downloadUtils", "currentLength = " + currentLength);
                        }

                        @Override
                        public void onFinish(String localPath) {
                            Log.e("downloadUtils", "onFinish");
                            if (getActivity() != null) {
                                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + localPath)));
                                checkFile(mData.getUrl());
                                ToastUtil.toastShortMessage("保存成功");
                            }
                        }

                        @Override
                        public void onFailure(String errorInfo) {
                            Log.e("downloadUtils", "onFailure");
                            hasSaveHp.remove(mData.getId());
                            checkFile(mData.getUrl());
                        }
                    });
                } else {
                    String url = mData.getUrl();
                    if (mData.getVideoElem() != null && !MediaFileUtil.isVideoFileType(url)) {
                        final String videoPath = TUIKitConstants.VIDEO_DOWNLOAD_DIR + mData.getVideoElem().getVideoUUID();
                        if (mData.getVideoElem() != null) {
                            final File videoFile = new File(videoPath);
                            if (!videoFile.exists()) {//若存在本地文件则优先获取本地文件
                                getVideo(mData.getVideoElem(), videoPath, true);
                            }
                        }
                        return;
                    }
                    FileUtil.copyFile(mData.getUrl(), getSavePath(mData.getUrl()));
                    checkFile(mData.getUrl());
                    ToastUtil.toastShortMessage("保存成功");
                }
            }
        });
        checkFile(mData.getUrl());
        mPhotoView = mBaseView.findViewById(R.id.photo_view);
        mPhotoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                if ("1".equals(mData.getType())) {
                    String url = mData.getUrl();
                    if (mData.getVideoElem() != null && !MediaFileUtil.isVideoFileType(url)) {
                        final String videoPath = TUIKitConstants.VIDEO_DOWNLOAD_DIR + mData.getVideoElem().getVideoUUID();
                        if (mData.getVideoElem() != null) {
                            final File videoFile = new File(videoPath);
                            if (!videoFile.exists()) {//若存在本地文件则优先获取本地文件
                                getVideo(mData.getVideoElem(), videoPath, false);
                            }
                        }
                        ToastUtil.toastShortMessage("正在加载视频请稍后");
                        return;
                    }
                    invokeVideoDetail(url);
                }
                getActivity().finish();
            }
        });
        mPhotoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                if ("1".equals(mData.getType())) {
                    String url = mData.getUrl();
                    if (mData.getVideoElem() != null && !MediaFileUtil.isVideoFileType(url)) {
                        final String videoPath = TUIKitConstants.VIDEO_DOWNLOAD_DIR + mData.getVideoElem().getVideoUUID();
                        if (mData.getVideoElem() != null) {
                            final File videoFile = new File(videoPath);
                            if (!videoFile.exists()) {//若存在本地文件则优先获取本地文件
                                getVideo(mData.getVideoElem(), videoPath, false);
                            }
                        }
                        ToastUtil.toastShortMessage("正在加载视频请稍后");
                        return;
                    }
                    invokeVideoDetail(url);
                }
                getActivity().finish();
            }
        });
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("1".equals(mData.getType())) {
                    String url = mData.getUrl();
                    if (mData.getVideoElem() != null && !MediaFileUtil.isVideoFileType(url)) {
                        final String videoPath = TUIKitConstants.VIDEO_DOWNLOAD_DIR + mData.getVideoElem().getVideoUUID();
                        if (mData.getVideoElem() != null) {
                            final File videoFile = new File(videoPath);
                            if (!videoFile.exists()) {//若存在本地文件则优先获取本地文件
                                getVideo(mData.getVideoElem(), videoPath, false);
                            }
                        }
                        ToastUtil.toastShortMessage("正在加载视频请稍后");
                        return;
                    }
                    invokeVideoDetail(url);
                }
                getActivity().finish();
            }
        });
        TextView position = mBaseView.findViewById(R.id.position);
        position.setText(mData.getPosition() + "/" + mData.getLength());
        position.setVisibility(View.GONE);
        if ("1".equals(mData.getType())) {
            //视频
            String coverUrl = mData.getCoverUrl();
            String url = mData.getUrl();
            if (coverUrl.isEmpty() && url.startsWith("https://dbx-assets.oss")) {
                coverUrl = url + "?x-oss-process=video/snapshot,t_0,f_jpg,w_600,h_1200,m_fast,ar_auto";
            }
            mPhotoView.setBgUrl(coverUrl);
        } else {
            mPhotoView.setBgUrl(mData.getUrl());
        }
    }

    private void invokeVideoDetail(String url) {
        if (url.startsWith("https://dbx-assets.oss-cn-shanghai.aliyuncs.com")) {
            AliVideoDetailActivity.invoke(getContext(), mData.getVideoElem(), url, mData.getCoverUrl(), mPhotoView.getImageHeight(), mPhotoView.getImageWidth());
        } else {
            VideoDetailActivity.invoke(getContext(), mData.getVideoElem(), url, mData.getCoverUrl(), mPhotoView.getImageHeight(), mPhotoView.getImageWidth());
        }
    }

    private void getVideo(V2TIMVideoElem videoElem, final String videoPath, final boolean needSave) {
        videoElem.downloadVideo(videoPath, new V2TIMDownloadCallback() {
            @Override
            public void onProgress(V2TIMElem.V2ProgressInfo progressInfo) {
                TUIKitLog.i("downloadVideo progress current:", progressInfo.getCurrentSize() + ", total:" + progressInfo.getTotalSize());
            }

            @Override
            public void onError(int code, String desc) {
                hasSaveHp.remove(mData.getId());
            }

            @Override
            public void onSuccess() {
                mData.setUrl(videoPath);
                if (needSave) {
                    FileUtil.copyFile(mData.getUrl(), getSavePath(mData.getUrl()));
                    ToastUtil.toastShortMessage("保存成功");
                    if (getActivity() != null) {
                        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + getSavePath(mData.getUrl()))));
                    }
                    checkFile(mData.getUrl());
                }
            }
        });
    }

    /**
     * 检测是否已下载
     *
     * @param path
     */
    public void checkFile(String path) {
        if (new File(getSavePath(path)).exists()) {
            save_btn.setVisibility(View.GONE);
        } else {
            save_btn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取保存文件地址
     *
     * @param path
     * @return
     */
    public String getSavePath(String path) {
        String pathStr = getPath();
        try {
            if (!new File(pathStr).exists()) {
                new File(pathStr).mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathStr + getFileName(path);
    }

    public String getPath() {
        if ("1".equals(mData.getType())) {
            return "/sdcard/dbx/video/";
        }
        return "/sdcard/dbx/image/";
    }

    public String getFileName(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        if (path.contains("/")) {
            int index = path.lastIndexOf("/");
            return path.substring(index + 1);
        }
        return "";
    }
}
