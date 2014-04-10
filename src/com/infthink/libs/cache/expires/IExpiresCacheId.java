package com.infthink.libs.cache.expires;

import com.infthink.libs.cache.ICacheId;

/**
 * 对缓存的唯一标识
 */
public interface IExpiresCacheId<T extends IExpiresCacheable> extends ICacheId<T> {

}
