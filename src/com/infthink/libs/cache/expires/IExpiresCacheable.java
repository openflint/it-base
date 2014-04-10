package com.infthink.libs.cache.expires;

import com.infthink.libs.cache.ICacheable;

/**
 * 可内存缓存实体
 */
public interface IExpiresCacheable extends ICacheable {

    /**
     * 判断缓存是否过期
     * @return
     */
    public boolean isExpires();

}
