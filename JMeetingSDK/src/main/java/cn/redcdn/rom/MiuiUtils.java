/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.redcdn.rom;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;

import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.CustomToast;

public class MiuiUtils {
    private static final String TAG = "MiuiUtils";


    /**
     * 获取小米 rom 版本号，获取失败返回 -1
     *
     * @return miui rom version code, if fail , return -1
     */
    public static int getMiuiVersion() {
        String version = RomUtils.getSystemProperty("ro.miui.ui.version.name");
        if (version != null) {
            try {
                return Integer.parseInt(version.substring(1));
            } catch (Exception e) {
                Log.e(TAG, "get miui version code error, version : " + version);
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return -1;
    }


    /**
     * 检测 miui 悬浮窗权限
     */
    public static boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;

        if (version >= 19) {
            boolean isPermissionGranted = checkOp(context, 24); //OP_SYSTEM_ALERT_WINDOW = 24;
            CustomLog.d(TAG, "MIUI checkFloatWindowPermission result : " + isPermissionGranted);
            return isPermissionGranted;
        } else {
            return true;
        }
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
            Log.e(TAG, "Below API 19 cannot invoke!");
        }
        return false;
    }


    /**
     * 小米 ROM 权限申请
     */
    public static void applyMiuiPermission(Context context) {
        CustomLog.i(TAG, "applyMiuiPermission()");

        int versionCode = getMiuiVersion();
        if (versionCode == 5) {
            goToMiuiPermissionActivity_V5(context);
        } else if (versionCode == 6) {
            goToMiuiPermissionActivity_V6(context);
        } else if (versionCode == 7) {
            goToMiuiPermissionActivity_V7(context);
        } else if (versionCode == 8) {
            goToMiuiPermissionActivity_V8(context);
        } else {
            CustomLog.e(TAG, "this is a special MIUI rom version, its version code " + versionCode);
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


    /**
     * 小米 V5 版本 ROM权限申请
     */
    public static void goToMiuiPermissionActivity_V5(Context context) {
        CustomLog.i(TAG, "goToMiuiPermissionActivity_V5()");

        Intent intent = null;
        String packageName = context.getPackageName();
        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        jumpToMiuiPermissionActivity_V5(intent, context);
    }


    public static void jumpToMiuiPermissionActivity_V5(Intent intent, Context context) {
        CustomLog.i(TAG, "jumpToMiuiPermissionActivity_V5()");

        try {
            if (isIntentAvailable(intent, context)) {
                context.startActivity(intent);
            } else {
                CustomLog.e(TAG, "intent is not available!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustomLog.e(TAG, "goToMiuiPermissionActivity_V5() Exception" + e.toString());
            CustomToast.show(context,
                context.getResources()
                    .getString(R.string.default_no_floating_window_permission_hint),
                CustomToast.LENGTH_LONG);
        }
    }


    /**
     * 小米 V6 版本 ROM权限申请
     */
    public static void goToMiuiPermissionActivity_V6(Context context) {
        CustomLog.i(TAG, "goToMiuiPermissionActivity_V6()");

        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter",
            "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        jumpToMiuiPermissionActivity_V5(intent, context);

    }


    /**
     * 小米 V7 版本 ROM权限申请
     */
    public static void goToMiuiPermissionActivity_V7(Context context) {
        CustomLog.i(TAG, "goToMiuiPermissionActivity_V7()");

        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter",
            "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        jumpToMiuiPermissionActivity_V5(intent, context);

    }


    /**
     * 小米 V8 版本 ROM权限申请
     */
    public static void goToMiuiPermissionActivity_V8(Context context) {
        CustomLog.i(TAG, "goToMiuiPermissionActivity_V8()");

        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter",
            "com.miui.permcenter.permissions.PermissionsEditorActivity");
        intent.putExtra("extra_pkgname", context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            if (isIntentAvailable(intent, context)) {
                context.startActivity(intent);
            } else {
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setPackage("com.miui.securitycenter");
                intent.putExtra("extra_pkgname", context.getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (isIntentAvailable(intent, context)) {
                    context.startActivity(intent);
                } else {
                    CustomLog.e(TAG, "Intent is not available!");
                }
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "goToMiuiPermissionActivity_V8() Exception : " + e.toString());
            CustomToast.show(context,
                context.getResources().getString(R.string.default_no_floating_window_permission_hint),
                CustomToast.LENGTH_LONG);
        }

    }
}
