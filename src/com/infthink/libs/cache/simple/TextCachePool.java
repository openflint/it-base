package com.infthink.libs.cache.simple;

import com.infthink.libs.cache.expires.ExpiresCachePool;
import com.infthink.libs.cache.expires.TextCacheId;
import com.infthink.libs.cache.expires.TextCacheable;

public class TextCachePool extends ExpiresCachePool<TextCacheId, TextCacheable> {

    public TextCachePool(int maxSize) {
        super(maxSize);
    }

}
