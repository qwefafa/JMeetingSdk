package cn.redcdn.rom;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.CustomToast;

/**
 * @author guoyx
 */

public class DefaultRomUtils {
    private static final String TAG = DefaultRomUtils.class.getSimpleName();


    /**
     * 检测 默认 Rom 悬浮窗权限
     */
    public static boolean checkDefaultRomFloatWindowPermission(Context context) {
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
            Log.e(TAG, "Below API 19 cannot invoke!");
        }
        return false;
    }


    public static void showDefaultOpenPermissonHint(Context mContext) {
        CustomLog.i(TAG, "showDefaultOpenPermissonHint()");

        CustomToast.show(mContext,
            mContext.getResources().getString(R.string.default_no_floating_window_permission_hint),
            CustomToast.LENGTH_LONG);
    }

}
