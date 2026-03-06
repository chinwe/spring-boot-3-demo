## ADDED Requirements

### Requirement: JVM Metrics Collection
The system SHALL automatically collect JVM metrics including heap memory usage, GC pauses, thread counts, and CPU utilization through Micrometer.

#### Scenario: JVM heap memory is tracked
- **WHEN** the application is running
- **THEN** system SHALL expose `jvm_memory_used_bytes` metric with tags `area` (heap/nonheap) and `id` (memory pool name)

#### Scenario: GC pauses are tracked
- **WHEN** garbage collection occurs
- **THEN** system SHALL expose `jvm_gc_pause_seconds` metric with tags `gc` (GC name) and `action` (end of minor GC/major GC)

### Requirement: HTTP Request Metrics
The system SHALL automatically record HTTP request metrics including request count, response time, and active requests.

#### Scenario: HTTP requests are counted
- **WHEN** any HTTP endpoint is called
- **THEN** system SHALL increment `http_server_requests` counter with tags `method`, `uri`, `status`, and `exception`

#### Scenario: HTTP request latency is measured
- **WHEN** an HTTP request completes
- **THEN** system SHALL record request duration in `http_server_requests` timer metric

### Requirement: Custom Async Task Metrics
The system SHALL provide custom metrics for asynchronous task execution including task count, success/failure rates, and execution duration.

#### Scenario: Async task execution is tracked
- **WHEN** an async task is submitted via AsyncService
- **THEN** system SHALL increment `async_tasks_total` counter with tags `status` (submitted/running/completed/failed)

#### Scenario: Async task duration is measured
- **WHEN** an async task completes
- **THEN** system SHALL record execution time in `async_task_duration_seconds` timer metric

### Requirement: Custom Retry Metrics
The system SHALL provide metrics for retry operations including retry attempts, successes, and failures.

#### Scenario: Retry attempts are counted
- **WHEN** a retry operation is attempted
- **THEN** system SHALL increment `retry_attempts_total` counter with tags `service`, `method`, and `outcome` (success/failure/max_reached)

#### Scenario: Retry backoff is measured
- **WHEN** a retry includes backoff delay
- **THEN** system SHALL record total retry duration including backoff in `retry_duration_seconds` timer

### Requirement: Custom Database Metrics
The system SHALL provide metrics for JOOQ database operations including query count, slow queries, and connection pool usage.

#### Scenario: Database queries are counted
- **WHEN** a JOOQ query is executed
- **THEN** system SHALL increment `jooq_queries_total` counter with tags `operation` (select/insert/update/delete) and `table`

#### Scenario: Query duration is measured
- **WHEN** a database query completes
- **THEN** system SHALL record query time in `jooq_query_duration_seconds` timer metric

### Requirement: Virtual Thread Metrics
The system SHALL provide metrics for virtual thread usage including pinned thread detection and thread counts.

#### Scenario: Virtual thread pinning is detected
- **WHEN** a virtual thread is pinned to a carrier thread
- **THEN** system SHALL increment `virtual_thread_pins_total` counter with tag `reason` (synchronized/native/file_io)

#### Scenario: Virtual thread count is tracked
- **WHEN** virtual threads are created/destroyed
- **THEN** system SHALL report live virtual thread count in `jvm_threads_live_threads` gauge with tag `type` (virtual)

### Requirement: Sentinel Flow Control Metrics
The system SHALL provide metrics for Sentinel flow control including blocked requests and degradation events.

#### Scenario: Blocked requests are counted
- **WHEN** a request is blocked by Sentinel flow control
- **THEN** system SHALL increment `sentinel_blocked_total` counter with tags `resource` and `rule_type`

#### Scenario: Circuit breaker state changes are tracked
- **WHEN** a circuit breaker opens or closes
- **THEN** system SHALL update `sentinel_circuit_state` gauge with tags `resource` and `state` (open/half_open/closed)

### Requirement: Metrics Configuration
The system SHALL allow metrics configuration via application properties without code changes for common settings.

#### Scenario: Metrics can be enabled/disabled
- **WHEN** `management.metrics.enable` property is configured
- **THEN** system SHALL respect the configuration for specified metrics

#### Scenario: Metric distribution percentiles can be configured
- **WHEN** `management.metrics.distribution.percentiles` is set
- **THEN** system SHALL publish configured percentiles for timer metrics
