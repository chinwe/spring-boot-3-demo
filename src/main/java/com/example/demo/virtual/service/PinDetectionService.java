package com.example.demo.virtual.service;

import com.example.demo.virtual.dto.PinDetectionReport;
import com.example.demo.virtual.exception.PinDetectedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pin 检测服务
 * 检测和报告虚拟线程被固定的场景
 */
@Service
@Slf4j
public class PinDetectionService {

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 检测 Pin 线程事件
     * 通过运行多个可能导致 Pin 的任务来检测
     */
    public PinDetectionReport detectPinnedThreads() {
        String reportId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("Starting pin detection with report ID: {}", reportId);

        List<PinDetectionReport.PinEvent> detectedEvents = new ArrayList<>();

        // 测试 synchronized 导致的 Pin
        detectedEvents.addAll(testSynchronizedPin(10, 100));

        // 测试文件 I/O 可能导致的 Pin
        detectedEvents.addAll(testFileIOPin());

        LocalDateTime endTime = LocalDateTime.now();
        long durationMillis = java.time.Duration.between(startTime, endTime).toMillis();

        PinDetectionReport report = PinDetectionReport.builder()
                .reportId(reportId)
                .startTime(startTime)
                .endTime(endTime)
                .durationMillis(durationMillis)
                .pinEvents(detectedEvents)
                .totalPinEvents(detectedEvents.size())
                .build();

        log.info("Pin detection completed. Total pin events detected: {}", detectedEvents.size());

        return report;
    }

    /**
     * 测试 synchronized 导致的 Pin
     */
    public List<PinDetectionReport.PinEvent> testSynchronizedPin(int taskCount, int delayMillis) {
        List<PinDetectionReport.PinEvent> events = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(taskCount);
        Object lock = new Object();

        log.info("Testing synchronized pin with {} tasks", taskCount);

        for (int i = 0; i < taskCount; i++) {
            final int taskIndex = i;
            CompletableFuture.runAsync(() -> {
                Thread currentThread = Thread.currentThread();

                log.info("Task {} running on thread: {}, isVirtual: {}",
                        taskIndex, currentThread.getName(), currentThread.isVirtual());

                // synchronized 会导致虚拟线程被固定
                synchronized (lock) {
                    log.info("Task {} entered synchronized block - PIN OCCURRED", taskIndex);
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                latch.countDown();
            }, virtualThreadExecutor);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 记录 Pin 事件
        events.add(PinDetectionReport.PinEvent.builder()
                .eventTime(LocalDateTime.now())
                .pinLocation("synchronized block in PinDetectionService.testSynchronizedPin")
                .durationMillis((long) taskCount * delayMillis)
                .threadName(Thread.currentThread().getName())
                .pinType(PinDetectionReport.PinType.SYNCHRONIZED_BLOCK)
                .description("synchronized block caused virtual thread pinning")
                .build());

        return events;
    }

    /**
     * 测试本地代码调用导致的 Pin
     */
    public PinDetectionReport.PinEvent testNativeCodePin() {
        log.info("Testing native code pin");

        Thread currentThread = Thread.currentThread();
        boolean isVirtual = currentThread.isVirtual();

        // 调用本地方法（如 System.gc()）可能导致 Pin
        // 注意：这是一个简化的示例，实际的本地方法调用 Pin 检测需要 JFR

        System.gc();

        return PinDetectionReport.PinEvent.builder()
                .eventTime(LocalDateTime.now())
                .pinLocation("System.gc() in PinDetectionService.testNativeCodePin")
                .durationMillis(-1L)
                .threadName(currentThread.getName())
                .pinType(PinDetectionReport.PinType.NATIVE_METHOD)
                .description("Native method call (System.gc) - potential pin detected")
                .build();
    }

    /**
     * 测试文件 I/O 导致的 Pin
     */
    public List<PinDetectionReport.PinEvent> testFileIOPin() {
        List<PinDetectionReport.PinEvent> events = new ArrayList<>();

        log.info("Testing file I/O pin");

        try {
            // 创建临时文件进行 I/O 操作
            Path tempFile = Files.createTempFile("pin-test", ".txt");

            CompletableFuture.runAsync(() -> {
                Thread currentThread = Thread.currentThread();
                log.info("File I/O task running on thread: {}, isVirtual: {}",
                        currentThread.getName(), currentThread.isVirtual());

                try {
                    // 文件 I/O 可能导致 Pin（取决于实现）
                    Files.writeString(tempFile, "Test content for pin detection");
                    String content = Files.readString(tempFile);
                    log.info("Read content: {}", content);
                } catch (IOException e) {
                    log.error("File I/O error", e);
                } finally {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException e) {
                        log.error("Failed to delete temp file", e);
                    }
                }
            }, virtualThreadExecutor).join();

            events.add(PinDetectionReport.PinEvent.builder()
                    .eventTime(LocalDateTime.now())
                    .pinLocation("Files.write/read in PinDetectionService.testFileIOPin")
                    .durationMillis(-1L)
                    .threadName(Thread.currentThread().getName())
                    .pinType(PinDetectionReport.PinType.FILE_IO)
                    .description("File I/O operations - potential pin detected")
                    .build());

        } catch (IOException e) {
            log.error("Failed to create temp file for pin test", e);
        }

        return events;
    }

    /**
     * 测试 Pin 并抛出异常（如果检测到）
     */
    public void testPinAndThrowException() throws PinDetectedException {
        log.info("Testing pin detection with exception");

        Object lock = new Object();

        CompletableFuture.runAsync(() -> {
            Thread currentThread = Thread.currentThread();
            log.info("Pin test running on: {}, isVirtual: {}",
                    currentThread.getName(), currentThread.isVirtual());

            // synchronized 会导致 Pin
            synchronized (lock) {
                log.warn("PIN DETECTED: Virtual thread pinned in synchronized block");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, virtualThreadExecutor).join();

        // 在实际场景中，这里应该检查 JFR 事件或使用其他检测机制
        // 这里我们模拟检测到 Pin 的情况
        throw new PinDetectedException(
                "Virtual thread pinning detected",
                "synchronized block in testPinAndThrowException",
                100L
        );
    }
}
