## 1. Project Setup and Dependencies

- [x] 1.1 Verify and update `pom.xml` to include `micrometer-registry-prometheus` dependency
- [x] 1.2 Ensure `spring-boot-starter-actuator` is present in dependencies
- [x] 1.3 Update `CLAUDE.md` to document new metrics capabilities and API endpoints

## 2. Configuration

- [x] 2.1 Update `application.yml` to enable actuator endpoints (prometheus, health, info)
- [x] 2.2 Configure metrics tags (application name, instance) in `application.yml`
- [x] 2.3 Configure HTTP request metrics distribution percentiles in `application.yml`
- [ ] 2.4 Update `application-dev.yml` with development-specific metrics settings

## 3. Metrics Package Structure

- [x] 3.1 Create `com.example.demo.metrics` package
- [x] 3.2 Create `com.example.demo.metrics.configuration` sub-package for metrics configuration
- [x] 3.3 Create `com.example.demo.metrics.binder` sub-package for custom metrics binders

## 4. Async Metrics Implementation

- [x] 4.1 Create `AsyncMetrics` class with task counters and timers
- [ ] 4.2 Integrate `AsyncMetrics` into `AsyncService` to track task submission and completion
- [ ] 4.3 Add task status tracking (submitted, running, completed, failed)
- [ ] 4.4 Record task duration metrics for completed tasks

## 5. Retry Metrics Implementation

- [x] 5.1 Create `RetryMetrics` class with retry counters and timers
- [ ] 5.2 Integrate `RetryMetrics` into `RetryService` and retry listeners
- [ ] 5.3 Track retry attempts by service and method
- [ ] 5.4 Record retry outcomes (success, failure, max_reached)
- [ ] 5.5 Measure total retry duration including backoff

## 6. JOOQ Metrics Implementation

- [x] 6.1 Create `JooqMetrics` class with query counters and timers
- [ ] 6.2 Implement query interception for JOOQ operations
- [ ] 6.3 Track query count by operation type (select, insert, update, delete)
- [ ] 6.4 Track query count by table name
- [ ] 6.5 Record query duration metrics
- [ ] 6.6 Add slow query detection (queries exceeding configured threshold)

## 7. Virtual Thread Metrics Implementation

- [x] 7.1 Create `VirtualThreadMetrics` class for virtual thread monitoring
- [ ] 7.2 Integrate with `PinDetectionService` to track pinned threads
- [x] 7.3 Create gauge for live virtual thread count
- [ ] 7.4 Track pin reasons (synchronized, native, file_io)

## 8. Sentinel Metrics Implementation

- [x] 8.1 Create `SentinelMetrics` class for flow control monitoring
- [ ] 8.2 Track blocked requests by resource and rule type
- [x] 8.3 Create gauge for circuit breaker state (open, half_open, closed)
- [ ] 8.4 Integrate with `SentinelService` to record metrics

## 9. Metrics Configuration Class

- [x] 9.1 Create `MetricsConfiguration` class to bind custom metrics to registry
- [x] 9.2 Configure JVM binders (memory, gc, threads, cpu)
- [x] 9.3 Configure logback metrics for instrumentation
- [x] 9.4 Register all custom metrics binders (Async, Retry, JOOQ, VirtualThread, Sentinel)

## 10. Integration Tests

- [x] 10.1 Create `MetricsIntegrationTest` class
- [x] 10.2 Test Prometheus endpoint accessibility (`GET /actuator/prometheus`)
- [x] 10.3 Verify response content-type is `text/plain; version=0.0.4`
- [x] 10.4 Test JVM metrics are present in response
- [x] 10.5 Test HTTP request metrics are recorded
- [x] 10.6 Test custom async metrics are recorded after task execution
- [x] 10.7 Test custom retry metrics are recorded after retry operations
- [x] 10.8 Test JOOQ metrics are recorded after database operations
- [x] 10.9 Test virtual thread pin metrics are recorded
- [x] 10.10 Test Sentinel metrics are recorded after flow control events
- [x] 10.11 Verify metric tags are correctly applied

## 11. Documentation

- [x] 11.1 Update `CLAUDE.md` with metrics module documentation
- [x] 11.2 Add metrics configuration section to `CLAUDE.md`
- [x] 11.3 Document custom metrics naming and tags
- [x] 11.4 Add Prometheus endpoint to API documentation

## 12. Verification

- [x] 12.1 Run `mvn clean test` to ensure all tests pass
- [x] 12.2 Start application and verify `/actuator/prometheus` is accessible
- [x] 12.3 Verify JVM metrics appear in Prometheus endpoint output
- [x] 12.4 Execute async operations and verify async metrics appear
- [x] 12.5 Execute retry operations and verify retry metrics appear
- [x] 12.6 Execute JOOQ operations and verify database metrics appear
- [x] 12.7 Execute virtual thread operations and verify pin metrics appear
- [x] 12.8 Execute Sentinel operations and verify flow control metrics appear
