# sf-rest

Neutral Spring Boot auto-configuration for outbound REST clients.

## Features
- Auto-configures a `RestTemplate` with configurable timeouts.
- Optional OAuth Bearer token injection via a lightweight interceptor (supports a static token for demos/tests).
- Boot 3 style auto-configuration discovery.

Planned/optional (parity roadmap):
- HMAC header signature support.
- Circuit breaker wrappers (Resilience4j / Hystrix adapter) around HTTP calls.
- Error handling utilities and status mapping.

## Package
- Base: `com.teststrategy.multimodule.maven.sf.framework.rest`

## Dependency
```xml
<dependency>
  <groupId>com.teststrategy.multimodule.maven</groupId>
  <artifactId>sf-rest</artifactId>
  <version>${sf-rest.version}</version>
</dependency>
```

This module depends on `sf-core` for general framework base.

## Auto-configuration
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` registers:
- `RestAutoConfiguration`

## Configuration Properties
- `sample-framework.rest.http-client.connect-timeout` (Duration, default `5s`)
- `sample-framework.rest.http-client.read-timeout` (Duration, default `30s`)
- `sample-framework.rest.http-client.max-conn-total` (int, default `200`)
- `sample-framework.rest.http-client.max-conn-per-route` (int, default `50`)
- `sample-framework.rest.oauth.enabled` (boolean, default `false`)
- `sample-framework.rest.oauth.strategy` (string, default `static`)
- `sample-framework.rest.oauth.static-token` (string, default empty)

Example (application.yml):
```yaml
sample-framework:
  rest:
    http-client:
      connect-timeout: 2s
      read-timeout: 3s
    oauth:
      enabled: true
      static-token: ${EXAMPLE_STATIC_TOKEN:}
```

## Usage
Just add the dependency; the `RestTemplate` bean is created if none is defined already.
The token interceptor is attached automatically when `sample-framework.rest.oauth.enabled=true`.

```java
@RestController
class DemoController {
  private final RestTemplate restTemplate;
  DemoController(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

  @GetMapping("/call")
  public ResponseEntity<String> call() {
    return restTemplate.getForEntity("https://example.org", String.class);
  }
}
```

## Neutralization Notes
- No internal domains, credentials, or vendor-specific gateways are included.
- Any advanced gateway integration (e.g., corporate API gateway) must be modeled via public interfaces and configured externally.

## Tests
- `RestAutoConfigurationTest` verifies RestTemplate creation and conditional token interceptor wiring.

## License
- Uses only OSS dependencies compatible with the repository policy.


## Additional Properties
- Forwarding headers:
  - `sample-framework.rest.forward-headers.enabled` (boolean, default `true`)
  - `sample-framework.rest.forward-headers.names` (list of header names to forward; defaults include `Authorization`, `USER-ID`, `PROGRAM-ID`, `Forwarded-Service`, `Global-Transaction-ID`)
- Correlation/transaction header:
  - `sample-framework.rest.correlation.enabled` (boolean, default `true`)
  - `sample-framework.rest.correlation.header-name` (string, default `Global-Transaction-ID`)
- HMAC signature:
  - `sample-framework.rest.hmac.enabled` (boolean, default `false`)
  - `sample-framework.rest.hmac.key-id` (string; identifier)
  - `sample-framework.rest.hmac.secret` (string; provide via env/secret store)
  - `sample-framework.rest.hmac.header-name` (string, default `Header-Authorization`)

## Domain configuration loading (sf-core)
A neutral environment initializer in `sf-core` loads a domain YAML into the Spring Environment when configured.
- Property: `sample-framework.rest.domain.config`
  - If it points to a directory, `domain.yml` inside it will be loaded
  - If it points to a YAML file, that exact file will be loaded

Example:
```properties
sample-framework.rest.domain.config=/etc/myapp/config
```
