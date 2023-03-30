package com.backbase.accelerators.service;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        value= {"scheduled-batch.enableCache"},
        havingValue = "true",
        matchIfMissing = false
)
@RequiredArgsConstructor
public class CacheCleanSchedulerService {

    private CacheManager cacheManager;

    @Scheduled(cron = "${scheduled-batch.clearAllCache:0 0 0 * * ?}")
    public void clearAllCache(){
        cacheManager.getCacheNames().stream()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());           // clear cache by name

    }
}
