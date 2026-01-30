package com.example.demo.virtual.service;

import com.example.demo.virtual.dto.VirtualThreadTaskDto;
import com.example.demo.virtual.exception.VirtualThreadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * 虚拟线程基础服务
 * 展示虚拟线程的基本用法和特性
 */
@Service
@Slf4j
public class VirtualThreadService {

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 执行基础虚拟线程任务
     */
    public VirtualThreadTaskDto executeBasicTask(String taskName, int delayMillis) {
        String taskId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        VirtualThreadTaskDto task = VirtualThreadTaskDto.builder()
                .taskId(taskId)
                .taskName(taskName)
                .status(VirtualThreadTaskDto.TaskStatus.PENDING)
                .createdAt(now)
                .build();

        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Thread currentThread = Thread.currentThread();
                task.setStartedAt(LocalDateTime.now());
                task.setStatus(VirtualThreadTaskDto.TaskStatus.RUNNING);
                task.setThreadName(currentThread.getName());
                task.setIsVirtualThread(currentThread.isVirtual());

                log.info("Task {} running on virtual thread: {}, isVirtual: {}",
                        taskName, currentThread.getName(), currentThread.isVirtual());

                try {
                    // 模拟工作负载
                    Thread.sleep(delayMillis);
                    task.setResult("Task completed successfully");
                    task.setStatus(VirtualThreadTaskDto.TaskStatus.COMPLETED);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    task.setStatus(VirtualThreadTaskDto.TaskStatus.FAILED);
                    task.setErrorMessage("Task interrupted: " + e.getMessage());
                }
            }, virtualThreadExecutor);

            // 等待任务完成
            future.join();

            task.setCompletedAt(LocalDateTime.now());
            task.setDurationMillis(java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).toMillis());

        } catch (Exception e) {
            task.setStatus(VirtualThreadTaskDto.TaskStatus.FAILED);
            task.setErrorMessage("Execution failed: " + e.getMessage());
            throw new VirtualThreadException("Failed to execute basic task", e);
        }

        return task;
    }

    /**
     * 批量执行虚拟线程任务
     * 展示虚拟线程的高并发能力
     */
    public List<VirtualThreadTaskDto> executeBatchTasks(int taskCount, int delayMillis) {
        List<VirtualThreadTaskDto> tasks = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(taskCount);

        log.info("Starting {} tasks with virtual threads", taskCount);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < taskCount; i++) {
            final int taskIndex = i;
            CompletableFuture<VirtualThreadTaskDto> future = CompletableFuture.supplyAsync(() -> {
                String taskId = UUID.randomUUID().toString();
                LocalDateTime now = LocalDateTime.now();

                VirtualThreadTaskDto task = VirtualThreadTaskDto.builder()
                        .taskId(taskId)
                        .taskName("Batch-Task-" + taskIndex)
                        .status(VirtualThreadTaskDto.TaskStatus.PENDING)
                        .createdAt(now)
                        .build();

                Thread currentThread = Thread.currentThread();
                task.setStartedAt(LocalDateTime.now());
                task.setStatus(VirtualThreadTaskDto.TaskStatus.RUNNING);
                task.setThreadName(currentThread.getName());
                task.setIsVirtualThread(currentThread.isVirtual());

                try {
                    // 模拟工作负载
                    Thread.sleep(delayMillis);
                    task.setResult("Task " + taskIndex + " completed");
                    task.setStatus(VirtualThreadTaskDto.TaskStatus.COMPLETED);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    task.setStatus(VirtualThreadTaskDto.TaskStatus.FAILED);
                    task.setErrorMessage("Task interrupted: " + e.getMessage());
                } finally {
                    task.setCompletedAt(LocalDateTime.now());
                    task.setDurationMillis(java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).toMillis());
                    latch.countDown();
                }

                return task;
            }, virtualThreadExecutor);

            tasks.add(null); // 占位
            future.thenAccept(task -> tasks.set(taskIndex, task));
        }

        try {
            // 等待所有任务完成
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VirtualThreadException("Batch execution interrupted", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("Completed {} tasks in {} ms", taskCount, endTime - startTime);

        return tasks;
    }

    /**
     * 模拟 Pin 线程场景
     * 使用 synchronized 块会导致虚拟线程被固定到载体线程
     */
    public VirtualThreadTaskDto simulatePinning(String taskName, int delayMillis) {
        String taskId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        VirtualThreadTaskDto task = VirtualThreadTaskDto.builder()
                .taskId(taskId)
                .taskName(taskName)
                .status(VirtualThreadTaskDto.TaskStatus.PENDING)
                .createdAt(now)
                .build();

        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Thread currentThread = Thread.currentThread();
                task.setStartedAt(LocalDateTime.now());
                task.setStatus(VirtualThreadTaskDto.TaskStatus.RUNNING);
                task.setThreadName(currentThread.getName());
                task.setIsVirtualThread(currentThread.isVirtual());

                log.info("Pin simulation task running on virtual thread: {}, isVirtual: {}",
                        currentThread.getName(), currentThread.isVirtual());

                // 使用 synchronized 会导致 Pin
                synchronized (this) {
                    log.info("Task {} entered synchronized block - PIN DETECTED", taskName);
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                task.setResult("Task with synchronized block completed");
                task.setStatus(VirtualThreadTaskDto.TaskStatus.COMPLETED);

            }, virtualThreadExecutor);

            future.join();

            task.setCompletedAt(LocalDateTime.now());
            task.setDurationMillis(java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).toMillis());

        } catch (Exception e) {
            task.setStatus(VirtualThreadTaskDto.TaskStatus.FAILED);
            task.setErrorMessage("Execution failed: " + e.getMessage());
            throw new VirtualThreadException("Failed to execute pin simulation task", e);
        }

        return task;
    }
}
