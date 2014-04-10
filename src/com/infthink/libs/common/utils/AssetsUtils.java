package com.infthink.libs.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * 读取assets目录下的资源
 * <pre>
 * String assetsFile = "a.txt";
 * Context context = getContext();
 * byte[] content = AssetsUtils.readAll(context, assetsFile);
 * String result = new String(content, "UTF-8");
 * </pre>
 */
public class AssetsUtils implements IDebuggable {

    private static final String TAG = AssetsUtils.class.getSimpleName();

    /**
     * 
     * @param context
     * @param path for example: a.text
     * @return
     */
    public static byte[] readAll(Context context, String path) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(8 * 1024);
        AssetManager manager = context.getAssets();
        InputStream is = null;
        try {
            is = manager.open(path);
            byte[] step = new byte[8 * 1024];
            int read = -1;
            while ((read = is.read(step)) != -1) {
                buffer.write(step, 0, read);
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, String.format("读取assets资源(%s)发生异常", path));
                e.printStackTrace();
            }
        } finally {
            IOUtils.close(is);
            IOUtils.close(buffer);
        }
        return buffer.toByteArray();
    }

}
