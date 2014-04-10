package com.infthink.libs.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import android.util.Log;

import com.infthink.libs.common.utils.FileUtils;
import com.infthink.libs.common.utils.IDebuggable;
import com.infthink.libs.common.utils.IOUtils;
import com.infthink.libs.network.HttpDownload.IOnAddRequestProperties;
import com.infthink.libs.network.HttpDownload.IOnErrorInput;

public class HttpFileDownload implements IDebuggable {

    private static final String TAG = HttpFileDownload.class.getSimpleName();

    private static final String DOWNLOAD_LENGTH = "Download_Length";
    private static final String DOWNLOAD_SUPPORT_CONTINUE = "Download_support_continue";
    private static final String RELOAD = "Reload";

    /**
     * @param httpUrl
     * @param path
     * @param onAddRequestProperties
     * @param onHttpFileDownload
     */
    public static void download(final String httpUrl, final String path, IOnAddRequestProperties onAddRequestProperties, final IOnHttpFileDownload onHttpFileDownload) {
        HttpDownload.download(HttpDownload.METHOD_GET, httpUrl, onAddRequestProperties, false, null, new HttpDownload.IOnDoInput() {
            @Override
            public void onDoInput(HttpURLConnection connection, InputStream is) {
                String contentLengthHeader = connection.getHeaderField("Content-Length");
                android.util.Log.d("XXXXXXXXXX", "contentLengthHeader = " + contentLengthHeader);
                long contentLength = -1;
                if (contentLengthHeader != null) {
                    try {
                        contentLength = Long.valueOf(contentLengthHeader);
                    } catch (Exception e) {
                        if (DEBUG)
                            e.printStackTrace();
                    }
                }
                if (DEBUG)
                    Log.d(TAG, String.format("网络返回内容长度:%s, url:%s", new Object[] { contentLength, httpUrl }));
                File file = FileUtils.createEmptyFileLike(path);
                if (file == null) {
                    if (DEBUG)
                        Log.d(TAG, String.format("创建空文件失败,path:%s", path));
                    if (onHttpFileDownload != null) {
                        onHttpFileDownload.onHttpFileDownload(httpUrl, null, -1, contentLength, connection);
                    }
                    return;
                }
                if (onHttpFileDownload != null && onHttpFileDownload.isAlreadyCancelled()) {
                    return;
                }
                RandomAccessFile randomAccessFile = null;
                try {
                    randomAccessFile = new RandomAccessFile(file, "rws");
                    if (contentLength > 0) {
                        randomAccessFile.setLength(contentLength);
                    }
                    byte[] step = new byte[16 * 1024];
                    int read = -1;
                    long downloadLength = 0;
                    while ((read = is.read(step)) != -1) {
                        downloadLength += read;
                        randomAccessFile.write(step, 0, read);
                        if (onHttpFileDownload != null) {
                            onHttpFileDownload.onHttpFileDownload(httpUrl, file, downloadLength, contentLength, connection);
                            if (onHttpFileDownload.isAlreadyCancelled()) {
                                break;
                            }
                        }
                    }
                    if (onHttpFileDownload != null) {
                        onHttpFileDownload.onHttpFileDownloaded(true);
                    }
                } catch (Exception e) {
                    if (DEBUG)
                        e.printStackTrace();
                    if (onHttpFileDownload != null) {
                        onHttpFileDownload.onHttpFileDownload(httpUrl, null, -1, contentLength, connection);
                        onHttpFileDownload.onHttpFileDownloaded(false);
                    }
                    FileUtils.deleteQuietly(file);
                    return;
                } finally {
                    IOUtils.close(randomAccessFile);
                }
                
            }
        }, new IOnErrorInput() {
            @Override
            public void onErrorInput(HttpURLConnection connection, InputStream is) {
                if (onHttpFileDownload != null) {
                    int contentLength = connection.getContentLength();
                    onHttpFileDownload.onHttpFileDownload(httpUrl, null, -1, contentLength, connection);
                }
            }
        });
    }

