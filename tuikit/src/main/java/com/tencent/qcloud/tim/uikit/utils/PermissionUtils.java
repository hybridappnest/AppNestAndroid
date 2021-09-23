package com.tencent.qcloud.tim.uikit.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.ymy.core.permission.DBXPermissionUtils;
import com.ymy.core.permission.PermissionCallback;
import com.ymy.core.utils.ToastUtils;

import java.util.ArrayList;

import static com.ymy.core.permission.DBXPermissionUtilsKt.requestPermission;

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();

    private static AlertDialog mPermissionDialog;

    public static boolean checkPermission(final Context context, final String permission) {
        TUIKitLog.i(TAG, "checkPermission permission:" + permission + "|sdk:" + Build.VERSION.SDK_INT);
        boolean flag = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ActivityCompat.checkSelfPermission(context, permission);
            if (PackageManager.PERMISSION_GRANTED != result) {
                new DBXPermissionUtils(context).permission("使用该功能，需要开启权限，鉴于您禁用相关权限，请手动设置开启权限", new String[]{permission})
                        .callback(new PermissionCallback() {
                            @Override
                            public void onPermissionGranted() {

                            }

                            @Override
                            public void onPermissionsDenied() {
//                                showPermissionDialog(context, permission);
                            }
                        }).request();
                flag = false;
            }
        }
        return flag;
    }

    private static void showPermissionDialog(final Context context, String permission) {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(context)
                    .setMessage("使用该功能，需要开启权限，鉴于您禁用相关权限，请手动设置开启权限")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            Uri packageURI = Uri.parse("package:" + context.getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    private static void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }
}
