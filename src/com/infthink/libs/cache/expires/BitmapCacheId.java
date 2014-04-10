package com.infthink.libs.cache.expires;

import java.io.InputStream;

import android.content.res.Resources;
import android.util.Log;

import com.infthink.libs.network.HttpDirectMemoryDownload;

public class BitmapCacheId implements IExpiresCacheId<BitmapCacheable> {

    private static final String TAG = BitmapCacheId.class.getSimpleName();
    private final String CACHE_ID;
    private int mMaxWidth = -1;
    private int mMaxHeight = -1;
    private String mUrl;
    private int mResId;
    private Resources mResources;
    private String mPath;

    public BitmapCacheId(int maxWidth, int maxHeight, String path) {
        mPath = path;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        CACHE_ID = mPath + "|" + mMaxWidth + "x" + mMaxHeight;
    }

    public BitmapCacheId(String url, int maxWidth, int maxHeight) {
        mUrl = url;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        CACHE_ID = mUrl + "|" + mMaxWidth + "x" + mMaxHeight;
    }

    public BitmapCacheId(Resources resources, int resId, int maxWidth, int maxHeight) {
        mResources = resources;
        mResId = resId;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        CACHE_ID = mResId + "|" + mMaxWidth + "x" + mMaxHeight;
    }

    @Override
    public BitmapCacheable createCache() {
        if (DEBUG) {
            if (mPath != null) {
                Log.d(TAG, String.format("创建Bitmap缓存 path:%s, maxWidth:%s, maxHeight:%s", mPath, mMaxWidth, mMaxHeight));
            } else if (mUrl != null) {
                Log.d(TAG, String.format("创建Bitmap缓存 url:%s, maxWidth:%s, maxHeight:%s", mUrl, mMaxWidth, mMaxHeight));
            } else {
                Log.d(TAG, String.format("创建Bitmap缓存 resId:%s, maxWidth:%s, maxHeight:%s", mResId, mMaxWidth, mMaxHeight));
            }
        }
        BitmapCacheable instance = null;
        if (mPath != null) {
            instance = new BitmapCacheable(mPath, mMaxWidth, mMaxHeight);
        } else if (mUrl != null) {
            InputStream is = HttpDirectMemoryDownload.download(mUrl, null);
            if (is != null) {
                instance = new BitmapCacheable(is, mMaxWidth, mMaxHeight);
            }
        } else {
            instance = new BitmapCacheable(mResources, mResId, mMaxWidth, mMaxHeight);
        }
        if (instance != null && instance.getBitmap() != null) {
            return instance;
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((CACHE_ID == null) ? 0 : CACHE_ID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BitmapCacheId other = (BitmapCacheId) obj;
        if (CACHE_ID == null) {
            if (other.CACHE_ID != null)
                return false;
        } else if (!CACHE_ID.equals(other.CACHE_ID))
            return false;
        return true;
    }

}
