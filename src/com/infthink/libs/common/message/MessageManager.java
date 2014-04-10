package com.infthink.libs.common.message;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.infthink.libs.common.utils.IDebuggable;

/**
 * {@linkplain #init(Context)}
 * {@linkplain #close()}
 * {@linkplain #register(Object)}
 * {@linkplain #unregister(Object)}
 */
public final class MessageManager implements IDebuggable {

    private static final String TAG = MessageManager.class.getSimpleName();
    private static final String ACTION_MESSAGE = MessageManager.class.getName();
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_RESPONSE_COUNT = "responseCount";
    private static final String EXTRA_RUN_ON_UI_THREAD = "runOnUiThread";
    private static LocalBroadcastManager sLocalBroadcastManager;

    private static Map<Object, Map<String, MessageReciver>> sMessageReciverInstancesMethods;
    private static List<Object> sMessageReciverInstances;

    private static boolean sCanInit = true;
    private static ReentrantReadWriteLock sReadWriteLock = new ReentrantReadWriteLock();
    private static Lock sReadLock = sReadWriteLock.readLock();
    private static Lock sWriteLock = sReadWriteLock.writeLock();

    private static HandlerThread sBackgroundHandlerThread;
    private static Handler sUiHandler;
    private static Handler sBackgroundHandler;

