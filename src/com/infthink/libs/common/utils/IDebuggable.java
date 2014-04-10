package com.infthink.libs.common.utils;

/**
 * 调试开关，建议实现此接口统一控制调试信息
 * <pre>
 * if (DEBUG) Log.d(TAG, "调试信息");
 * if (DEBUG) Log.e(TAG, "调试信息");
 * </pre>
 */
public interface IDebuggable {

    public static final boolean DEBUG = true;

}
