/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.redcdn.rom;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.CustomToast;

public class QikuUtils {
    private static final String TAG = "QikuUtils";


    /**
     * 检测 360 悬浮窗权限
     */
    public static boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 24); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return true;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(
                Context.APP_OPS_SERVICE);
            try {
                Class clazz = AppOpsManager.class;
                Method method = clazz.getDeclaredMethod("checkOp", int.class, int.class,
                    String.class);
                return AppOpsManager.MODE_ALLOWED ==
                    (int) method.invoke(manager, op, Binder.getCallingUid(),
                        context.getPackageName());
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else {
            Log.e("", "Below API 19 cannot invoke!");
        }
        return false;
    }


    /**
     * 去360权限申请页面
     */
    public static void goto360ApplyPermissionActivity(Context context) {
        CustomLog.i(TAG, "gotoHuaweiApplyPermissionActivity()");

        Intent intent = new Intent();
        intent.setClassName("com.android.settings",
            "com.android.settings.Settings$OverlaySettingsActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            if (isIntentAvailable(intent, context)) {
                context.startActivity(intent);
            } else {
                intent.setClassName("com.qihoo360.mobilesafe",
                    "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
                if (isIntentAvailable(intent, context)) {
                    context.startActivity(intent);
                } else {
                    Log.e(TAG, "can't open permission page with particular name, please use " +
                        "\"adb shell dumpsys activity\" command and tell me the name of the float window permission page");
                }
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "goto360ApplyPermissionActivity Exception : " + e.toString());
            CustomToast.show(context,
                context.getResources().getString(R.string.default_no_floating_window_permission_hint),
                CustomToast.LENGTH_LONG);
        }

    }


    private static boolean isIntentAvailable(Intent intent, Context context) {
        if (intent == null) {
            return false;
        }
        return context.getPackageManager()
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .size() > 0;
    }
}
