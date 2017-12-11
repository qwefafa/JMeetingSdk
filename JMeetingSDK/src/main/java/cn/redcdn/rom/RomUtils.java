/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.redcdn.rom;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cn.redcdn.log.CustomLog;

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-05-23
 */
public class RomUtils {
    private static final String TAG = "RomUtils";


    /**
     * 获取 emui 版本号
     */
    public static double getEmuiVersion() {
        try {
            String emuiVersion = getSystemProperty("ro.build.version.emui");
            String version = emuiVersion.substring(emuiVersion.indexOf("_") + 1);
            return Double.parseDouble(version);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 4.0;
    }


    /**
     * 获取小米 rom 版本号，获取失败返回 -1
     *
     * @return miui rom version code, if fail , return -1
     */
    public static int getMiuiVersion() {
        String version = getSystemProperty("ro.miui.ui.version.name");
        if (version != null) {
            try {
                return Integer.parseInt(version.substring(1));
            } catch (Exception e) {
                Log.e(TAG, "get miui version code error, version : " + version);
            }
        }
        return -1;
    }


    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read sysprop " + propName, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }


    public static boolean checkIsHuaweiRom() {
        CustomLog.i(TAG, "checkIsHuaweiRom()");
        boolean isHuaweiRom = Build.MANUFACTURER.contains("HUAWEI");
        CustomLog.d(TAG, "If the target rom belongs to Huawei : " + isHuaweiRom);
        return isHuaweiRom;
    }


    /**
     * check if is miui ROM
     */
    public static boolean checkIsMiuiRom() {
        CustomLog.i(TAG, "checkIsMiuiRom()");
        boolean isMiuiRom = !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
        CustomLog.d(TAG, "If the target rom belongs to MIUI : " + isMiuiRom);
        return isMiuiRom;
    }


    public static boolean checkIsMeizuRom() {
        CustomLog.i(TAG, "checkIsMeizuRom()");
        String meizuFlymeOSFlag = getSystemProperty("ro.build.display.id");
        if (TextUtils.isEmpty(meizuFlymeOSFlag)) {
            CustomLog.d(TAG, "If the target rom belongs to Meizu : " + false);
            return false;
        } else if (meizuFlymeOSFlag.contains("flyme") ||
            meizuFlymeOSFlag.toLowerCase().contains("flyme")) {
            CustomLog.d(TAG, "If the target rom belongs to Meizu : " + true);
            return true;
        } else {
            CustomLog.d(TAG, "If the target rom belongs to Meizu : " + false);
            return false;
        }
    }


    public static boolean checkIs360Rom() {
        CustomLog.i(TAG, "checkIs360Rom()");

        boolean is360Rom = Build.MANUFACTURER.contains("QiKU")
            || Build.MANUFACTURER.contains("360");
        CustomLog.d(TAG, "If the target rom belongs to 360 : " + is360Rom);
        return is360Rom;
    }
}
