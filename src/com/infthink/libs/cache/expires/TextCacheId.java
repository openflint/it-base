package com.infthink.libs.cache.expires;

import java.io.InputStream;

import android.util.Log;

import com.infthink.libs.network.HttpDirectMemoryDownload;

public class TextCacheId implements IExpiresCacheId<TextCacheable> {

    private static final String TAG = TextCacheId.class.getSimpleName();
    private String mUrl;

    public TextCacheId(String httpUrl) {
        mUrl = httpUrl;
    }

    @Override
    public TextCacheable createCache() {
        if (DEBUG) {
            Log.d(TAG, String.format("创建Text缓存 url:%s", mUrl));
        }
        TextCacheable instance = null;
        InputStream is = HttpDirectMemoryDownload.download(mUrl, null);
        if (is != null) {
            instance = new TextCacheable(is);
        }
        if (instance != null && instance.getText() != null) {
            return instance;
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mUrl == null) ? 0 : mUrl.hashCode());
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
        TextCacheId other = (TextCacheId) obj;
        if (mUrl == null) {
            if (other.mUrl != null)
                return false;
        } else if (!mUrl.equals(other.mUrl))
            return false;
        return true;
    }

}
