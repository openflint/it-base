package com.infthink.libs.upgrade;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.infthink.libs.common.os.ApkInstall;
import com.infthink.libs.common.os.AsyncFiloTask;
import com.infthink.libs.common.utils.IDebuggable;
import com.infthink.libs.common.utils.SystemUtils;
import com.infthink.libs.network.HttpFileDownload;
import com.infthink.libs.network.HttpMemoryDownload;

public class Upgrade implements IDebuggable {

    private static final String TAG = Upgrade.class.getSimpleName();
    private static final String KEY_UPGRADE_URL = "upgrade_url";
    private String mUpgradeUrl;
    private UpgradeView mUpgradeView;
    private UpgradeTask mTask;
    private IInfoParser mInfoParser;
    private int mVersionCode;
    private String mUpgradeApkPath;
    private boolean mShowChecking;

    private Upgrade() {

    }

    public static Upgrade init(Context context) {
        Upgrade instance = new Upgrade();
        try {
            instance.mVersionCode = SystemUtils.getPackageInfo(context).versionCode;
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = info.metaData;
            String upgradeUrl = metaData.getString(KEY_UPGRADE_URL, null);
            if (upgradeUrl == null) {
                if (DEBUG)
                    Log.e(TAG, String.format("没有在AndroidManifest.xml application下配置meta-data android:name=\"%s\"", KEY_UPGRADE_URL));
                throw new NameNotFoundException(String.format("没有找到升级地址,找不到以%s为KEY的Application metaData", KEY_UPGRADE_URL));
            }
            if (DEBUG)
                Log.d(TAG, String.format("versionCode:%s, 升级地址:%s", new Object[] { instance.mVersionCode, upgradeUrl }));
            instance.mUpgradeUrl = upgradeUrl;
        } catch (Exception e) {
            if (DEBUG)
                e.printStackTrace();
            instance = null;
        }
        return instance;
    }

    public void prepareUpgradeView(Activity context) {
        mUpgradeView = new UpgradeView(context);
        mUpgradeView.showChecking(mShowChecking);
    }

    public void showChecking(boolean showChecking) {
        mShowChecking = showChecking;
        if (mUpgradeView != null) {
            mUpgradeView.showChecking(mShowChecking);
        }
    }

    public void setInfoParser(IInfoParser infoParser) {
        mInfoParser = infoParser;
    }

    public void setUpgradeApkPath(String path) {
        mUpgradeApkPath = path;
    }

    public synchronized void upgrade() {
        mUpgradeView.showChecking(this);
        if (isTaskCancel()) {
            if (DEBUG)
                Log.d(TAG, String.format("检测升级信息 UpgradeUrl:%s", new Object[] { mUpgradeUrl }));
            mTask = new UpgradeTask() {

                @Override
                protected Void doInBackground(Void... params) {
                    HttpMemoryDownload.download(mUpgradeUrl, null, new HttpMemoryDownload.IOnHttpMemoryDownload() {
                        @Override
                        public void onHttpMemoryDownload(String httpUrl, InputStream inputStream) {
                            if (!isCancelled()) {
                                mHttpUrl = httpUrl;
                                mInfo = null;
                                if (inputStream != null) {
                                    mInfo = mInfoParser.parse(inputStream);
                                }
                            } else {
                                mHttpUrl = null;
                                mInfo = null;
                            }
                        }

                        @Override
                        public boolean isAlreadyCancelled() {
                            return isCancelled();
                        }
                    });
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (!isCancelled() && mHttpUrl != null) {
                        if (mInfo == null) {
                            checkUpgradeError(mHttpUrl);
                        } else {
                            checkUpgradeComplete(mHttpUrl, mInfo);
                        }
                    }
                    mTask.setComplete(true);
                }
            };
            mTask.execute();
        }
    }

    public synchronized void cancel() {
        if (!isTaskCancel()) {
            mTask.cancel(true);
        }
        mTask = null;
    }

    public boolean isTaskCancel() {
        return mTask == null || mTask.isCancelled() || mTask.isComplete();
    }

