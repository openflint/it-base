package com.infthink.libs.cache.simple;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.infthink.libs.base.BaseApplication;
import com.infthink.libs.cache.expires.BitmapCacheId;
import com.infthink.libs.cache.expires.BitmapCacheable;
import com.infthink.libs.common.os.AsyncFiloTask;
import com.infthink.libs.common.utils.SystemUtils;

public class BitmapLoader {

    private static final int TAG_IMAGE_URL = 0x5f130000;
    private static final int TAG_TASK = 0x5f130001;
    private static final String TAG = BitmapLoader.class.getSimpleName();

    public static interface IBitmapLoadListener {

        public void onLoad(Bitmap bitmap);

        /**
         * @see View#setTag(int, Object)
         * @param key
         * @param tag
         */
        public void setTag(int key, Object tag);

        /**
         * @see View#getTag(int)
         * @param key
         * @return
         */
        public Object getTag(int key);

    }

    public static class SimpleBitmapLoadListener implements IBitmapLoadListener {

        private SparseArray<Object> mKeyedTags;

        @Override
        public void onLoad(Bitmap bitmap) {
            throw new UnsupportedOperationException("子类应该重写此方法");
        }

        @Override
        public final void setTag(int key, final Object tag) {
            // If the package id is 0x00 or 0x01, it's either an undefined package
            // or a framework id
            if ((key >>> 24) < 2) {
                throw new IllegalArgumentException("The key must be an application-specific " + "resource id.");
            }

            setKeyedTag(key, tag);
        }

        private void setKeyedTag(int key, Object tag) {
            if (mKeyedTags == null) {
                mKeyedTags = new SparseArray<Object>();
            }
            mKeyedTags.put(key, tag);
        }

        @Override
        public final Object getTag(int key) {
            if (mKeyedTags != null)
                return mKeyedTags.get(key);
            return null;
        }

    }

    /**
     * 
     * @param context
     * @param cachePool
     * @param listener 内部只持有对listener的弱引用
     * @param imageUrl
     * @param httpParams
     */
    public static void loadBitmap(final BitmapCachePool cachePool, IBitmapLoadListener listener, final String imageUrl) {
        if (imageUrl == null) {
            return;
        }
        listener.setTag(TAG_IMAGE_URL, imageUrl);
        final WeakReference<IBitmapLoadListener> refListener = new WeakReference<IBitmapLoadListener>(listener);
        AsyncFiloTask<Void, Void, Bitmap> task = new AsyncFiloTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                IBitmapLoadListener listener = refListener.get();
                if (listener != null && imageUrl.equals(listener.getTag(TAG_IMAGE_URL))) {
                    return getBitmap(cachePool, imageUrl);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null) {
                    IBitmapLoadListener listener = refListener.get();
                    if (listener != null) {
                        if (imageUrl.equals(listener.getTag(TAG_IMAGE_URL))) {
                            listener.onLoad(result);
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "WeakReference 对 IBitmapLoadListener 的引用为null");
                        }
                    }
                }
            }

        };
        listener.setTag(TAG_TASK, task);
        task.execute();
    }

    /**
     * 
     * @param context
     * @param cachePool
     * @param listener 内部只持有对listener的弱引用
     * @param imageUrl
     * @param httpParams
     */
    public static void loadBitmap(final BitmapCachePool cachePool, IBitmapLoadListener listener, final Resources resources, final Integer resId) {
        if (resources == null || resId == null) {
            return;
        }
        listener.setTag(TAG_IMAGE_URL, resId);
        final WeakReference<IBitmapLoadListener> refListener = new WeakReference<IBitmapLoadListener>(listener);
        AsyncFiloTask<Void, Void, Bitmap> task = new AsyncFiloTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                IBitmapLoadListener listener = refListener.get();
                if (listener != null && resId.equals(listener.getTag(TAG_IMAGE_URL))) {
                    return getBitmap(cachePool, resources, resId);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null) {
                    IBitmapLoadListener listener = refListener.get();
                    if (listener != null) {
                        if (resId.equals(listener.getTag(TAG_IMAGE_URL))) {
                            listener.onLoad(result);
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "WeakReference 对 IBitmapLoadListener 的引用为null");
                        }
                    }
                }
            }

        };
        listener.setTag(TAG_TASK, task);
        task.execute();
    }

    private static Bitmap getBitmap(BitmapCachePool cachePool, String imageUrl) {
        DisplayMetrics displayMetrics = SystemUtils.getDisplayMetrics(BaseApplication.getInstance());
        BitmapCacheId cacheId = new BitmapCacheId(imageUrl, displayMetrics.widthPixels, displayMetrics.heightPixels);
        BitmapCacheable cacheable = cachePool.getCache(cacheId);
        return cacheable == null ? null : cacheable.getBitmap();
    }

    private static Bitmap getBitmap(BitmapCachePool cachePool, Resources resources, int resId) {
        BitmapCacheId cacheId = new BitmapCacheId(resources, resId, 1000, 1000);
        BitmapCacheable cacheable = cachePool.getCache(cacheId);
        return cacheable == null ? null : cacheable.getBitmap();
    }

}
