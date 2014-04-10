package com.infthink.libs.cache.simple;

import com.infthink.libs.cache.expires.BitmapCacheId;
import com.infthink.libs.cache.expires.BitmapCacheable;
import com.infthink.libs.cache.expires.ExpiresCachePool;

public class BitmapCachePool extends ExpiresCachePool<BitmapCacheId, BitmapCacheable> {

    public BitmapCachePool(int maxSize) {
        super(maxSize);
    }

}
