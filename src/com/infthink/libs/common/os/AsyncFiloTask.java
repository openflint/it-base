package com.infthink.libs.common.os;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.infthink.libs.common.utils.IDebuggable;

/**
 * 使用后进先出的方式,保证最近需要加载的尽快的处理.
 * {@linkplain #execute()}
 */
public abstract class AsyncFiloTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements IDebuggable {

    private static final String TAG = AsyncFiloTask.class.getSimpleName();
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger COUNT = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, TAG + "#" + COUNT.getAndIncrement());
        }
    };

    @SuppressWarnings("serial")
    private static final BlockingQueue<Runnable> POOL_WORK_QUEUE = new LinkedBlockingDeque<Runnable>() {

        @Override
        public boolean offer(Runnable e) {
            return super.offerLast(e);
        }

        @Override
        public boolean offer(Runnable e, long timeout, TimeUnit unit) throws InterruptedException {
            return super.offerLast(e, timeout, unit);
        }

        @Override
        public Runnable take() throws InterruptedException {
            return super.takeLast();
        }

        @Override
        public Runnable poll() {
            return super.pollLast();
        }

        @Override
        public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
            return super.pollLast(timeout, unit);
        }

    };

    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, POOL_WORK_QUEUE, THREAD_FACTORY);

    private static final HandlerThread HANDLER_THREAD = new HandlerThread(TAG + "#HandlerThread", Process.THREAD_PRIORITY_MORE_FAVORABLE);
    static {
        HANDLER_THREAD.start();
    }

    private final Handler mHandler;
    {
        mHandler = new MyHandler<Params, Progress, Result>(this, HANDLER_THREAD.getLooper());
    }

    private static class MyHandler<Params, Progress, Result> extends Handler {

        private AsyncFiloTask<Params, Progress, Result> mLoaderTask;

        private MyHandler(AsyncFiloTask<Params, Progress, Result> loaderTask, Looper looper) {
            super(looper);
            mLoaderTask = loaderTask;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            if (mLoaderTask != null && !mLoaderTask.isCancelled()) {
                mLoaderTask.executeOnExecutor(THREAD_POOL_EXECUTOR, null);
            }
        }

    }

    public final void execute() {
        mHandler.sendEmptyMessageAtTime(0, 0);
    }

}
