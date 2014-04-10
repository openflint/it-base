package com.infthink.libs.cache.simple;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.infthink.libs.base.BaseApplication;
import com.infthink.libs.cache.expires.BitmapCacheId;
import com.infthink.libs.cache.expires.BitmapCacheable;
import com.infthink.libs.common.os.AsyncFiloTask;
import com.infthink.libs.common.utils.SystemUtils;

public class BackgroundLoader {

    private static final int TAG_IMAGE_URL = 0x5f131005;
    private static final int TAG_TASK = 0x5f131006;
    private static final String TAG = BackgroundLoader.class.getSimpleName();

    public static void loadImage(final BitmapCachePool cachePool, View view, final String imageUrl) {
        if (imageUrl == null) {
            return;
        }
        view.setTag(TAG_IMAGE_URL, imageUrl);
        final WeakReference<View> refView = new WeakReference<View>(view);
        AsyncFiloTask<Void, Void, Bitmap> task = new AsyncFiloTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                View view = refView.get();
                if (view != null && imageUrl.equals(view.getTag(TAG_IMAGE_URL))) {
                    return getBitmap(cachePool, imageUrl);
                } else {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null) {
                    View view = refView.get();
                    if (view != null) {
                        if (imageUrl.equals(view.getTag(TAG_IMAGE_URL))) {
                            result = result.copy(Bitmap.Config.ARGB_8888, true);
                            Canvas canvas = new Canvas(result);
                            canvas.drawARGB(0xe0, 0, 0, 0);
                            GradientDrawable drawable = new GradientDrawable(Orientation.TOP_BOTTOM, new int[] { 0x00000000, 0xff000000 });
                            drawable.draw(canvas);
                            view.setBackgroundDrawable(new BitmapDrawable(view.getResources(), result));
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "WeakReference 对 ImageView 的引用为null");
                        }
                    }
                }
            }

        };
        view.setTag(TAG_TASK, task);
        task.execute();
    }

    private static Bitmap getBitmap(BitmapCachePool cachePool, String imageUrl) {
        DisplayMetrics displayMetrics = SystemUtils.getDisplayMetrics(BaseApplication.getInstance());
        BitmapCacheId cacheId = new BitmapCacheId(imageUrl, displayMetrics.widthPixels, displayMetrics.heightPixels);
        BitmapCacheable cacheable = cachePool.getCache(cacheId);
        return cacheable == null ? null : cacheable.getBitmap();
    }

}
