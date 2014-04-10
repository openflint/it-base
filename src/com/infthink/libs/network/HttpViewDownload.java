package com.infthink.libs.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.util.Log;
import android.view.View;

import com.infthink.libs.common.utils.IDebuggable;
import com.infthink.libs.common.utils.IOUtils;
import com.infthink.libs.network.HttpDownload.IOnAddRequestProperties;
import com.infthink.libs.network.HttpDownload.IOnErrorInput;

public class HttpViewDownload implements IDebuggable {

    private static final String TAG = HttpViewDownload.class.getSimpleName();
    private static final int TAG_URL = 0x7f060100;

    public static void setTag(View view, Object tag) {
        view.setTag(TAG_URL, tag);
    }

    public static boolean isTagEquals(View view, Object tag) {
        return tag.equals(view.getTag(TAG_URL));
    }

    public static Object getTag(View view) {
        return view.getTag(TAG_URL);
    }

    /**
     * @param view 不能为null
     * @param httpUrl 不能为null
     * @param onAddRequestProperties
     * @param onHttpViewDownload
     */
    public static void download(final View view, final String httpUrl, IOnAddRequestProperties onAddRequestProperties, final IOnHttpViewDownload onHttpViewDownload) {
        setTag(view, httpUrl);
        HttpDownload.download(HttpDownload.METHOD_GET, httpUrl, onAddRequestProperties, true, null, new HttpDownload.IOnDoInput() {
            @Override
            public void onDoInput(HttpURLConnection connection, InputStream is) {
                int contentLength = connection.getContentLength();
                if (DEBUG)
                    Log.d(TAG, String.format("网络返回内容长度:%s, url:%s", new Object[] { contentLength, httpUrl }));
                // check tag
                if (!isTagEquals(view, httpUrl))
                    return;

                ByteArrayOutputStream baos = null;
                ByteArrayInputStream bais = null;
                try {
                    baos = new ByteArrayOutputStream(16 * 1024);
                } catch (Exception e) {
                    if (DEBUG)
                        e.printStackTrace();
                }
                // check tag
                if (!isTagEquals(view, httpUrl))
                    return;

                if (baos == null) {
                    if (DEBUG)
                        Log.d(TAG, String.format("创建ByteArrayOutputStream失败 %s", new Object[] { httpUrl }));
                    if (onHttpViewDownload != null) {
                        onHttpViewDownload.onHttpViewDownload(httpUrl, view, null);
                    }
                    return;
                }
                try {
                    byte[] step = new byte[16 * 1024];
                    int read = -1;
                    while ((read = is.read(step)) != -1) {
                        // check tag
                        if (!isTagEquals(view, httpUrl))
                            return;

                        baos.write(step, 0, read);
                    }
                    bais = new ByteArrayInputStream(baos.toByteArray());
                    // check tag
                    if (!isTagEquals(view, httpUrl))
                        return;

                    if (onHttpViewDownload != null) {
                        onHttpViewDownload.onHttpViewDownload(httpUrl, view, bais);
                    }
                } catch (IOException e) {
                    if (DEBUG)
                        e.printStackTrace();
                    if (onHttpViewDownload != null) {
                        onHttpViewDownload.onHttpViewDownload(httpUrl, view, null);
                    }
                } finally {
                    IOUtils.close(baos);
                    IOUtils.close(bais);
                }
            }
        }, new IOnErrorInput() {
            @Override
            public void onErrorInput(HttpURLConnection connection, InputStream is) {
                if (onHttpViewDownload != null) {
                    onHttpViewDownload.onHttpViewDownload(httpUrl, view, null);
                }
            }
        });

    }

    public interface IOnHttpViewDownload {

        /**
         * <pre>
         * {@linkplain HttpViewDownload#isTagEquals(View, Object)}
         * {@linkplain HttpViewDownload#getTag(View)}
         * {@linkplain HttpViewDownload#setTag(View, Object)}
         * </pre>
         * @param httpUrl 资源网络地址
         * @param view
         * @param inputStream 如果出现错误,此参数为null
         */
        public void onHttpViewDownload(String httpUrl, View view, InputStream inputStream);
    }

}
