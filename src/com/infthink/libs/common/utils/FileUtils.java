package com.infthink.libs.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.util.Log;

public class FileUtils implements IDebuggable {

    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * 删除文件或者文件夹
     * @param file
     */
    public static void deleteQuietly(File file) {
        if (DEBUG) {
            Log.d(TAG, "deleteQuietly " + file);
        }
        try {
            delete(file);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <pre>
     * 以指定路径创建空文件,如果文件已经存在,则会按照一定的规则创建一个类似的文件.
     * 例如:
     * 如果创建/sdcard/xxx/a.zip文件时,该文件已经存在,则会创建/sdcard/xxx/a(1).zip文件
     * </pre>
     * @param path
     * @return 如果创建失败,返回null.
     */
    public static File createEmptyFileLike(String path) {
        File file = null;
        try {
            file = new File(path);
            if (file.exists() && file.isDirectory()) {
                throw new IOException(String.format("创建空文件失败, 指定路径已经是一个目录path:%s.", path));
            }
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (!file.createNewFile()) {
                String prefix = FilenameUtils.getPath(path) + FilenameUtils.getBaseName(path);
                String suffix = FilenameUtils.getExtension(path);
                if (!"".equals(suffix)) {
                    suffix = "." + suffix;
                }
                for (int i = 1; i < Integer.MAX_VALUE; i++) {
                    if (i == Integer.MAX_VALUE - 1) {
                        throw new IOException(String.format("该目录中存在太多相似文件名的文件 path:%s", new Object[] { path }));
                    }
                    file = new File(prefix + "(" + i + ")" + suffix);
                    if (file.createNewFile()) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            file = null;
            if (DEBUG)
                e.printStackTrace();
        }
        return file;
    }

    /**
     * <pre>
     * 以指定路径创建空文件.
     * </pre>
     * @param path
     * @return 如果创建失败,返回null.
     */
    public static File createFileIfNeed(String path) {
        File file = null;
        try {
            file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    throw new IOException(String.format("创建空文件失败, 指定路径已经是一个目录path:%s.", path));
                }
            } else {
                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                if (!file.createNewFile()) {
                    file = null;
                }
            }
        } catch (Exception e) {
            file = null;
            if (DEBUG)
                new IOException("path:" + path, e).printStackTrace();
        }
        return file;
    }

    /**
     * 在指定目录创建临时文件，如果创建失败，返回null
     * @param directory 如果为空，则在默认目录创建临时文件
     * @return
     */
    public static File createTempFile(File directory) {
        try {
            return File.createTempFile("infthink_", null, directory);
        } catch (IOException e) {
            if (DEBUG)
                e.printStackTrace();
        }
        return null;
    }

    public static File createTempFile() {
        return createTempFile(null);
    }

    /**
     * 在指定目录创建临时文件，如果创建失败，返回null
     * @param prefix 前缀不能少于3个字符
     * @param suffix 如果为空，则使用.tmp作为后缀
     * @param directory 如果为空，则在默认目录创建临时文件
     * @return
     */
    public static File createTempFile(String prefix, String suffix, File directory) {
        try {
            return File.createTempFile(prefix, suffix, directory);
        } catch (IOException e) {
            if (DEBUG)
                e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除文件或者文件夹
     * @param file
     */
    public static void delete(File file) {
        if (file != null && file.exists()) {
            if (file.isFile()) {
                if(!file.delete()) {
                    if (DEBUG) {
                        Log.d(TAG, "删除文件失败 " + file);
                    }
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "删除文件成功 " + file);
                    }
                }
            } else {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        delete(f);
                    }
                    if(!file.delete()) {
                        if (DEBUG) {
                            Log.d(TAG, "删除文件夹失败 " + file);
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "删除文件夹成功 " + file);
                        }
                    }
                } else {
                    if(!file.delete()) {
                        if (DEBUG) {
                            Log.d(TAG, "删除文件夹失败 " + file);
                        }
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "删除文件夹成功 " + file);
                        }
                    }
                }
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, "删除文件/文件夹失败, 为null或者不存在 " + file);
            }
        }
    }

    /**
     * 文件copy,将源文件的内容copy到目标文件
     * @param src 源文件
     * @param target 目标文件
     * @param force 如果目标文件已经存在，是否强制copy。
     * @return copy的字节数，如果失败，返回-1。
     */
    public static long copyFile(File src, File target, boolean force) {
        if (src == null || target == null || !src.exists() || !src.isFile()) {
            return -1;
        }
        if (target.exists()) {
            if (force) {
                delete(target);
            }
        }
        FileChannel srcChannel = null;
        FileChannel targetChannel = null;
        try {
            if (!target.createNewFile()) {
                return -1;
            }
            srcChannel = new FileInputStream(src).getChannel();
            targetChannel = new FileOutputStream(target).getChannel();
            return srcChannel.transferTo(0, srcChannel.size(), targetChannel);
        } catch (IOException e) {
            if (DEBUG)
                e.printStackTrace();
        } finally {
            IOUtils.close(srcChannel);
            IOUtils.close(targetChannel);
        }
        return -1;
    }

    /**
     * 如果目标文件存在并且不是文件夹，则会先删除再创建
     * @param file
     */
    public static void forceMkdir(File file) {
        if (file != null) {
            if (file.exists()) {
                if (!file.isDirectory()) {
                    deleteQuietly(file);
                    file.mkdirs();
                }
            } else {
                file.mkdirs();
            }
        }
    }

    /**
     * 如果目标文件已经存在，则先删除在创建空文件
     * @param file
     * @return
     */
    public static boolean forceMkFile(File file) {
        if (file != null) {
            if (file.exists()) {
                delete(file);
            }
            File parent = file.getParentFile();
            if (parent != null) {
                forceMkdir(parent);
            }
            try {
                return file.createNewFile();
            } catch (IOException e) {
                if (DEBUG)
                    e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将内容写入文件
     * @param content
     * @param file
     * @param append
     */
    public static void writeToFile(String content, File file, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, append);
            fos.write(content.getBytes());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(fos);
        }
    }

    public static byte[] readFileToBytes(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024 * 8);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] step = new byte[1024 * 8];
            int stepSize = -1;
            while ((stepSize = fis.read(step)) != -1) {
                buffer.write(step, 0, stepSize);
            }
        } catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
        } finally {
            IOUtils.close(fis);
        }
        return buffer.toByteArray();
    }

}
