package com.infthink.libs.base;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.infthink.libs.common.message.MessageManager;
import com.infthink.libs.common.os.AsyncFiloTask;
import com.infthink.libs.common.utils.IDebuggable;

public abstract class BaseService extends Service implements IDebuggable {

    private static final String TAG = BaseService.class.getSimpleName();
    private BaseServiceBinder mBinder;
    private boolean mInitialized;
    private List<IBaseServiceInitalizedListener> mBaseServiceInitalizedListeners;
    private AsyncFiloTask<Void, Void, Void> mInitTask;

    @Override
    public final void onCreate() {
        super.onCreate();
        init();
        mInitTask = new AsyncFiloTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (!isCancelled()) {
                    onInit();
                }
                if (DEBUG)
                    Log.d(TAG, String.format("%s onInitRequired end", getServiceClassName()));
                if (!isCancelled()) {
                    publishProgress();
                    onTaskBackground();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                MessageManager.register(getContext());
                mInitialized = true;
                for (IBaseServiceInitalizedListener listener : mBaseServiceInitalizedListeners) {
                    listener.onInitalized(getContext());
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        };
        mInitTask.execute();
    }

    private void init() {
        mBinder = new BaseServiceBinder();
        mBaseServiceInitalizedListeners = new ArrayList<IBaseServiceInitalizedListener>();
    }

    void registerBaseServiceInitalizedListener(IBaseServiceInitalizedListener listener) {
        if (mBaseServiceInitalizedListeners.contains(listener)) {
            throw new IllegalStateException(String.format("已经注册初始化监听 %s", listener), new RuntimeException());
        }
        mBaseServiceInitalizedListeners.add(listener);
        if (mInitialized) {
            listener.onInitalized(this);
        }
    }

    void unregisterBaseServiceInitalizedListener(IBaseServiceInitalizedListener listener) {
        if (!mBaseServiceInitalizedListeners.contains(listener)) {
            throw new IllegalStateException(String.format("没有注册初始化监听 %s", listener), new RuntimeException());
        }
        mBaseServiceInitalizedListeners.remove(listener);
    }

    public final String getServiceClassName() {
        return getClass().getName();
    }

    /**
     * 这是一个非UI方法
     */
    protected void onInit() {

    }

    /**
     * 这是一个非UI方法
     */
    protected void onTaskBackground() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInitTask.cancel(true);
        if (mInitialized) {
            MessageManager.unregister(getContext());
        }
    }

    @Override
    public final IBinder onBind(Intent intent) {
        return mBinder;
    }

    public final class BaseServiceBinder extends Binder {

        public BaseService getService() {
            return BaseService.this;
        }

    }

    public final BaseService getContext() {
        return this;
    }

    interface IBaseServiceInitalizedListener {
        void onInitalized(BaseService baseService);
    }

}
