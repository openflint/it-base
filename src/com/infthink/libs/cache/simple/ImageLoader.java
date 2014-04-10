package com.infthink.libs.cache.simple;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.infthink.libs.base.BaseApplication;
import com.infthink.libs.cache.expires.BitmapCacheId;
import com.infthink.libs.cache.expires.BitmapCacheable;
import com.infthink.libs.common.os.AsyncFiloTask;
import com.infthink.libs.common.utils.SystemUtils;

public class ImageLoader {

    private static final int TAG_IMAGE_URL = 0x5f130000;
    private static final int TAG_TASK = 0x5f130001;
    private static final String TAG = ImageLoader.class.getSimpleName();
    private static HandlerThread HANDLER_THREAD = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
    static {
        HANDLER_THREAD.start();
    }
    private static Handler sUIHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            MSG_ obj = (MSG_) msg.obj;
            ImageView imageView = obj.refView.get();
            if (imageView != null) {
                if (obj.imageUrl.equals(imageView.getTag(TAG_IMAGE_URL))) {
                    imageView.setImageBitmap(obj.bitmap);
                }
            }
        }

    };
    private static Handler sHandler = new Handler(HANDLER_THREAD.getLooper()) {

        private long mLastTime;

        @Override
        public void handleMessage(Message msg) {
            MSG_ obj = (MSG_) msg.obj;
            if (obj.refView.get() == null) {
                return;
            }
            sUIHandler.sendMessage(sUIHandler.obtainMessage(0, obj));
            long currentTime = System.currentTimeMillis();
            long sleep = currentTime - mLastTime;
            sleep = Math.min(sleep, 100);
            mLastTime = System.currentTimeMillis();
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    };

    public static void loadImage(ImageView imageView, final String imagePath, final BitmapCachePool cachePool) {
        if (imagePath == null) {
            return;
        }
        imageView.setTag(TAG_IMAGE_URL, imagePath);
        final WeakReference<ImageView> refView = new WeakReference<ImageView>(imageView);
        AsyncFiloTask<Void, Void, Bitmap> task = new AsyncFiloTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                ImageView imageView = refView.get();
                if (imageView != null && imagePath.equals(imageView.getTag(TAG_IMAGE_URL))) {
                    Bitmap bitmap = getBitmap(imagePath, cachePool);
                    imageView = refView.get();
                    if (imageView != null && bitmap != null) {
                        if (imagePath.equals(imageView.getTag(TAG_IMAGE_URL))) {
                            MSG_ obj = new MSG_();
                            obj.bitmap = bitmap;
                            obj.imageUrl = imagePath;
                            obj.refView = refView;
                            sHandler.sendMessage(sHandler.obtainMessage(0, obj));
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "WeakReference 对 ImageView 的引用为null");
                        }
                    }
                }
                return null;
            }

        };
        imageView.setTag(TAG_TASK, task);
        task.execute();
    }

    public static void loadImage(final BitmapCachePool cachePool, ImageView imageView, final String imageUrl) {
        if (imageUrl == null) {
            return;
        }
        imageView.setTag(TAG_IMAGE_URL, imageUrl);
        final WeakReference<ImageView> refView = new WeakReference<ImageView>(imageView);
        AsyncFiloTask<Void, Void, Bitmap> task = new AsyncFiloTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                ImageView imageView = refView.get();
                if (imageView != null && imageUrl.equals(imageView.getTag(TAG_IMAGE_URL))) {
                    Bitmap bitmap = getBitmap(cachePool, imageUrl);
                    imageView = refView.get();
                    if (imageView != null && bitmap != null) {
                        if (imageUrl.equals(imageView.getTag(TAG_IMAGE_URL))) {
                            MSG_ obj = new MSG_();
                            obj.bitmap = bitmap;
                            obj.imageUrl = imageUrl;
                            obj.refView = refView;
                            sHandler.sendMessage(sHandler.obtainMessage(0, obj));
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "WeakReference 对 ImageView 的引用为null");
                        }
                    }
                }
                return null;
            }

        };
        imageView.setTag(TAG_TASK, task);
        task.execute();
    }

    private static Bitmap getBitmap(BitmapCachePool cachePool, String imageUrl) {
        DisplayMetrics displayMetrics = SystemUtils.getDisplayMetrics(BaseApplication.getInstance());
        BitmapCacheId cacheId = new BitmapCacheId(imageUrl, displayMetrics.widthPixels, displayMetrics.heightPixels);
        BitmapCacheable cacheable = cachePool.getCache(cacheId);
        
        return cacheable == null ? null : cacheable.getBitmap();
    }

    private static Bitmap getBitmap(String path, BitmapCachePool cachePool) {
        DisplayMetrics displayMetrics = SystemUtils.getDisplayMetrics(BaseApplication.getInstance());
        BitmapCacheId cacheId = new BitmapCacheId(displayMetrics.widthPixels, displayMetrics.heightPixels, path);
        BitmapCacheable cacheable = cachePool.getCache(cacheId);
        
        return cacheable == null ? null : cacheable.getBitmap();
    }

    static class MSG_ {
        Bitmap bitmap;
        WeakReference<ImageView> refView;
        String imageUrl;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        HANDLER_THREAD.quit();
    }

}
