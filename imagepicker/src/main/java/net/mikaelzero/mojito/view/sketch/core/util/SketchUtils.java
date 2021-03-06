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

package net.mikaelzero.mojito.view.sketch.core.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.opengl.EGL14;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.Initializer;
import net.mikaelzero.mojito.view.sketch.core.SLog;
import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.SketchView;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPool;
import net.mikaelzero.mojito.view.sketch.core.datasource.DataSource;
import net.mikaelzero.mojito.view.sketch.core.decode.ImageDecodeUtils;
import net.mikaelzero.mojito.view.sketch.core.decode.ImageOrientationCorrector;
import net.mikaelzero.mojito.view.sketch.core.decode.ImageType;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchLoadingDrawable;
import net.mikaelzero.mojito.view.sketch.core.request.DisplayRequest;
import net.mikaelzero.mojito.view.sketch.core.uri.UriModel;
import net.mikaelzero.mojito.view.sketch.core.zoom.Size;
import net.mikaelzero.mojito.view.sketch.core.zoom.block.Block;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


@SuppressWarnings("WeakerAccess")
public class SketchUtils {

    private static final float[] MATRIX_VALUES = new float[9];

    /**
     * Read apk file icon. Although the PackageManager will cache the icon, the bitmap returned by this method every time
     *
     * @param context         {@link Context}
     * @param apkFilePath     Apk file path
     * @param lowQualityImage If set true use ARGB_4444 create bitmap, KITKAT is above is invalid
     * @param logName         Print log is used identify log type
     * @param bitmapPool      Try to find Reusable bitmap from bitmapPool
     */
    @Nullable
    public static Bitmap readApkIcon(@NonNull Context context, @NonNull String apkFilePath, boolean lowQualityImage,
                                     @NonNull String logName, @NonNull BitmapPool bitmapPool) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo == null) {
            SLog.w(logName, "get packageInfo is null. %s", apkFilePath);
            return null;
        }

        packageInfo.applicationInfo.sourceDir = apkFilePath;
        packageInfo.applicationInfo.publicSourceDir = apkFilePath;

        Drawable drawable = null;
        try {
            drawable = packageManager.getApplicationIcon(packageInfo.applicationInfo);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        if (drawable == null) {
            SLog.w(logName, "app icon is null. %s", apkFilePath);
            return null;
        }

        return drawableToBitmap(drawable, lowQualityImage, bitmapPool);
    }

    /**
     * Drawable into Bitmap. Each time a new bitmap is drawn
     */
    @Nullable
    public static Bitmap drawableToBitmap(@Nullable Drawable drawable, boolean lowQualityImage, @Nullable BitmapPool bitmapPool) {
        if (drawable == null || drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            return null;
        }

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        Bitmap.Config config = lowQualityImage ? Bitmap.Config.ARGB_4444 : Bitmap.Config.ARGB_8888;
        Bitmap bitmap;
        if (bitmapPool != null) {
            bitmap = bitmapPool.getOrMake(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), config);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), config);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * ????????????
     *
     * @return true?????????
     */
    public static boolean cleanDir(@Nullable File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return true;
        }

        File[] files = dir.listFiles();
        boolean cleanSuccess = true;
        if (files != null) {
            for (File tempFile : files) {
                if (tempFile.isDirectory()) {
                    cleanSuccess &= cleanDir(tempFile);
                }
                cleanSuccess &= tempFile.delete();
            }
        }
        return cleanSuccess;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param file ???????????????
     * @return true??????????????????false???????????????
     */
    public static boolean deleteFile(@Nullable File file) {
        if (file == null || !file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            cleanDir(file);
        }
        return file.delete();
    }

    /**
     * ???????????????????????????????????????
     *
     * @param fileName ?????????test.txt
     * @param suffix   ?????????.txt
     */
    public static boolean checkSuffix(@Nullable String fileName, @NonNull String suffix) {
        if (fileName == null) {
            return false;
        }

        // ???????????????
        String fileNameSuffix;
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex > -1) {
            fileNameSuffix = fileName.substring(lastIndex);
        } else {
            return false;
        }

        return suffix.equalsIgnoreCase(fileNameSuffix);
    }

    public static void close(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }

        if (closeable instanceof OutputStream) {
            try {
                ((OutputStream) closeable).flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(@Nullable AssetFileDescriptor fileDescriptor) {
        if (fileDescriptor == null) {
            return;
        }

        try {
            fileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isGifImage(@Nullable Drawable drawable) {
        if (drawable != null) {
            LayerDrawable layerDrawable;
            while (drawable instanceof LayerDrawable) {
                layerDrawable = (LayerDrawable) drawable;
                if (layerDrawable.getNumberOfLayers() > 0) {
                    drawable = layerDrawable.getDrawable(layerDrawable.getNumberOfLayers() - 1);
                } else {
                    drawable = null;
                }
            }
            return drawable instanceof SketchDrawable && ImageType.GIF.getMimeType().equals(((SketchDrawable) drawable).getMimeType());
        }

        return false;
    }

    @NonNull
    public static String viewLayoutFormatted(int size) {
        if (size >= 0) {
            return String.valueOf(size);
        } else if (size == ViewGroup.LayoutParams.MATCH_PARENT) {
            return "MATCH_PARENT";
        } else if (size == ViewGroup.LayoutParams.WRAP_CONTENT) {
            return "WRAP_CONTENT";
        } else {
            return "Unknown";
        }
    }

    /**
     * ??????????????????
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * ???????????????????????????
     */
    @Nullable
    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am != null ? am.getRunningAppProcesses() : null;
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    /**
     * ??????????????????????????????
     */
    public static boolean isMainProcess(@NonNull Context context) {
        return context.getPackageName().equalsIgnoreCase(getProcessName(context));
    }

    /**
     * ????????????????????????????????????????????????????????? com.my.app:push???????????????????????? :push
     */
    @Nullable
    public static String getSimpleProcessName(@NonNull Context context) {
        String processName = getProcessName(context);
        if (processName == null) {
            return null;
        }
        String packageName = context.getPackageName();
        int lastIndex = processName.lastIndexOf(packageName);
        return lastIndex != -1 ? processName.substring(lastIndex + packageName.length()) : null;
    }

    /**
     * ?????? app ??????????????????????????? sdcard ??????????????????
     */
    @Nullable
    public static File getAppCacheDir(@NonNull Context context) {
        File appCacheDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            appCacheDir = context.getExternalCacheDir();
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    /**
     * ?????????????????????????????????
     */
    public static long getAvailableBytes(@NonNull File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            return 0;
        }
        StatFs dirStatFs;
        try {
            dirStatFs = new StatFs(dir.getPath());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return dirStatFs.getAvailableBytes();
        } else {
            //noinspection deprecation
            return (long) dirStatFs.getAvailableBlocks() * dirStatFs.getBlockSize();
        }
    }

    /**
     * ??????????????????????????????
     */
    public static long getTotalBytes(@NonNull File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            return 0;
        }
        StatFs dirStatFs;
        try {
            dirStatFs = new StatFs(dir.getPath());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return dirStatFs.getTotalBytes();
        } else {
            //noinspection deprecation
            return (long) dirStatFs.getBlockCount() * dirStatFs.getBlockSize();
        }
    }

    /**
     * ????????????????????? sdcard ?????????
     *
     * @return ??????????????? sdcard ?????????
     */
    @Nullable
    public static String[] getAllAvailableSdcardPath(@NonNull Context context) {
        String[] paths;
        Method getVolumePathsMethod;
        try {
            getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths");
        } catch (NoSuchMethodException e) {
            SLog.e("getAllAvailableSdcardPath", "not found StorageManager.getVolumePaths() method");
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return new String[]{Environment.getExternalStorageDirectory().getPath()};
            } else {
                return null;
            }
        }

        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            paths = (String[]) getVolumePathsMethod.invoke(sm);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

        if (paths == null || paths.length == 0) {
            return null;
        }

        // ???????????????????????????
        List<String> storagePathList = new LinkedList<>();
        Collections.addAll(storagePathList, paths);
        Iterator<String> storagePathIterator = storagePathList.iterator();

        String path;
        Method getVolumeStateMethod = null;
        while (storagePathIterator.hasNext()) {
            path = storagePathIterator.next();
            if (getVolumeStateMethod == null) {
                try {
                    getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState", String.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            String status;
            try {
                status = (String) getVolumeStateMethod.invoke(sm, path);
            } catch (Exception e) {
                e.printStackTrace();
                storagePathIterator.remove();
                continue;
            }
            if (!(Environment.MEDIA_MOUNTED.equals(status) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(status))) {
                storagePathIterator.remove();
            }
        }
        return storagePathList.toArray(new String[0]);
    }

    @NonNull
    public static String appendProcessName(@NonNull Context context, @NonNull String dirName) {
        // ??????????????????????????????????????????????????????????????????????????????????????????
        String simpleProcessName = SketchUtils.getSimpleProcessName(context);
        if (simpleProcessName != null) {
            try {
                dirName += URLEncoder.encode(simpleProcessName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return dirName;
    }

    @NonNull
    public static File getDefaultSketchCacheDir(@NonNull Context context, @NonNull String dirName, boolean compatManyProcess) {
        File appCacheDir = SketchUtils.getAppCacheDir(context);
        return new File(appCacheDir, compatManyProcess ? appendProcessName(context, dirName) : dirName);
    }

    public static boolean testCreateFile(@NonNull File cacheDir) throws Exception {
        File parentDir = cacheDir;
        while (parentDir != null) {
            // ???????????????????????????????????????
            if (!parentDir.exists()) {
                parentDir = cacheDir.getParentFile();
                continue;
            }

            // ????????????????????????
            File file = new File(parentDir, "create_test.temp");

            // ????????????????????????????????????????????????
            if (file.exists() && !file.delete()) {
                throw new Exception("Delete old test file failed: " + file.getPath());
            }

            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();

            if (file.exists()) {
                if (file.delete()) {
                    return true;
                } else {
                    throw new Exception("Delete test file failed: " + file.getPath());
                }
            } else {
                return false;
            }
        }

        return false;
    }

    /**
     * ????????????????????????????????? sdcard ?????????
     *
     * @param dirName            ????????????
     * @param compatManyProcess  ?????????????????????????????????
     * @param minSpaceSize       ????????????
     * @param cleanOnNoSpace     ???????????????????????????????????????
     * @param cleanOldCacheFiles ????????????????????????
     * @param expandNumber       ??? dirName ??????????????????????????? dirName1???dirName2???dirName3...
     * @return ???????????????????????????????????????????????????
     * @throws NoSpaceException ?????????????????? minSpaceSize???UnableCreateDirException??????????????????????????????UnableCreateFileException???????????????????????????????????????
     */
    @NonNull
    public static File buildCacheDir(@NonNull Context context, @NonNull String dirName, boolean compatManyProcess, long minSpaceSize, boolean cleanOnNoSpace,
                                     boolean cleanOldCacheFiles, int expandNumber) throws NoSpaceException, UnableCreateDirException, UnableCreateFileException {
        List<File> appCacheDirs = new LinkedList<>();

        String[] sdcardPaths = getAllAvailableSdcardPath(context);
        if (sdcardPaths != null && sdcardPaths.length > 0) {
            for (String sdcardPath : sdcardPaths) {
                appCacheDirs.add(new File(sdcardPath, "Android" + File.separator + "data" + File.separator + context.getPackageName() + File.separator + "cache"));
            }
        }
        appCacheDirs.add(context.getCacheDir());

        String diskCacheDirName = compatManyProcess ? appendProcessName(context, dirName) : dirName;

        NoSpaceException noSpaceException = null;
        UnableCreateFileException unableCreateFileException = null;
        File diskCacheDir = null;
        int expandCount;

        for (File appCacheDir : appCacheDirs) {
            expandCount = 0;
            while (expandCount <= expandNumber) {
                diskCacheDir = new File(appCacheDir, diskCacheDirName + (expandCount > 0 ? expandCount : ""));

                if (diskCacheDir.exists()) {
                    // ??????????????????????????????????????????????????????
                    if (cleanOldCacheFiles) {
                        File journalFile = new File(diskCacheDir, DiskLruCache.JOURNAL_FILE);
                        if (!journalFile.exists()) {
                            cleanDir(diskCacheDir);
                        }
                    }
                } else {
                    // ?????????????????????????????????????????????false?????????????????????????????????????????????
                    if (!diskCacheDir.mkdirs() && !diskCacheDir.exists()) {
                        expandCount++;
                        continue;
                    }
                }

                // ?????????????????????minSpaceSize???????????????
                long availableBytes = getAvailableBytes(diskCacheDir);
                if (availableBytes < minSpaceSize) {
                    // ?????????????????????????????????????????????????????????????????????
                    if (cleanOnNoSpace) {
                        cleanDir(diskCacheDir);
                        availableBytes = getAvailableBytes(diskCacheDir);
                    }

                    // ????????????????????????????????????
                    if (availableBytes < minSpaceSize) {
                        String availableFormatted = Formatter.formatFileSize(context, availableBytes);
                        String minSpaceFormatted = Formatter.formatFileSize(context, minSpaceSize);
                        noSpaceException = new NoSpaceException("Need " + availableFormatted + ", with only " + minSpaceFormatted + " in " + diskCacheDir.getPath());
                        break;
                    }
                }

                // ??????????????????
                try {
                    if (testCreateFile(diskCacheDir)) {
                        return diskCacheDir;
                    } else {
                        unableCreateFileException = new UnableCreateFileException("Unable create file in " + diskCacheDir.getPath());
                        expandCount++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    unableCreateFileException = new UnableCreateFileException(e.getClass().getSimpleName() + ": " + e.getMessage());
                    expandCount++;
                }
            }
        }

        if (noSpaceException != null) {
            throw noSpaceException;
        } else if (unableCreateFileException != null) {
            throw unableCreateFileException;
        } else {
            throw new UnableCreateDirException("Unable create dir: " + (diskCacheDir != null ? diskCacheDir.getPath() : "null"));
        }
    }

    /**
     * ??? {@link SketchView} ????????? {@link DisplayRequest}
     */
    @Nullable
    public static DisplayRequest findDisplayRequest(@Nullable SketchView sketchView) {
        if (sketchView != null) {
            final Drawable drawable = sketchView.getDrawable();
            if (drawable instanceof SketchLoadingDrawable) {
                return ((SketchLoadingDrawable) drawable).getRequest();
            }
        }
        return null;
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param type            ??????
     * @param imageWidth      ?????????
     * @param imageHeight     ?????????
     * @param mimeType        ????????????
     * @param exifOrientation ????????????
     * @param bitmap          {@link Bitmap}
     * @param byteCount       {@link Bitmap} ???????????????
     */
    @NonNull
    public static String makeImageInfo(@Nullable String type, int imageWidth, int imageHeight, @Nullable String mimeType,
                                       int exifOrientation, @Nullable Bitmap bitmap, long byteCount, @Nullable String key) {
        if (bitmap == null) {
            return "Unknown";
        }

        type = TextUtils.isEmpty(type) ? "Bitmap" : type;
        String hashCode = Integer.toHexString(bitmap.hashCode());
        String config = bitmap.getConfig() != null ? bitmap.getConfig().name() : null;
        String finalKey = key != null ? String.format(", key=%s", key) : "";
        return String.format(Locale.US, "%s(image=%dx%d,%s,%s, bitmap=%dx%d,%s,%d,%s%s)",
                type, imageWidth, imageHeight, mimeType, ImageOrientationCorrector.toName(exifOrientation),
                bitmap.getWidth(), bitmap.getHeight(), config, byteCount, hashCode,
                finalKey);
    }

    /**
     * ????????? {@link LayerDrawable}????????????????????????????????????????????????????????????
     */
    @Nullable
    public static Drawable getLastDrawable(@Nullable Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (!(drawable instanceof LayerDrawable)) {
            return drawable;
        }

        LayerDrawable layerDrawable = (LayerDrawable) drawable;

        int layerCount = layerDrawable.getNumberOfLayers();
        if (layerCount <= 0) {
            return null;
        }

        return getLastDrawable(layerDrawable.getDrawable(layerCount - 1));
    }

    /**
     * ?????????????????????????????????
     *
     * @param matrix     {@link Matrix}
     * @param whichValue ???????????????????????? {@link Matrix#MSCALE_X}
     */
    public static float getMatrixValue(@NonNull Matrix matrix, int whichValue) {
        synchronized (MATRIX_VALUES) {
            matrix.getValues(MATRIX_VALUES);
            return MATRIX_VALUES[whichValue];
        }
    }

    /**
     * ??? {@link Matrix} ?????????????????????
     */
    public static float getMatrixScale(@NonNull Matrix matrix) {
        synchronized (MATRIX_VALUES) {
            matrix.getValues(MATRIX_VALUES);
            final float scaleX = MATRIX_VALUES[Matrix.MSCALE_X];
            final float skewY = MATRIX_VALUES[Matrix.MSKEW_Y];
            return (float) Math.sqrt((float) Math.pow(scaleX, 2) + (float) Math.pow(skewY, 2));
        }
    }

    /**
     * ??? {@link Matrix} ?????????????????????
     */
    public static int getMatrixRotateDegrees(@NonNull Matrix matrix) {
        synchronized (MATRIX_VALUES) {
            matrix.getValues(MATRIX_VALUES);
            final float skewX = MATRIX_VALUES[Matrix.MSKEW_X];
            final float scaleX = MATRIX_VALUES[Matrix.MSCALE_X];
            final int degrees = (int) Math.round(Math.atan2(skewX, scaleX) * (180 / Math.PI));
            if (degrees < 0) {
                return Math.abs(degrees);
            } else if (degrees > 0) {
                return 360 - degrees;
            } else {
                return 0;
            }
        }
    }

    /**
     * ??? {@link Matrix} ?????????????????????
     */
    public static void getMatrixTranslation(@NonNull Matrix matrix, @NonNull PointF point) {
        synchronized (MATRIX_VALUES) {
            matrix.getValues(MATRIX_VALUES);
            point.x = MATRIX_VALUES[Matrix.MTRANS_X];
            point.y = MATRIX_VALUES[Matrix.MTRANS_Y];
        }
    }

    /**
     * ?????? OpenGL ?????????
     */
    @NonNull
    public static String getOpenGLVersion(@NonNull Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am != null ? am.getDeviceConfigurationInfo().getGlEsVersion() : "";
    }

    /**
     * ?????? OpenGL ?????????????????????????????????(?????????)
     */
    public static int getOpenGLMaxTextureSize() {
        int maxTextureSize = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                maxTextureSize = getOpenGLMaxTextureSizeJB1();
            } else {
                maxTextureSize = getOpenGLMaxTextureSizeBase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (maxTextureSize == 0) {
            maxTextureSize = 4096;
        }

        return maxTextureSize;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static int getOpenGLMaxTextureSizeJB1() {
        // Then get a hold of the default display, and initialize.
        // This could get more complex if you have to deal with devices that could have multiple displays,
        // but will be sufficient for a typical phone/tablet:
        android.opengl.EGLDisplay dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] vers = new int[2];
        EGL14.eglInitialize(dpy, vers, 0, vers, 1);

        // Next, we need to find a config. Since we won't use this context for rendering,
        // the exact attributes aren't very critical:
        int[] configAttr = {
                EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
                EGL14.EGL_LEVEL, 0,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
        };
        android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
        int[] numConfig = new int[1];
        EGL14.eglChooseConfig(dpy, configAttr, 0,
                configs, 0, 1, numConfig, 0);
        //noinspection StatementWithEmptyBody
        if (numConfig[0] == 0) {
            // TROUBLE! No config found.
        }
        android.opengl.EGLConfig config = configs[0];

        // To make a context current, which we will need later,
        // you need a rendering surface, even if you don't actually plan to render.
        // To satisfy this requirement, create a small offscreen (Pbuffer) surface:
        int[] surfAttr = {
                EGL14.EGL_WIDTH, 64,
                EGL14.EGL_HEIGHT, 64,
                EGL14.EGL_NONE
        };
        android.opengl.EGLSurface surf = EGL14.eglCreatePbufferSurface(dpy, config, surfAttr, 0);

        // Next, create the context:
        int[] ctxAttrib = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        android.opengl.EGLContext ctx = EGL14.eglCreateContext(dpy, config, EGL14.EGL_NO_CONTEXT, ctxAttrib, 0);

        // Ready to make the context current now:
        EGL14.eglMakeCurrent(dpy, surf, surf, ctx);

        // If all of the above succeeded (error checking was omitted), you can make your OpenGL calls now:
        int[] maxSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0);

        // Once you're all done, you can tear down everything:
        EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(dpy, surf);
        EGL14.eglDestroyContext(dpy, ctx);
        EGL14.eglTerminate(dpy);

        return maxSize[0];
    }

    private static int getOpenGLMaxTextureSizeBase() {
        // In JELLY_BEAN will collapse
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            return 0;
        }

        EGL10 egl = (EGL10) EGLContext.getEGL();

        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] vers = new int[2];
        egl.eglInitialize(dpy, vers);

        int[] configAttr = {
                EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                EGL10.EGL_LEVEL, 0,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                EGL10.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfig = new int[1];
        egl.eglChooseConfig(dpy, configAttr, configs, 1, numConfig);
        //noinspection StatementWithEmptyBody
        if (numConfig[0] == 0) {
            // TROUBLE! No config found.
        }
        EGLConfig config = configs[0];

        int[] surfAttr = new int[]{
                EGL10.EGL_WIDTH, 64,
                EGL10.EGL_HEIGHT, 64,
                EGL10.EGL_NONE
        };
        EGLSurface surf = egl.eglCreatePbufferSurface(dpy, config, surfAttr);
        final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;  // missing in EGL10
        int[] ctxAttrib = {
                EGL_CONTEXT_CLIENT_VERSION, 1,
                EGL10.EGL_NONE
        };
        EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, ctxAttrib);
        egl.eglMakeCurrent(dpy, surf, surf, ctx);
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);

        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surf);
        egl.eglDestroyContext(dpy, ctx);
        egl.eglTerminate(dpy);

        return maxSize[0];
    }

    /**
     * ???????????????????????????????????????????????????
     */
    public static float formatFloat(float floatValue, int newScale) {
        BigDecimal b = new BigDecimal(floatValue);
        return b.setScale(newScale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * ?????????????????????????????????????????????????????????
     */
    public static boolean formatSupportBitmapRegionDecoder(@Nullable ImageType imageType) {
        return imageType == ImageType.JPEG || imageType == ImageType.PNG || imageType == ImageType.WEBP;
    }

    /**
     * ??????????????????????????????
     */
    public static boolean isCross(@NonNull Rect rect1, @NonNull Rect rect2) {
        return rect1.left < rect2.right && rect2.left < rect1.right && rect1.top < rect2.bottom && rect2.top < rect1.bottom;
    }

    /**
     * dp ????????? px
     */
    public static int dp2px(@NonNull Context context, int dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5);
    }

    /**
     * ???????????????????????????????????????????????????????????? 90 ???????????????
     */
    public static void reverseRotateRect(@NonNull Rect rect, int rotateDegrees, @NonNull Size drawableSize) {
        if (rotateDegrees % 90 != 0) {
            return;
        }

        if (rotateDegrees == 90) {
            int cache = rect.bottom;
            //noinspection SuspiciousNameCombination
            rect.bottom = rect.left;
            //noinspection SuspiciousNameCombination
            rect.left = rect.top;
            //noinspection SuspiciousNameCombination
            rect.top = rect.right;
            rect.right = cache;

            rect.top = drawableSize.getHeight() - rect.top;
            rect.bottom = drawableSize.getHeight() - rect.bottom;
        } else if (rotateDegrees == 180) {
            int cache = rect.right;
            rect.right = rect.left;
            rect.left = cache;

            cache = rect.bottom;
            rect.bottom = rect.top;
            rect.top = cache;

            rect.top = drawableSize.getHeight() - rect.top;
            rect.bottom = drawableSize.getHeight() - rect.bottom;

            rect.left = drawableSize.getWidth() - rect.left;
            rect.right = drawableSize.getWidth() - rect.right;
        } else if (rotateDegrees == 270) {
            int cache = rect.bottom;
            //noinspection SuspiciousNameCombination
            rect.bottom = rect.right;
            //noinspection SuspiciousNameCombination
            rect.right = rect.top;
            //noinspection SuspiciousNameCombination
            rect.top = rect.left;
            rect.left = cache;

            rect.left = drawableSize.getWidth() - rect.left;
            rect.right = drawableSize.getWidth() - rect.right;
        }
    }

    /**
     * ??????????????????????????? 90 ????????????
     */
    public static void rotatePoint(@NonNull PointF point, int rotateDegrees, @NonNull Size drawableSize) {
        if (rotateDegrees % 90 != 0) {
            return;
        }

        if (rotateDegrees == 90) {
            float newX = drawableSize.getHeight() - point.y;
            //noinspection SuspiciousNameCombination
            float newY = point.x;
            point.x = newX;
            point.y = newY;
        } else if (rotateDegrees == 180) {
            float newX = drawableSize.getWidth() - point.x;
            float newY = drawableSize.getHeight() - point.y;
            point.x = newX;
            point.y = newY;
        } else if (rotateDegrees == 270) {
            //noinspection SuspiciousNameCombination
            float newX = point.y;
            float newY = drawableSize.getWidth() - point.x;
            point.x = newX;
            point.y = newY;
        }
    }

    /**
     * ???????????? key
     *
     * @param imageUri   ????????????
     * @param optionsKey ?????? key
     * @see SketchImageView#getOptionsKey()
     */
    @NonNull
    public static String makeRequestKey(@NonNull String imageUri, @NonNull UriModel uriModel, @NonNull String optionsKey) {
        if (uriModel.isConvertShortUriForKey()) {
            imageUri = SketchMD5Utils.md5(imageUri);
        }

        if (TextUtils.isEmpty(optionsKey)) {
            return imageUri;
        }

        StringBuilder builder = new StringBuilder(imageUri);
        if (imageUri.lastIndexOf("?") == -1) {
            builder.append('?');
        } else {
            builder.append('&');
        }
        builder.append("options");
        builder.append("=");
        builder.append(optionsKey);
        return builder.toString();
    }

    /**
     * ???????????????????????????????????????
     */
    @Nullable
    public static String blockListToString(@Nullable List<Block> blockList) {
        if (blockList == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Block block : blockList) {
            if (builder.length() > 1) {
                builder.append(",");
            }
            builder.append("\"");
            builder.append(block.drawRect.left).append(",");
            builder.append(block.drawRect.top).append(",");
            builder.append(block.drawRect.right).append(",");
            builder.append(block.drawRect.bottom);
            builder.append("\"");
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * ??????????????? {@link Bitmap} ?????????????????????????????????
     */
    @NonNull
    public static Bitmap.CompressFormat bitmapConfigToCompressFormat(@Nullable Bitmap.Config config) {
        return config == Bitmap.Config.RGB_565 ?
                Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG;
    }

    /**
     * ?????? {@link Bitmap} ?????????????????????????????????
     */
    public static int getByteCount(@Nullable Bitmap bitmap) {
        // bitmap.isRecycled()?????????????????????4.4?????????????????????bitmap?????????????????????getAllocationByteCount()?????????????????????
        if (bitmap == null || bitmap.isRecycled()) {
            return 0;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else {
            return bitmap.getByteCount();
        }
    }

    /**
     * ???????????????????????????????????????????????????
     */
    public static int computeByteCount(int width, int height, @Nullable Bitmap.Config config) {
        return width * height * getBytesPerPixel(config);
    }

    /**
     * ????????????????????????????????????????????????
     */
    public static int getBytesPerPixel(@Nullable Bitmap.Config config) {
        // A bitmap by decoding a gif has null "config" in certain environments.
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }

        int bytesPerPixel;
        switch (config) {
            case ALPHA_8:
                bytesPerPixel = 1;
                break;
            case RGB_565:
            case ARGB_4444:
                bytesPerPixel = 2;
                break;
            case ARGB_8888:
            default:
                bytesPerPixel = 4;
        }
        return bytesPerPixel;
    }

    /**
     * ???????????????????????????
     */
    @NonNull
    public static String getTrimLevelName(int level) {
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                return "COMPLETE";
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                return "MODERATE";
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                return "BACKGROUND";
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                return "UI_HIDDEN";
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                return "RUNNING_CRITICAL";
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                return "RUNNING_LOW";
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                return "RUNNING_MODERATE";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????
     */
    public static boolean invokeIn(@Nullable StackTraceElement[] stackTraceElements, @NonNull Class<?> cla, @NonNull String methodName) {
        if (stackTraceElements == null || stackTraceElements.length == 0) {
            return false;
        }

        String targetClassName = cla.getName();
        StackTraceElement element;
        String elementClassName;
        String elementMethodName;
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            element = stackTraceElement;

            elementClassName = element.getClassName();
            elementMethodName = element.getMethodName();
            if (targetClassName.equals(elementClassName) && methodName.equals(elementMethodName)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public static String toHexString(@Nullable Object object) {
        if (object == null) {
            return null;
        }

        return Integer.toHexString(object.hashCode());
    }

    public static int calculateSamplingSize(int value1, int inSampleSize) {
        return (int) Math.ceil(value1 / (float) inSampleSize);
    }

    public static int calculateSamplingSizeForRegion(int value1, int inSampleSize) {
        return (int) Math.floor(value1 / (float) inSampleSize);
    }

    public static boolean isDisabledARGB4444() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    @Nullable
    public static String generatorTempFileName(@NonNull DataSource dataSource, @NonNull String uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            ImageDecodeUtils.decodeBitmap(dataSource, options);
        } catch (Throwable e) {
            e.printStackTrace();
            options = null;
        }

        String uriEncode = SketchMD5Utils.md5(uri);
        if (options != null && options.outMimeType != null && options.outMimeType.startsWith("image/")) {
            String suffix = options.outMimeType.replace("image/", "");
            return String.format("%s.%s", uriEncode, suffix);
        } else {
            return uriEncode;
        }

    }

    @Nullable
    public static Initializer findInitializer(@NonNull Context context) {
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        String initializerClassName = null;
        if (appInfo.metaData != null) {
            for (String key : appInfo.metaData.keySet()) {
                if (Sketch.META_DATA_KEY_INITIALIZER.equals(appInfo.metaData.get(key))) {
                    initializerClassName = key;
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(initializerClassName)) {
            return null;
        }

        Class<?> initializerClass;
        try {
            initializerClass = Class.forName(initializerClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        if (!Initializer.class.isAssignableFrom(initializerClass)) {
            SLog.e("findInitializer", initializerClassName + " must be implements Initializer");
            return null;
        }
        //noinspection TryWithIdenticalCatches
        try {
            return (Initializer) initializerClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ???????????? uri ??????????????? key????????????????????? uri ??????????????????????????????????????????????????? key??????????????????????????????????????????????????????
     *
     * @param uri      ?????? uri
     * @param filePath ?????????????????????????????????????????????
     * @return ?????? uri ??????????????? key
     */
    @NonNull
    public static String createFileUriDiskCacheKey(@NonNull String uri, @NonNull String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            long lastModifyTime = file.lastModified();
            // ??????????????? uri ?????????????????????????????? filePath??????????????? filePath ?????????????????????????????????????????? uri ??????????????????????????????
            return uri + "." + lastModifyTime;
        } else {
            return uri;
        }
    }

    public static void postOnAnimation(@NonNull View view, @NonNull Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.postOnAnimation(runnable);
        } else {
            view.postDelayed(runnable, 1000 / 60);
        }
    }

    public static int getPointerIndex(int action) {
        return (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    }

    /**
     * Match MimeType
     *
     * @param template For example: application/*
     * @param mimeType For example: application/zip
     */
    public static boolean matchMimeType(@NonNull String template, @Nullable String mimeType) {
        String[] templateItems = template.split("/");
        String[] mimeItems = (mimeType != null ? mimeType : "").split("/");
        boolean result = true;
        if (templateItems.length > 0 && templateItems.length == mimeItems.length) {
            for (int index = 0; index < templateItems.length; index++) {
                String templateItem = templateItems[index].trim();
                String mimeItem = mimeItems[index].trim();
                result = "*".equals(templateItem) || templateItem.toLowerCase().equals(mimeItem.toLowerCase());
                if (!result) {
                    break;
                }
            }
        } else {
            result = false;
        }
        return result;
    }
}
