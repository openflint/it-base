package com.infthink.libs.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.util.Log;

import com.infthink.libs.common.utils.IDebuggable;
import com.infthink.libs.common.utils.IOUtils;
import com.infthink.libs.network.HttpDownload.IOnAddRequestProperties;
import com.infthink.libs.network.HttpDownload.IOnErrorInput;

public class HttpMemoryDownload implements IDebuggable {

    private static final String TAG = HttpMemoryDownload.class.getSimpleName();

    /**
     * @param httpUrl
     * @param onAddRequestProperties
     * @param onHttpMemoryDownload
     */
    public static void download(final String httpUrl, IOnAddRequestProperties onAddRequestProperties, final IOnHttpMemoryDownload onHttpMemoryDownload) {
        HttpDownload.download(HttpDownload.METHOD_GET, httpUrl, onAddRequestProperties, true, null, new HttpDownload.IOnDoInput() {
            @Override
            public void onDoInput(HttpURLConnection connection, InputStream is) {
                int contentLength = connection.getContentLength();
                if (DEBUG)
                    Log.d(TAG, String.format("网络返回内容长度:%s, url:%s", new Object[] { contentLength, httpUrl }));
                if (onHttpMemoryDownload != null && onHttpMemoryDownload.isAlreadyCancelled()) {
                    return;
                }

                ByteArrayOutputStream baos = null;
                ByteArrayInputStream bais = null;
                try {
                    baos = new ByteArrayOutputStream(16 * 1024);
                } catch (Exception e) {
                    if (DEBUG)
                        e.printStackTrace();
                }
                if (baos == null) {
                    if (DEBUG)
                        Log.d(TAG, String.format("创建MemoryFile失败, name:%s, length:%s", new Object[] { null, contentLength }));
                    if (onHttpMemoryDownload != null) {
                        onHttpMemoryDownload.onHttpMemoryDownload(httpUrl, null);
                    }
                    return;
                }
                try {
                    byte[] step = new byte[16 * 1024];
                    int read = -1;
                    while ((read = is.read(step)) != -1) {
                        baos.write(step, 0, read);
                        if (onHttpMemoryDownload != null && onHttpMemoryDownload.isAlreadyCancelled()) {
                            break;
                        }
                    }
                    bais = new ByteArrayInputStream(baos.toByteArray());
                    if (onHttpMemoryDownload != null && !onHttpMemoryDownload.isAlreadyCancelled()) {
                        onHttpMemoryDownload.onHttpMemoryDownload(httpUrl, bais);
                    }
                } catch (IOException e) {
                    if (DEBUG)
                        e.printStackTrace();
                    if (onHttpMemoryDownload != null) {
                        onHttpMemoryDownload.onHttpMemoryDownload(httpUrl, null);
                    }
                } finally {
                    IOUtils.close(baos);
                    IOUtils.close(bais);
                }
            }
        }, new IOnErrorInput() {
            @Override
            public void onErrorInput(HttpURLConnection connection, InputStream is) {
                if (onHttpMemoryDownload != null) {
                    onHttpMemoryDownload.onHttpMemoryDownload(httpUrl, null);
                }
            }
        });

    }

    public interface IOnHttpMemoryDownload {

        /**
         * @param httpUrl 文件网络地址
         * @param inputStream 如果出现错误,此参数为null
         */
        public void onHttpMemoryDownload(String httpUrl, InputStream inputStream);

        /**
         * 控制中断下载
         * @return
         */
        public boolean isAlreadyCancelled();
    }

}
