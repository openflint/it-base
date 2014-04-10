package com.infthink.libs.base;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.infthink.libs.base.BaseService.BaseServiceBinder;
import com.infthink.libs.base.BaseService.IBaseServiceInitalizedListener;
import com.infthink.libs.common.message.MessageManager;
import com.infthink.libs.common.utils.IDebuggable;

public abstract class BaseActivity<T extends BaseService> extends Activity implements IDebuggable, IBaseServiceInitalizedListener {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private T mService;
    private boolean mStopServiceOnDestroy;
    private boolean mInitalized;

    protected abstract Class<T> getServiceClass();

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        onCreateBeforeSuper(savedInstanceState);
        super.onCreate(savedInstanceState);
        init();
        onCreateAfterSuper(savedInstanceState);
        onInitAliases(savedInstanceState);
    }

    public void setStopServiceOnDestroy(boolean stopServiceOnDestroy) {
        mStopServiceOnDestroy = stopServiceOnDestroy;
    }

    private void init() {
        
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG)
                Log.d(TAG, String.format("onServiceDisconnected %s", name.toShortString()));
            mService = null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG)
                Log.d(TAG, String.format("onServiceConnected %s", name.toShortString()));
            mService = (T) ((BaseServiceBinder) service).getService();
            mService.registerBaseServiceInitalizedListener(getContext());
        }

    };

    @Override
    public final void onInitalized(BaseService baseService) {
        MessageManager.register(this);
        mInitalized = true;
        onInitialized();
    }

    protected void onInitialized() {

    }

    protected void onCreateBeforeSuper(Bundle savedInstanceState) {

    }

    protected void onCreateAfterSuper(Bundle savedInstanceState) {

    }

    private void onInitAliases(Bundle savedInstanceState) {
        Intent intent = new Intent(this, getServiceClass());
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInitalized) {
            MessageManager.unregister(this);
        }
        unbindService(mServiceConnection);
        if (mStopServiceOnDestroy) {
            stopService(new Intent(this, getServiceClass()));
        }
    }

    public T getService() {
        return mService;
    }

    public BaseActivity<T> getContext() {
        return this;
    }

}
