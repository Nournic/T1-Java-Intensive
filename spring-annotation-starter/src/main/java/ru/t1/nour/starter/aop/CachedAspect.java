package ru.t1.nour.starter.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import ru.t1.nour.starter.aop.annotation.Cached;

@Aspect
@Slf4j
@RequiredArgsConstructor
@Component
public class CachedAspect {
    private final CacheManager cacheManager;

    @Around("@annotation(ru.t1.nour.starter.aop.annotation.Cached)")
    public Object cache(ProceedingJoinPoint jp, Cached cachedAnnotation) throws Throwable {
        String cacheName = cachedAnnotation.cacheName();
        Cache cache = cacheManager.getCache(cacheName);

        if(cache == null){
            log.warn("Cache {} not found. Executing method without caching.", cacheName);
            return jp.proceed();
        }

        Object[] args = jp.getArgs();
        if (args.length == 0)
            return jp.proceed();

        Object key = args[0];

        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper != null) {
            log.info("Cache HIT for cache '{}', key '{}'", cacheName, key);

            return valueWrapper.get();
        }

        log.info("Cache MISS for cache '{}', key '{}'", cacheName, key);
        Object result = jp.proceed();

        if (result != null)
            cache.put(key, result);

        return result;
    }
}
