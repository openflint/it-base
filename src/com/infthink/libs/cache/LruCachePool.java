package com.infthink.libs.cache;

import android.util.LruCache;

/**
 * @see #getCache(ICacheId)
 * @see #removeCache(ICacheId)
 * @see #clearAllCache()
 */
public class LruCachePool<K extends ICacheId<V>, V extends ICacheable> extends LruCache<K, V> implements ICachePool<K, V> {

    /**
     * @param maxSize 最大的强引用缓存的大小，强引用队列中的所有缓存对象的 {@linkplain #sizeOf(ICacheId, ICacheable)}}
     * 的size值之和不会超过此值。
     */
    public LruCachePool(int maxSize) {
        super(maxSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getCache(K cacheId) {
        return super.get(cacheId);
    }

    @Override
    public void removeCache(K cacheId) {
        super.remove(cacheId);
    }

    @Override
    public void clearAllCache() {
        super.evictAll();
    }

    @Override
    protected V create(K cacheId) {
        return cacheId.createCache();
    }

    @Override
    protected int sizeOf(K key, V value) {
        return value == null ? 0 : value.getCacheSize();
    }

    @Override
    public void putCache(K cacheId, V cacheable) {
        super.put(cacheId, cacheable);
    }

}
