package com.infthink.libs.cache.expires;

import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.infthink.libs.common.utils.BitmapUtils;

/**
 * Bitmap缓存对象
 */
public class BitmapCacheable implements IExpiresCacheable {

    private Bitmap mBitmap;

    /**
     * @param is
     * @param maxWidth decode图片的最大宽度，-1表示不限制
     * @param maxHeight decode图片的最大高度，-1表示不限制
     */
    public BitmapCacheable(InputStream is, int maxWidth, int maxHeight) {
        mBitmap = BitmapUtils.decodeBitmap(is, maxWidth, maxHeight);
    }

    /**
     * @param path 本地文件地址
     * @param maxWidth decode图片的最大宽度，-1表示不限制
     * @param maxHeight decode图片的最大高度，-1表示不限制
     */
    public BitmapCacheable(String path, int maxWidth, int maxHeight) {
        mBitmap = BitmapUtils.decodeBitmap(path, maxWidth, maxHeight);
    }

    /**
     * @param resId
     * @param maxWidth decode图片的最大宽度，-1表示不限制
     * @param maxHeight decode图片的最大高度，-1表示不限制
     */
    public BitmapCacheable(Resources resources, int resId, int maxWidth, int maxHeight) {
        mBitmap = BitmapUtils.decodeBitmap(resources, resId, maxWidth, maxHeight);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public int getCacheSize() {
        return mBitmap == null ? 0 : mBitmap.getByteCount();
    }

    @Override
    public boolean isExpires() {
        return false;
    }

}
