package com.backbase.accelerators.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class CacheCleanSchedulerServiceTest {

    @InjectMocks
    CacheCleanSchedulerService cacheCleanSchedulerService;

    @Mock
    CacheManager cacheManager;

    @Test
    void clearAllCache(){
        cacheCleanSchedulerService.clearAllCache();
        Mockito.verify(cacheManager, Mockito.times(1)).getCacheNames();
    }
}