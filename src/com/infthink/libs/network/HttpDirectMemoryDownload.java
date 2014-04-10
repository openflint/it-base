package com.infthink.libs.network;

import java.io.InputStream;

import com.infthink.libs.common.utils.IDebuggable;
import com.infthink.libs.network.HttpDownload.IOnAddRequestProperties;
import com.infthink.libs.network.HttpMemoryDownload.IOnHttpMemoryDownload;

public class HttpDirectMemoryDownload implements IDebuggable {

    private static final long TIME_OUT = 60000;

    public static InputStream download(String httpUrl, IOnAddRequestProperties onAddRequestProperties) {
        final ObjectWrapper<InputStream> inputStreamWrapper = new ObjectWrapper<InputStream>();
        HttpMemoryDownload.download(httpUrl, onAddRequestProperties, new IOnHttpMemoryDownload() {
            @Override
            public void onHttpMemoryDownload(String httpUrl, InputStream inputStream) {
                inputStreamWrapper.setObject(inputStream);
                if (inputStreamWrapper.isLocked()) {
                    inputStreamWrapper.notify();
                } else {
                    inputStreamWrapper.setLocked(true);
                }
            }

            @Override
            public boolean isAlreadyCancelled() {
                return false;
            }
        });
        synchronized (inputStreamWrapper) {
            try {
                if (!inputStreamWrapper.isLocked()) {
                    inputStreamWrapper.setLocked(true);
                    inputStreamWrapper.wait(TIME_OUT);
                }
            } catch (Exception e) {
                if (DEBUG)
                    e.printStackTrace();
            }
        }
        return inputStreamWrapper.getObject();
    }

    public static class ObjectWrapper<T> {

        private T mObject;
        private boolean mLocked;

        public void setObject(T object) {
            mObject = object;
        }

        public T getObject() {
            return mObject;
        }

        public void setLocked(boolean locked) {
            mLocked = locked;
        }

        public boolean isLocked() {
            return mLocked;
        }

    }

}
