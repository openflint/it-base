package com.infthink.libs.cache;

import com.infthink.libs.common.utils.IDebuggable;

/**
 * 可缓存的对象
 */
public interface ICacheable extends IDebuggable {

    /**
     * @return 缓存大小值 以字节为单位
     */
    public int getCacheSize();

}
