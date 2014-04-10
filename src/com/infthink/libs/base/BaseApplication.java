package com.infthink.libs.base;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;

import android.app.Application;
import android.net.http.HttpResponseCache;
import android.util.Log;

import com.infthink.libs.common.message.MessageManager;
import com.infthink.libs.common.utils.IDebuggable;

public abstract class BaseApplication extends Application implements IDebuggable {

    private static final String TAG = BaseApplication.class.getSimpleName();
    private static BaseApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        try {
            File externalCacheDir = getExternalCacheDir();
            if (externalCacheDir != null) {
                File httpCacheDir = new File(externalCacheDir, "http");
                long httpCacheSize = 300 * 1024 * 1024; // 300 MiB
                HttpResponseCache.install(httpCacheDir, httpCacheSize);
            }
        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "HTTP response cache 安装失败", e);
        }

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        MessageManager.init(this);

        // 监听sdcard调整HttpResponseCache
    }

    public static BaseApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
        MessageManager.close();
    }

}
