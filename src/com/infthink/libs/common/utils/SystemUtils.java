package com.infthink.libs.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * 系统信息
 */
public class SystemUtils implements IDebuggable {

    private static final String TAG = SystemUtils.class.getSimpleName();

    public static LocationManager getLocationManager(Context context) {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager;
    }

    /**
     * 获得现实屏幕相关的信息
     * @param context
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = null;
        try {
            displayMetrics = context.getResources().getDisplayMetrics();
        } catch (Exception e) {
            if (DEBUG) {
                Log.d(TAG, "从Resources中获取DisplayMetrics失败，尝试从WindowManager中读取");
                e.printStackTrace();
            }
        }
        if (displayMetrics == null) {
            displayMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        }
        return displayMetrics;
    }

    public static TelephonyManager getTelephonyManager(Context context) {
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 读取指定应用的packageInfo
     * @param context
     * @param packageName
     * @param flags
     * @return
     */
    public static PackageInfo getPackageInfo(Context context, String packageName, int flags) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, flags);
        } catch (NameNotFoundException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return packageInfo;
    }

    /**
     * 读取当前应用的packageInfo
     * @param context
     * @return
     */
    public static PackageInfo getPackageInfo(Context context) {
        return getPackageInfo(context, context.getPackageName(), 0);
    }

    public static class BuildInfo extends Build {

        private BuildInfo() {
            //
        }

    }

}
