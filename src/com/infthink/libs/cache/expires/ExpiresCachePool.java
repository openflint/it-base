package com.infthink.libs.cache.expires;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

import com.infthink.libs.cache.ICacheId;
import com.infthink.libs.cache.LruCachePool;

public class ExpiresCachePool<K extends IExpiresCacheId<V>, V extends IExpiresCacheable> extends LruCachePool<K, V> {

    private static final String TAG = ExpiresCachePool.class.getSimpleName();
    private final Map<ICacheId<V>, Reference<V>> mCache;

    public ExpiresCachePool(int maxSize) {
        super(maxSize);
        mCache = new ConcurrentHashMap<ICacheId<V>, Reference<V>>();
    }

    @Override
    public void clearAllCache() {
        mCache.clear();
        super.clearAllCache();
    }

    @Override
    public void removeCache(K cacheId) {
        mCache.remove(cacheId);
        super.removeCache(cacheId);
    }

    @Override
    public void putCache(K cacheId, V cacheable) {
        // 此方法会覆盖旧的缓存记录
        mCache.put(cacheId, new SoftReference<V>(cacheable));
        super.putCache(cacheId, cacheable);
    }

    @Override
    protected V create(K cacheId) {
        Reference<V> referenceCache = mCache.get(cacheId);
        if (referenceCache != null) {
            V cache = referenceCache.get();
            if (cache != null) {
                return cache;
            }
        }
        V cacheable = super.create(cacheId);
        if (cacheable != null) {
            // 此方法会覆盖旧的缓存记录
            mCache.put(cacheId, new SoftReference<V>(cacheable));
        }
        return cacheable;
    }

    @Override
    public V getCache(K cacheId) {
        // TODO Auto-generated method stub
        V cacheable = super.getCache(cacheId);
        if (cacheable == null) {
            return null;
        }
        if (cacheable.isExpires()) {
            removeCache(cacheId);
            cacheable = super.getCache(cacheId);
            if (cacheable != null && cacheable.isExpires()) {
                if (DEBUG)
                    Log.d(TAG, String.format("新创建的数据也是已经过期的 %s", cacheId.getClass().getName()));
            }
        }
        return cacheable;
    }

}
