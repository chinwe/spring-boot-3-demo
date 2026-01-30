package com.example.demo.virtual.service;

import com.example.demo.virtual.dto.PinDetectionReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pin 检测服务测试
 */
class PinDetectionServiceTest {

    private PinDetectionService service;

    @BeforeEach
    void setUp() {
        service = new PinDetectionService();
    }

    @Test
    void testDetectPinnedThreads() {
        PinDetectionReport report = service.detectPinnedThreads();

        assertNotNull(report);
        assertNotNull(report.getReportId());
        assertNotNull(report.getStartTime());
        assertNotNull(report.getEndTime());
        assertNotNull(report.getPinEvents());
        assertEquals(report.getTotalPinEvents(), report.getPinEvents().size());
    }

    @Test
    void testTestSynchronizedPin() {
        List<PinDetectionReport.PinEvent> events = service.testSynchronizedPin(5, 100);

        assertNotNull(events);
        assertFalse(events.isEmpty());

        PinDetectionReport.PinEvent event = events.get(0);
        assertEquals(PinDetectionReport.PinType.SYNCHRONIZED_BLOCK, event.getPinType());
        assertNotNull(event.getPinLocation());
    }

    @Test
    void testTestNativeCodePin() {
        PinDetectionReport.PinEvent event = service.testNativeCodePin();

        assertNotNull(event);
        assertEquals(PinDetectionReport.PinType.NATIVE_METHOD, event.getPinType());
        assertNotNull(event.getPinLocation());
    }

    @Test
    void testTestFileIOPin() {
        List<PinDetectionReport.PinEvent> events = service.testFileIOPin();

        assertNotNull(events);
        assertFalse(events.isEmpty());

        PinDetectionReport.PinEvent event = events.get(0);
        assertEquals(PinDetectionReport.PinType.FILE_IO, event.getPinType());
    }

    @Test
    void testPinDetectionWithSmallTaskCount() {
        List<PinDetectionReport.PinEvent> events = service.testSynchronizedPin(2, 50);

        assertNotNull(events);
        assertEquals(1, events.size()); // 应该产生一个汇总事件
    }

    @Test
    void testPinDetectionWithLargeTaskCount() {
        List<PinDetectionReport.PinEvent> events = service.testSynchronizedPin(20, 50);

        assertNotNull(events);
        assertEquals(1, events.size());
    }
}
