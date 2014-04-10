package com.infthink.libs.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils implements IDebuggable {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 此方法并不会close输入流
     * @param is
     * @return 读取失败 返回null
     */
    public static String readString(InputStream is) {
        String result = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(16 * 1024);
        byte[] step = new byte[16 * 1024];
        int read = -1;
        try {
            while ((read = is.read(step)) != -1) {
                baos.write(step, 0, read);
            }
            result = baos.toString();
        } catch (IOException e) {
            if (DEBUG)
                e.printStackTrace();
        }
        return result;
    }

    /**
     * 此方法不会关闭输入输出流
     * @param is
     * @param os
     * @return copy的字节数,如果copy失败,返回-1.
     */
    public static long copy(InputStream is, OutputStream os) {
        long copy = 0;
        try {
            byte[] step = new byte[16 * 1024];
            int read = -1;
            while ((read = is.read(step)) != -1) {
                os.write(step, 0, read);
                copy += read;
            }
        } catch (IOException e) {
            if (DEBUG)
                e.printStackTrace();
            copy = -1;
        }
        return copy;
    }

}
