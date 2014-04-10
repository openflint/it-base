package com.infthink.libs.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkUtils implements IDebuggable {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static boolean isNetWorkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] nis = cm.getAllNetworkInfo();
        if (nis != null) {
            for (NetworkInfo ni : nis) {
                if (ni != null) {
                    if (ni.isConnected()) {
                        if (DEBUG) {
                            Log.d(TAG, "发现了一个网络链接 " + ni.getTypeName());
                        }
                        return true;
                    }
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "没有发现任何可用的网络链接");
        }
        return false;
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean isWifiEnable = wifiMgr.isWifiEnabled();
        if (DEBUG) {
            if (isWifiEnable) {
                Log.d(TAG, "wifi服务可用");
            } else {
                Log.d(TAG, "wifi服务不可用");
            }
        }
        return isWifiEnable;
    }

}