    /**
     * 支持断点续传的下载
     * @param httpUrl
     * @param path
     * @param onAddRequestProperties
     * @param onHttpFileDownload
     */
    public static void downloadReget(final String httpUrl, final String path, final IOnAddRequestProperties onAddRequestProperties, final IOnHttpFileDownload onHttpFileDownload) {
        downloadReget(httpUrl, path, false, onAddRequestProperties, onHttpFileDownload);
    }

    /**
     * @param httpUrl
     * @param path
     * @param onAddRequestProperties
     * @param onHttpFileDownload
     */
    private static void downloadReget(final String httpUrl, final String path, final boolean newLoad, final IOnAddRequestProperties onAddRequestProperties, final IOnHttpFileDownload onHttpFileDownload) {
        checkDownloadFile(path);
        final File file = FileUtils.createFileIfNeed(path);
        final File secondaryFile = FileUtils.createFileIfNeed(file.getPath() + ".sdf");
        final Properties properties = new Properties();
        final FileInputStream in = openFileInput(secondaryFile);
        final FileOutputStream out = openFileOutput(secondaryFile);
        long modified = -1;
        long length = -1;
        boolean canContinue = false;
        if (!newLoad && in != null) {
            try {
                properties.load(in);
                canContinue = Boolean.valueOf(properties.getProperty(DOWNLOAD_SUPPORT_CONTINUE, "false"));
                modified = Long.valueOf(properties.getProperty(HttpDownload.HEAD_Last_Modified, "-1"));
                length = Long.valueOf(properties.getProperty(DOWNLOAD_LENGTH, "-1"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        final long tmpLastModified = modified;
        HttpDownload.download(HttpDownload.METHOD_GET, httpUrl, canContinue ? length : -1, onAddRequestProperties, false, null, new HttpDownload.IOnDoInput() {
            @Override
            public void onDoInput(HttpURLConnection connection, InputStream is) {
                String contentLengthHeader = connection.getHeaderField(HttpDownload.HEAD_Content_Length);
                String acceptRangesHeader = connection.getHeaderField(HttpDownload.HEAD_Accept_Ranges);
                String range = connection.getHeaderField(HttpDownload.HEAD_Content_Range);
                long lastModifiedHeader = connection.getLastModified();
                boolean reload = false;
                if (properties != null && out != null) {
                    if (acceptRangesHeader != null) {
                        properties.setProperty(DOWNLOAD_SUPPORT_CONTINUE, acceptRangesHeader.trim().equalsIgnoreCase("bytes") ? "true" : "false");
                    } else {
                        if (range == null) {
                            properties.setProperty(DOWNLOAD_SUPPORT_CONTINUE, "false");
                        }
                    }
                }

                if (tmpLastModified != lastModifiedHeader && properties != null && out != null) {
                    properties.setProperty(RELOAD, newLoad ? "false" : "true");
                    properties.setProperty(HttpDownload.HEAD_Last_Modified, String.valueOf(lastModifiedHeader));
                    if (!newLoad) {
                        reload = true;
                    }
                }
                if (reload) {
                    try {
                        properties.store(out, "");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                long contentLength = -1;
                if (contentLengthHeader != null) {
                    try {
                        contentLength = Long.valueOf(contentLengthHeader);
                    } catch (Exception e) {
                        if (DEBUG)
                            e.printStackTrace();
                    }
                }

                long startPos = 0;
                if (range != null && !range.equals("")) {
                    String[] strs = range.split(" ");
                    if (strs.length >= 2) {
                        String[] tmp = strs[1].split("-");
                        if (tmp.length > 1) {
                            startPos = Long.valueOf(tmp[0]);
                        }
                        
                        tmp = strs[1].split("/");
                        if (tmp.length > 1) {
                            contentLength = Long.valueOf(tmp[1]);
                        }
                    }
                    
                }
                if (DEBUG)
                    Log.d(TAG, String.format("网络返回内容长度:%s, url:%s", new Object[] { contentLength, httpUrl }));
                if (file == null) {
                    if (DEBUG)
                        Log.d(TAG, String.format("创建空文件失败,path:%s", path));
                    if (onHttpFileDownload != null) {
                        onHttpFileDownload.onHttpFileDownload(httpUrl, null, -1, contentLength, connection);
                    }
                    return;
                }
                if (onHttpFileDownload != null && onHttpFileDownload.isAlreadyCancelled()) {
                    return;
                }
                RandomAccessFile randomAccessFile = null;
                try {
                    randomAccessFile = new RandomAccessFile(file, "rws");
                    if (contentLength > 0) {
                        if (contentLength == startPos + 1) {
                            onHttpFileDownload.onHttpFileDownload(httpUrl, file, contentLength, contentLength, connection);
                            return;
                        }
//                        randomAccessFile.setLength(contentLength);

                        if (startPos > 0 && startPos < contentLength) {
                            randomAccessFile.seek(startPos);
                        }
                    }
                    byte[] step = new byte[16 * 1024];
                    int read = -1;
                    long downloadLength = startPos;
                    boolean downloadCancelled = false;
                    while ((contentLength < 0 || downloadLength < contentLength) && ((read = is.read(step)) != -1)) {
                        downloadLength += read;
                        randomAccessFile.write(step, 0, read);
                        if (properties != null && out != null) {
                            properties.setProperty(DOWNLOAD_LENGTH, String.valueOf(downloadLength));
                        }
                        if (onHttpFileDownload != null) {
                            onHttpFileDownload.onHttpFileDownload(httpUrl, file, downloadLength, contentLength, connection);
                            if (onHttpFileDownload.isAlreadyCancelled()) {
                                downloadCancelled = true;
                                break;
                            }
                        }
                    }
                    if (!downloadCancelled) {
                        if (secondaryFile.exists()) {
                            secondaryFile.delete();
                        }
                    } else {
                        properties.store(out, "");
                    }
                } catch (SocketException e) {
                    if (DEBUG)
                        e.printStackTrace();
                    if (onHttpFileDownload != null) {
                        onHttpFileDownload.onHttpFileDownload(httpUrl, null, -1, contentLength, connection);
                    }
                    try {
                        if (properties != null && out != null) {
                            properties.store(out, "");
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG)
                        e.printStackTrace();
                    if (onHttpFileDownload != null) {
                        onHttpFileDownload.onHttpFileDownload(httpUrl, null, -1, contentLength, connection);
                    }
                    FileUtils.deleteQuietly(file);
                    FileUtils.deleteQuietly(secondaryFile);
                    return;
                } finally {
                    IOUtils.close(randomAccessFile);
                }
            }
        }, new IOnErrorInput() {
            @Override
            public void onErrorInput(HttpURLConnection connection, InputStream is) {
                if (onHttpFileDownload != null) {
                    int contentLength = (connection == null) ? -1 : connection.getContentLength();
                    onHttpFileDownload.onHttpFileDownload(httpUrl, null, -1, contentLength, connection);
                }
            }
        });
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!newLoad && secondaryFile.exists() && Boolean.valueOf(properties.getProperty(RELOAD, "false"))) {
            downloadReget(httpUrl, path, true, onAddRequestProperties, onHttpFileDownload);
        }
    }

    private static void checkDownloadFile(String path) {
        File file = null;
        try {
            file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    throw new IOException(String.format("创建空文件失败, 指定路径已经是一个目录path:%s.", path));
                }
            } else {
                File sdf = FileUtils.createFileIfNeed(path + ".sdf");
                if (sdf.exists()) {
                    sdf.delete();
                }
            }
        } catch (Exception e) {
            File sdf = FileUtils.createFileIfNeed(path + ".sdf");
            if (sdf.exists()) {
                sdf.delete();
            }
        }
    }

    private static FileInputStream openFileInput(File file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return in;
    }

    private static FileOutputStream openFileOutput(File file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return out;
    }

    public interface IOnHttpFileDownload {

        /**
         * @param httpUrl 文件网络地址
         * @param file 如果出现错误,此参数为null
         * @param downloadLength 当前已经下载的长度 byte, 如果出错返回-1.
         * @param contentLength 网络返回的Content-Length长度,如果网络没有指定此实体头,返回-1
         */
        public void onHttpFileDownload(String httpUrl, File file, long downloadLength, long contentLength, HttpURLConnection connection);

        /**
         * 控制中断下载
         * @return
         */
        public boolean isAlreadyCancelled();
        
        public void onHttpFileDownloaded(boolean sucessed);
    }

}