    /**
     * 初始化MessageManager
     * @param context
     */
    public static void init(Context context) {
        sWriteLock.lock();
        try {
            if (!sCanInit) {
                throw new IllegalStateException("MessageManager已经初始化了", new RuntimeException());
            }
            sCanInit = false;

            sBackgroundHandlerThread = new HandlerThread(TAG);
            sBackgroundHandlerThread.start();
            sBackgroundHandler = new Handler(sBackgroundHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (DEBUG)
                        Log.d(TAG, "派发后台消息");
                    executeMessage(msg);
                }
            };

            sUiHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (DEBUG)
                        Log.d(TAG, "派发Ui消息");
                    executeMessage(msg);
                }
            };

            sMessageReciverInstancesMethods = new HashMap<Object, Map<String, MessageReciver>>();
            sMessageReciverInstances = new ArrayList<Object>();
            sLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
            sLocalBroadcastManager.registerReceiver(sLocalBroadcastReceiver, new IntentFilter(ACTION_MESSAGE));
        } finally {
            sWriteLock.unlock();
        }
    }

    private static void executeMessage(Message msg) {
        sReadLock.lock();
        try {
            if (sCanInit) {
                throw new IllegalStateException("MessageManager没有初始化", new RuntimeException());
            }
            List<Object> messageReciverInstances = sMessageReciverInstances;
            int responseCount = msg.what;
            if (responseCount == -1) {
                responseCount = Integer.MAX_VALUE;
            }
            Object message = msg.obj;
            String msgName = message.getClass().getName();
            int messageReciverInstancesSize = messageReciverInstances.size();
            if (DEBUG)
                Log.d(TAG, String.format("派发消息 message:%s, responseCount:%s, messageReciverInstancesSize:%s", msgName, responseCount, messageReciverInstancesSize));
            for (int i = 0; i < messageReciverInstancesSize && responseCount > 0; i++) {
                Object instance = messageReciverInstances.get(i);
                Map<String, MessageReciver> messageReciverMap = sMessageReciverInstancesMethods.get(instance);
                if (messageReciverMap != null) {
                    MessageReciver messageReciver = messageReciverMap.get(msgName);
                    if (messageReciver != null) {
                        if (DEBUG)
                            Log.d(TAG, String.format("消息%s, 匹配到 %s:%s", msgName, instance.getClass().getName(), messageReciver.mMethod.getName()));
                        try {
                            messageReciver.mMethod.invoke(instance, message);
                        } catch (Exception e) {
                            throw new RuntimeException("调用方法失败", e);
                        }
                        responseCount--;
                    }
                }
            }
        } finally {
            sReadLock.unlock();
        }
    }

    /**
     * @param message 消息内容
     * @param responseCount
     * @param runOnUiThread
     */
    public final static void sendMessage(Object message, int responseCount, boolean runOnUiThread) {
        sReadLock.lock();
        try {
            if (sCanInit) {
                throw new IllegalStateException("MessageManager没有初始化", new RuntimeException());
            }
            Intent intent = new Intent(ACTION_MESSAGE);
            intent.putExtra(EXTRA_MESSAGE, new MessageWrapper(message));
            intent.putExtra(EXTRA_RESPONSE_COUNT, responseCount);
            intent.putExtra(EXTRA_RUN_ON_UI_THREAD, runOnUiThread);
            sLocalBroadcastManager.sendBroadcast(intent);
        } finally {
            sReadLock.unlock();
        }
    }

    /**
     * 关闭MessageManager
     */
    public static void close() {
        sWriteLock.lock();
        try {
            if (sCanInit) {
                throw new IllegalStateException("MessageManager没有初始化", new RuntimeException());
            }
            sCanInit = true;
            sBackgroundHandlerThread.quit();
            sLocalBroadcastManager.unregisterReceiver(sLocalBroadcastReceiver);
        } finally {
            sWriteLock.unlock();
        }
    }

    /**
     * 向MessageManager注册一个类,会分析这个类中相关的注解
     * @param object
     */
    public static void register(Object object) {
        sWriteLock.lock();
        try {
            if (sCanInit) {
                throw new IllegalStateException("MessageManager没有初始化", new RuntimeException());
            }
            if (sMessageReciverInstances.contains(object)) {
                throw new IllegalStateException(String.format("该类已经注册 %s", object), new RuntimeException());
            }
            sMessageReciverInstances.add(object);
            Method[] methods = object.getClass().getDeclaredMethods();
            Map<String, MessageReciver> messageReciverMap = new HashMap<String, MessageReciver>();
            for (Method method : methods) {
                MessageResponse message = method.getAnnotation(MessageResponse.class);
                if (message != null) {
                    Class<?>[] pTypes = method.getParameterTypes();
                    if (pTypes.length != 1) {
                        throw new IllegalArgumentException(String.format("消息接收方法有且只能有一个参数 %s:%s", object.getClass().getName(), method.getName()), new RuntimeException());
                    }
                    MessageReciver messageAnnotation = new MessageReciver();
                    messageAnnotation.mMethod = method;
                    messageAnnotation.mParamType = pTypes[0];
                    if (messageReciverMap.containsKey(pTypes[0].getName())) {
                        throw new IllegalStateException(String.format("消息接收方法不能重复定义 %s %s:%s", object.getClass().getName(), method.getName(),
                                messageReciverMap.get(pTypes[0].getName()).mMethod.getName()), new RuntimeException());
                    }
                    messageReciverMap.put(messageAnnotation.mParamType.getName(), messageAnnotation);
                }
            }
            sMessageReciverInstancesMethods.put(object, messageReciverMap);
        } finally {
            sWriteLock.unlock();
        }
    }

    /**
     * 向MessageManager反注册一个类
     * @param object
     */
    public static void unregister(Object object) {
        sWriteLock.lock();
        try {
            if (sCanInit) {
                throw new IllegalStateException("MessageManager没有初始化", new RuntimeException());
            }
            if (!sMessageReciverInstances.contains(object)) {
                throw new IllegalStateException(String.format("该类没有注册 %s", object), new RuntimeException());
            }
            sMessageReciverInstances.remove(object);
            sMessageReciverInstancesMethods.remove(object);
        } finally {
            sWriteLock.unlock();
        }
    }

    private static BroadcastReceiver sLocalBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            sReadLock.lock();
            try {
                if (sCanInit) {
                    throw new IllegalStateException("MessageManager没有初始化", new RuntimeException());
                }
                Serializable messageWrapper = intent.getSerializableExtra(EXTRA_MESSAGE);
                boolean runOnUiThread = intent.getBooleanExtra(EXTRA_RUN_ON_UI_THREAD, false);
                int responseCount = intent.getIntExtra(EXTRA_RESPONSE_COUNT, 0);
                if (responseCount != 0 && messageWrapper != null) {
                    Object message = ((MessageWrapper) messageWrapper).mMessage;
                    if (DEBUG)
                        Log.d(TAG, String.format("准备派送消息 message:%s runOnUiThread:%s responseCount:%s", message.getClass().getName(), runOnUiThread, responseCount));
                    if (runOnUiThread) {
                        sUiHandler.obtainMessage(responseCount, message).sendToTarget();
                    } else {
                        sBackgroundHandler.obtainMessage(responseCount, message).sendToTarget();
                    }
                }
            } finally {
                sReadLock.unlock();
            }
        }

    };

    private static class MessageReciver {

        private Method mMethod;
        private Class<?> mParamType;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mParamType == null) ? 0 : mParamType.getName().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MessageReciver other = (MessageReciver) obj;
            if (mParamType == null) {
                if (other.mParamType != null)
                    return false;
            } else if (!mParamType.getName().equals(other.mParamType.getName()))
                return false;
            return true;
        }

    }

    @SuppressWarnings("serial")
    private static class MessageWrapper implements Serializable {

        private Object mMessage;

        public MessageWrapper(Object message) {
            mMessage = message;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mMessage == null) ? 0 : mMessage.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MessageWrapper other = (MessageWrapper) obj;
            if (mMessage == null) {
                if (other.mMessage != null)
                    return false;
            } else if (!mMessage.equals(other.mMessage))
                return false;
            return true;
        }

    }

}
