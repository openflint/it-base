package com.infthink.libs.cache;

import com.infthink.libs.common.utils.IDebuggable;

/**
 * 缓存池，存放缓存内容。提供一组缓存的存放和读取方法。
 * @see ICacheId 缓存标识
 * @see ICacheable 缓存的实体
 */
public interface ICachePool<K extends ICacheId<?>, V extends ICacheable> extends IDebuggable {

    /**
     * 从缓存池中读取一个缓存，当没有找到缓存记录时，会尝试从CacheId创建。
     * @see ICacheId#createCache()
     * @param cacheId
     * @return
     */
    public V getCache(K cacheId);

    /**
     * 从缓存池中删除一个缓存
     * @param cacheId
     */
    public void removeCache(K cacheId);

    /**
     * 向缓存池中添加一个缓存记录
     * @param cacheId
     * @param cacheable
     */
    public void putCache(K cacheId, V cacheable);

    /**
     * 清除缓存池中的所有缓存
     */
    public void clearAllCache();

}
