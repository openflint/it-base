package com.infthink.libs.cache;

import com.infthink.libs.common.utils.IDebuggable;

/**
 * 对缓存的唯一标识
 */
public interface ICacheId<T extends ICacheable> extends IDebuggable {

    /**
     * 创建缓存对象，当没有在缓存池中的找到此CacheId对应的缓存对象时，
     * 就会调用该方法创建缓存对象，并缓存到缓存中。
     * @return
     */
    public T createCache();

}
