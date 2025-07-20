package com.example.demo.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AsyncConfigurationTest {

    @Autowired
    private AsyncConfiguration asyncConfiguration;

    @Test
    void testAsyncTaskExecutorConfiguration() {
        // When
        Executor executor = asyncConfiguration.asyncTaskExecutor();

        // Then
        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(5, taskExecutor.getCorePoolSize());
        assertEquals(20, taskExecutor.getMaxPoolSize());
        assertEquals(100, taskExecutor.getQueueCapacity());
        assertTrue(taskExecutor.getThreadNamePrefix().startsWith("async-task-"));
        assertEquals(60, taskExecutor.getKeepAliveSeconds());
    }

    @Test
    void testAsyncConfigurationIsNotNull() {
        assertNotNull(asyncConfiguration);
    }
}