    public void confirm() {
        if (isTaskCancel()) {
            if (DEBUG)
                Log.d(TAG, String.format("下载升级文件 httpUrl:%s, info.upgradeApkUrl:%s, info.versionCode:%s", new Object[] { mTask.mHttpUrl, mTask.mInfo.mUpgradeApkUrl, mTask.mInfo.mVersionCode }));
            mUpgradeView.showDownloading(this, -1);
            UpgradeTask old = mTask;
            cancel();
            mTask = new UpgradeTask(old) {
                private File mFile;
                private long mDownloadLength;
                private long mContentLength;

                @Override
                protected Void doInBackground(Void... params) {
                    if (!isCancelled()) {
                        String path = mUpgradeApkPath;
                        HttpFileDownload.download(mInfo.mUpgradeApkUrl, path, null, new HttpFileDownload.IOnHttpFileDownload() {
                            @Override
                            public void onHttpFileDownload(String httpUrl, File file, long downloadLength, long contentLength, HttpURLConnection connection) {
                                if (!isCancelled()) {
                                    mFile = file;
                                    mDownloadLength = downloadLength;
                                    mContentLength = contentLength;
                                    publishProgress();
                                }
                            }

                            @Override
                            public boolean isAlreadyCancelled() {
                                return isCancelled();
                            }

                            @Override
                            public void onHttpFileDownloaded(boolean sucessed) {
                                // TODO Auto-generated method stub
                                
                            }

                        });
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Void... values) {
                    if (!isCancelled()) {
                        if (mFile == null) {
                            if (DEBUG)
                                Log.d(TAG, "showDownloadingFail");
                            mUpgradeView.showDownloadingFail(Upgrade.this);
                        } else {
                            if (DEBUG)
                                Log.d(TAG, String.format("showDownloading#mDownloadLength:%s, mContentLength:%s", mDownloadLength, mContentLength));
                            if (mDownloadLength < mContentLength) {
                                long downloadPercent = mDownloadLength * 100 / mContentLength;
                                mUpgradeView.showDownloading(Upgrade.this, (int)downloadPercent);
                            } else {
                                mUpgradeView.dismiss();
                                // 安装升级
                                ApkInstall.install(mUpgradeView.getContxt(), mFile);
                            }
                        }
                    }
                }

                @Override
                protected void onPostExecute(Void result) {
                    mTask.setComplete(true);
                }

            };
            mTask.execute();
        }
    }

    /**
     * 获取升级信息失败
     * @param upgradeCheckUrl
     */
    private void checkUpgradeError(String upgradeCheckUrl) {
        if (!isTaskCancel()) {
            if (DEBUG)
                Log.d(TAG, String.format("checkUpgradeError upgradeCheckUrl:%s", upgradeCheckUrl));
            mUpgradeView.showCheckingError(this);
        }
    }

    /**
     * 获取升级信息成功
     * @param upgradeCheckUrl
     * @param inputStream
     */
    private void checkUpgradeComplete(String upgradeCheckUrl, Info info) {
        if (!isTaskCancel()) {
            if (DEBUG)
                Log.d(TAG, String.format("checkUpgradeComplete upgradeCheckUrl:%s", upgradeCheckUrl));
            if (info == null) {
                mUpgradeView.showCheckingError(this);
            } else if (info.mVersionCode > mVersionCode) {
                mUpgradeView.showConfirm(this);
            } else {
                mUpgradeView.showCheckingLatest(this);
            }
        }
    }

    public static class Info {
        private int mVersionCode;
        private String mUpgradeApkUrl;

        public int getVersionCode() {
            return mVersionCode;
        }

        public void setVersionCode(int versionCode) {
            mVersionCode = versionCode;
        }

        public String getUpgradeApkUrl() {
            return mUpgradeApkUrl;
        }

        public void setUpgradeApkUrl(String upgradeApkUrl) {
            mUpgradeApkUrl = upgradeApkUrl;
        }
    }

    public static interface IInfoParser {
        public Info parse(InputStream is);
    }

    abstract class UpgradeTask extends AsyncFiloTask<Void, Void, Void> {
        String mHttpUrl;
        Info mInfo;
        boolean mComplete;

        boolean isComplete() {
            return mComplete;
        }

        void setComplete(boolean complete) {
            mComplete = complete;
        }

        UpgradeTask() {

        }

        UpgradeTask(UpgradeTask task) {
            if (task != null) {
                mHttpUrl = task.mHttpUrl;
                mInfo = task.mInfo;
            }
        }

    }

}
