package com.infthink.libs.cache.expires;

import java.io.InputStream;

import android.util.Log;

import com.infthink.libs.common.utils.IOUtils;

/**
 * 文本缓存对象
 */
public class TextCacheable implements IExpiresCacheable {

    private static final String TAG = TextCacheable.class.getSimpleName();
    private static final long TIME_OUT = 5 * 60000;
    private long mDateExpires;
    private String mText;

    public TextCacheable(InputStream is) {
        mText = IOUtils.readString(is);
        mDateExpires = System.currentTimeMillis() + TIME_OUT;
    }

    public String getText() {
        return mText;
    }

    @Override
    public int getCacheSize() {
        return mText == null ? 0 : mText.getBytes().length;
    }

    @Override
    public boolean isExpires() {
        long expiresTime = System.currentTimeMillis() - mDateExpires;
        boolean expires = expiresTime > 0;
        if (DEBUG && expires)
            Log.d(TAG, String.format("缓存过期 TIME_OUT:%s, expires time:%s", new Object[] { TIME_OUT, expiresTime }));
        return expires;
    }

}
