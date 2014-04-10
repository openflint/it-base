package com.infthink.libs.cache.simple;

import java.lang.ref.WeakReference;

import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.infthink.libs.cache.expires.TextCacheId;
import com.infthink.libs.cache.expires.TextCacheable;
import com.infthink.libs.common.os.AsyncFiloTask;

public class TextLoader {

    private static final int TAG_TEXT_URL = 0x5f230000;
    private static final int TAG_TASK = 0x5f230001;
    private static final String TAG = TextLoader.class.getSimpleName();

    public static interface ITextLoadListener<T> {

        /**
         * 非UI方法
         * @param text
         */
        public T parseText(String text);

        /**
         * UI方法
         */
        public void onLoadResult(T object);

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

    public static class SimpleTextLoadListener<T> implements ITextLoadListener<T> {

        private SparseArray<Object> mKeyedTags;

        @Override
        public T parseText(String text) {
            throw new UnsupportedOperationException("子类应该重写此方法");
        }

        @Override
        public void onLoadResult(T object) {
            throw new UnsupportedOperationException("子类应该重写此方法");
        }

        @Override
        public final void setTag(int key, final Object tag) {
            // If the package id is 0x00 or 0x01, it's either an undefined package
            // or a framework id
            if ((key >>> 24) < 2) {
                throw new IllegalArgumentException("The key must be an application-specific "
                        + "resource id.");
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
            if (mKeyedTags != null) return mKeyedTags.get(key);
            return null;
        }

    }

    public static <T> void loadText(final TextCachePool cachePool, ITextLoadListener<T> listener, final String textUrl) {
        if (textUrl == null) {
            return;
        }
        listener.setTag(TAG_TEXT_URL, textUrl);
        final WeakReference<ITextLoadListener<T>> refListener = new WeakReference<ITextLoadListener<T>>(listener);
        AsyncFiloTask<Void, Void, T> task = new AsyncFiloTask<Void, Void, T>() {

            @Override
            protected T doInBackground(Void... params) {
                ITextLoadListener<T> listener = refListener.get();
                if (listener != null && textUrl.equals(listener.getTag(TAG_TEXT_URL))) {
                    String text = getText(cachePool, textUrl);
                    if (text != null) {
                        return listener.parseText(text);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(T result) {
                ITextLoadListener<T> listener = refListener.get();
                if (listener != null) {
                    if (textUrl.equals(listener.getTag(TAG_TEXT_URL))) {
                        listener.onLoadResult(result);
                    }
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "WeakReference 对 ITextLoadListener 的引用为null");
                    }
                }
            }

        };
        listener.setTag(TAG_TASK, task);
        task.execute();
    }

    private static String getText(TextCachePool cachePool, String httpUrl) {
        TextCacheId cacheId = new TextCacheId(httpUrl);
        TextCacheable cacheable = cachePool.getCache(cacheId);
        return cacheable == null ? null : cacheable.getText();
    }

}
