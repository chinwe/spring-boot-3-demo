## ADDED Requirements

### Requirement: Prometheus Endpoint Exposure
The system SHALL expose a Prometheus-compatible metrics endpoint at `/actuator/prometheus` that returns metrics in Prometheus text format.

#### Scenario: Endpoint returns Prometheus format
- **WHEN** a GET request is made to `/actuator/prometheus`
- **THEN** system SHALL respond with HTTP 200 and content-type `text/plain; version=0.0.4; charset=utf-8`
- **AND** response SHALL contain metrics in Prometheus exposition format

#### Scenario: Endpoint is accessible in development
- **WHEN** application runs with `dev` profile
- **THEN** `/actuator/prometheus` endpoint SHALL be accessible without authentication
- **AND** endpoint SHALL be included in `management.endpoints.web.exposure.include`

### Requirement: Endpoint Security Configuration
The system SHALL support security configuration for the Prometheus endpoint in production environments.

#### Scenario: Production access can be restricted
- **WHEN** `management.endpoints.web.exposure.include` excludes prometheus
- **THEN** `/actuator/prometheus` endpoint SHALL return HTTP 404
- **AND** metrics SHALL still be collected internally

#### Scenario: Endpoint respects actuator port configuration
- **WHEN** `management.server.port` is configured separately from application port
- **THEN** Prometheus endpoint SHALL be available on the management port only

### Requirement: Metric Scrape Support
The system SHALL support Prometheus scrape patterns including instance labeling and metric filtering.

#### Scenario: Metrics include instance label
- **WHEN** metrics are scraped
- **THEN** each metric SHALL include a `instance` label with host:port
- **AND** each metric SHALL include a `application` label with the application name

#### Scenario: Empty metrics are handled gracefully
- **WHEN** no custom metrics have been recorded yet
- **THEN** `/actuator/prometheus` SHALL still return JVM and HTTP metrics
- **AND** response SHALL not be empty

### Requirement: Endpoint Performance
The system SHALL ensure the Prometheus endpoint responds quickly to avoid scrape timeouts.

#### Scenario: Endpoint response time is acceptable
- **WHEN** Prometheus scrapes metrics
- **THEN** response time SHALL be under 5 seconds for typical workloads
- **AND** endpoint SHALL not block on metric calculation

#### Scenario: Large metric volumes are handled
- **WHEN** application has many custom metrics
- **THEN** endpoint SHALL stream response without buffering all metrics in memory

### Requirement: Health Check Integration
The system SHALL integrate with Actuator health endpoint to report metrics collection status.

#### Scenario: Health endpoint includes metrics status
- **WHEN** `/actuator/health` is queried
- **THEN** response MAY include metrics binders status if enabled
- **AND** status SHALL indicate if Micrometer is functioning correctly

### Requirement: Actuator Configuration
The system SHALL expose Prometheus endpoint through standard Actuator configuration.

#### Scenario: Actuator is properly configured
- **WHEN** application starts
- **THEN** `management.endpoints.web.base-path` SHALL default to `/actuator`
- **AND** `management.endpoints.web.exposure.include` SHALL contain `prometheus,health,info`

#### Scenario: Prometheus registry is auto-configured
- **WHEN** `micrometer-registry-prometheus` is on classpath
- **THEN** Spring Boot SHALL auto-configure `PrometheusMeterRegistry`
- **AND** MeterRegistry SHALL be available for injection via `@Autowired`
