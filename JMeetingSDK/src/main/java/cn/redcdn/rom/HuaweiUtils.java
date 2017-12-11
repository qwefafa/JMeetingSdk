/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.redcdn.rom;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.CustomToast;

public class HuaweiUtils {
    private static final String TAG = "HuaweiUtils";


    /**
     * 检测 Huawei 悬浮窗权限
     */
    public static boolean checkFloatWindowPermission(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 24); //OP_SYSTEM_ALERT_WINDOW = 24;
        }
        return true;
    }


    /**
     * 去华为权限申请页面
     */
    public static void gotoHuaweiApplyPermissionActivity(Context context) {
        CustomLog.i(TAG, "gotoHuaweiApplyPermissionActivity()");

        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager",
                "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");//悬浮窗管理页面
            intent.setComponent(comp);
            if (RomUtils.getEmuiVersion() == 3.1) {
                //emui 3.1 的适配
                context.startActivity(intent);
                CustomLog.d(TAG, "Go to Huawei EMUI 3.1 gotoMeizuApplyPermissionActivity activity");
            } else {
                //emui 3.0 的适配
                comp = new ComponentName("com.huawei.systemmanager",
                    "com.huawei.notificationmanager.ui.NotificationManagmentActivity");//悬浮窗管理页面
                intent.setComponent(comp);
                context.startActivity(intent);
                CustomLog.d(TAG, "Go to Huawei EMUI 3.0 gotoMeizuApplyPermissionActivity activity");
            }
        } catch (SecurityException e) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager",
                "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理，跳转到本app的权限管理页面,这个需要华为接口权限，未解决
            intent.setComponent(comp);
            context.startActivity(intent);

            CustomLog.d(TAG, "Now in Huawei SecurityException branch");
            CustomLog.e(TAG, Log.getStackTraceString(e));
        } catch (ActivityNotFoundException e) {
            /**
             * 手机管家版本较低 HUAWEI SC-UL10
             */
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.Android.settings",
                "com.android.settings.permission.TabItem");//权限管理页面 android4.4
            intent.setComponent(comp);
            context.startActivity(intent);

            CustomLog.d(TAG, "Now in Huawei ActivityNotFoundException branch");
            CustomLog.e(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (Exception e) {
            CustomLog.e(TAG, Log.getStackTraceString(e));
            CustomToast.show(context,
                context.getResources().getString(R.string.default_no_floating_window_permission_hint),
                CustomToast.LENGTH_LONG);
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
}

