package com.ymy.core.utils;

import com.ymy.core.Ktx;

import java.io.File;


public class TUIKitConstants {

    public static final String CAMERA_IMAGE_PATH = "camera_image_path";
    public static final String IMAGE_WIDTH = "image_width";
    public static final String IMAGE_HEIGHT = "image_height";
    public static final String VIDEO_TIME = "video_time";
    public static final String CAMERA_VIDEO_PATH = "camera_video_path";
    public static final String IMAGE_DATA = "image_data";
    public static final String SELF_MESSAGE = "self_message";
    public static final String CAMERA_TYPE = "camera_type";
    public static String APP_DIR =  Ktx.app.getFilesDir().getPath();
    public static String RECORD_DIR = APP_DIR + "/record/";
    public static String RECORD_DOWNLOAD_DIR = APP_DIR + "/record/download/";
    public static String VIDEO_DOWNLOAD_DIR = APP_DIR + "/video/download/";
    public static String IMAGE_BASE_DIR = APP_DIR + "/image/";
    public static String IMAGE_DOWNLOAD_DIR = IMAGE_BASE_DIR + "download/";
    public static String IMAGE_GROUP_FACE_DIR = IMAGE_BASE_DIR + "groupface/";
    public static String MEDIA_DIR = APP_DIR + "/media";
    public static String FILE_DOWNLOAD_DIR = APP_DIR + "/file/download/";
    public static String CRASH_LOG_DIR = APP_DIR + "/crash/";
    public static String UI_PARAMS = "ilive_ui_params";
    public static String SOFT_KEY_BOARD_HEIGHT = "soft_key_board_height";

    public static void initPath() {

        File f = new File(TUIKitConstants.MEDIA_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(TUIKitConstants.RECORD_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(TUIKitConstants.RECORD_DOWNLOAD_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(TUIKitConstants.VIDEO_DOWNLOAD_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(TUIKitConstants.IMAGE_DOWNLOAD_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(TUIKitConstants.FILE_DOWNLOAD_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(TUIKitConstants.CRASH_LOG_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }

    }
}
