package com.infthink.libs.common.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

public class BitmapUtils implements IDebuggable {

    private static final String TAG = BitmapUtils.class.getSimpleName();

    /**
     * @param path 图片在本地的文件地址
     * @param maxWidth 限制最大的宽度, -1表示不限制
     * @param maxHeight 限制最大的高度, -1表示不限制
     * @return 如果解码失败，返回null
     */
    public static Bitmap decodeBitmap(String path, int maxWidth, int maxHeight) {
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
        if (bitmap == null && opts.outWidth > 0 && opts.outHeight > 0) {
            if (DEBUG) {
                Log.d(TAG, String.format("图片有效 url:%s, w:%s, h:%s, mw:%s, mh:%s", path, opts.outWidth, opts.outHeight, maxWidth, maxHeight));
            }
            // 文件有效
            opts.inSampleSize = 1;
            opts.inJustDecodeBounds = false;
            if ((maxWidth != -1 && maxWidth < opts.outWidth) || (maxHeight != -1 && maxHeight < opts.outHeight)) {
                float inSampleSizeWidth = opts.outWidth * 1.0f / maxWidth;
                float inSampleSizeHeight = opts.outHeight * 1.0f / maxHeight;
                float inSampleSize = Math.max(inSampleSizeWidth, inSampleSizeHeight);
                opts.inSampleSize = (int) Math.ceil(inSampleSize);
            }
            bitmap = BitmapFactory.decodeFile(path, opts);
        } else {
            if (DEBUG) {
                Log.d(TAG, String.format("图片无效 url:%s, w:%s, h:%s, mw:%s, mh:%s", path, opts.outWidth, opts.outHeight, maxWidth, maxHeight));
            }
        }
        if (bitmap == null) {
            if (DEBUG) {
                Log.d(TAG, String.format("解码图片失败 url:%s, w:%s, h:%s, mw:%s, mh:%s", path, opts.outWidth, opts.outHeight, maxWidth, maxHeight));
            }
        }
        return bitmap;
    }

    /**
     * @param inputStream 图片内容流
     * @param maxWidth 限制最大的宽度, -1表示不限制
     * @param maxHeight 限制最大的高度, -1表示不限制
     * @return 如果解码失败，返回null
     */
    public static Bitmap decodeBitmap(InputStream inputStream, int maxWidth, int maxHeight) {
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
        if (bitmap == null && opts.outWidth > 0 && opts.outHeight > 0) {
            if (DEBUG) {
                Log.d(TAG, String.format("图片有效 图片流, w:%s, h:%s, mw:%s, mh:%s", opts.outWidth, opts.outHeight, maxWidth, maxHeight));
            }
            // 文件有效
            opts.inSampleSize = 1;
            opts.inJustDecodeBounds = false;
            if ((maxWidth != -1 && maxWidth < opts.outWidth) || (maxHeight != -1 && maxHeight < opts.outHeight)) {
                float inSampleSizeWidth = opts.outWidth * 1.0f / maxWidth;
                float inSampleSizeHeight = opts.outHeight * 1.0f / maxHeight;
                float inSampleSize = Math.max(inSampleSizeWidth, inSampleSizeHeight);
                opts.inSampleSize = (int) Math.ceil(inSampleSize);
            }
            try {
                inputStream.reset();
            } catch (IOException e) {
                if (DEBUG)
                    e.printStackTrace();
            }
            bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
        } else {
            if (DEBUG) {
                Log.d(TAG, String.format("图片无效 图片流, w:%s, h:%s, mw:%s, mh:%s", opts.outWidth, opts.outHeight, maxWidth, maxHeight));
            }
        }
        if (bitmap == null) {
            if (DEBUG) {
                Log.d(TAG, String.format("解码图片失败 图片流, w:%s, h:%s, mw:%s, mh:%s", opts.outWidth, opts.outHeight, maxWidth, maxHeight));
            }
        }
        return bitmap;
    }

    /**
     * @param res
     * @param id 资源id {@code R.id.xxx}
     * @param maxWidth 限制对大的宽度, -1表示不限制
     * @param maxHeight 限制对大的高度, -1表示不限制
     * @return 如果解码失败，返回null
     */
    public static Bitmap decodeBitmap(Resources res, int id, int maxWidth, int maxHeight) {
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(res, id, opts);
        if (bitmap == null && opts.outWidth > 0 && opts.outHeight > 0) {
            if (DEBUG) {
                Log.d(TAG, String.format("图片有效 id:%s, w:%s, h:%s, mw:%s, mh:%s", id, opts.outWidth, opts.outHeight, maxWidth, maxHeight));
            }
            // 文件有效
            opts.inSampleSize = 1;
            opts.inJustDecodeBounds = false;
            if ((maxWidth != -1 && maxWidth < opts.outWidth) || (maxHeight != -1 && maxHeight < opts.outHeight)) {
                float inSampleSizeWidth = opts.outWidth * 1.0f / maxWidth;
                float inSampleSizeHeight = opts.outHeight * 1.0f / maxHeight;
                float inSampleSize = Math.max(inSampleSizeWidth, inSampleSizeHeight);
                opts.inSampleSize = (int) Math.ceil(inSampleSize);
            }
            bitmap = BitmapFactory.decodeResource(res, id, opts);
        } else {
            if (DEBUG) {
                Log.d(TAG, String.format("图片无效 id:%s, w:%s, h:%s, mw:%s, mh:%s", id, opts.outWidth, opts.outHeight, maxWidth, maxHeight));
            }
        }
        if (bitmap == null) {
            if (DEBUG) {
                Log.d(TAG, String.format("解码图片失败 id:%s, w:%s, h:%s, mw:%s, mh:%s", id, opts.outWidth, opts.outHeight, maxWidth, maxHeight));
            }
        }
        return bitmap;
    }

}
