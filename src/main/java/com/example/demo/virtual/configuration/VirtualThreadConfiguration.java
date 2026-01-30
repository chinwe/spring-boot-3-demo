package com.example.demo.virtual.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 虚拟线程配置类
 * 提供虚拟线程执行器和线程工厂 Bean
 */
@Configuration
public class VirtualThreadConfiguration {

    /**
     * 虚拟线程执行器 Bean
     * 每个任务使用一个新的虚拟线程
     */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 虚拟线程工厂 Bean
     * 用于自定义虚拟线程配置
     */
    @Bean(name = "virtualThreadFactory")
    public ThreadFactory virtualThreadFactory() {
        return Thread.ofVirtual().factory();
    }
}
