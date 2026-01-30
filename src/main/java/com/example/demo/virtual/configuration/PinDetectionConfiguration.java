package com.example.demo.virtual.configuration;

import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pin 检测配置类
 * 配置 JFR 录制以检测虚拟线程被固定的场景
 */
@Configuration
public class PinDetectionConfiguration {

    /**
     * Pin 检测开关
     */
    private final AtomicBoolean pinDetectionEnabled = new AtomicBoolean(
        Boolean.getBoolean("jfr.enabled") || Boolean.getBoolean("pin.detection.enabled")
    );

    /**
     * Pin 检测属性配置
     */
    @Bean
    public PinDetectionProperties pinDetectionProperties() {
        return new PinDetectionProperties();
    }

    /**
     * JFR 录制 Bean（可选）
     * 当 Pin 检测启用时创建
     */
    @Bean(name = "pinDetectionRecording", destroyMethod = "close")
    public Recording pinDetectionRecording(PinDetectionProperties properties) throws IOException {
        if (!pinDetectionEnabled.get()) {
            return null;
        }

        Recording recording = new Recording();
        // 启用虚拟线程 Pin 事件检测
        recording.enable("jdk.VirtualThreadPinned");
        recording.enable("jdk.VirtualThreadStart");
        recording.enable("jdk.VirtualThreadEnd");

        // 设置最大大小和持续时间
        if (properties.getMaxSize() > 0) {
            recording.setMaxSize(properties.getMaxSize());
        }
        if (properties.getDuration() > 0) {
            recording.setDuration(java.time.Duration.ofSeconds(properties.getDuration()));
        }

        // 设置输出路径
        if (properties.getOutputPath() != null) {
            recording.setDestination(Paths.get(properties.getOutputPath()));
        }

        recording.start();
        return recording;
    }

    /**
     * 检查 Pin 检测是否启用
     */
    public boolean isPinDetectionEnabled() {
        return pinDetectionEnabled.get();
    }

    /**
     * 启用 Pin 检测
     */
    public void enablePinDetection() {
        pinDetectionEnabled.set(true);
    }

    /**
     * 禁用 Pin 检测
     */
    public void disablePinDetection() {
        pinDetectionEnabled.set(false);
    }

    /**
     * Pin 检测属性配置类
     */
    public static class PinDetectionProperties {
        private long maxSize = 100 * 1024 * 1024; // 100MB
        private long duration = 60; // 60 seconds
        private String outputPath = "./pin-detection.jfr";

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public void setOutputPath(String outputPath) {
            this.outputPath = outputPath;
        }
    }
}
