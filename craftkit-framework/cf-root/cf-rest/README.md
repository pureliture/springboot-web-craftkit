# sf-rest

Neutral Spring Boot auto-configuration for outbound REST clients.

## Features
- Auto-configures a `RestTemplate` with configurable timeouts.
- Optional OAuth Bearer token injection via a lightweight interceptor (supports a static token for demos/tests).
- Boot 3 style auto-configuration discovery.

Planned/optional (parity roadmap):
- HMAC header signature support.
- Circuit breaker wrappers (Resilience4j) around HTTP calls.
- Error handling utilities and status mapping.

## Package
- Base: `com.springboot.craftkit.sf.framework.rest`

## Dependency
```xml
<dependency>
  <groupId>com.springboot.craftkit</groupId>
  <artifactId>sf-rest</artifactId>
  <version>${sf-rest.version}</version>
</dependency>
```

This module depends on `sf-core` for general framework base.

## Auto-configuration
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` registers:
- `RestAutoConfiguration`

## Configuration Properties
- `sf-rest.http-client.connect-timeout` (Duration, default `5s`)
- `sf-rest.http-client.read-timeout` (Duration, default `30s`)
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

## Domain API configuration (sf-core)
To describe per-domain API paths and resolve `{@domain.api}` placeholders in URI templates, provide a YAML and set:
- Property: `sample-framework.rest.domain.api.config` → path to `domain-api.yml`

YAML example:
```yaml
# domain-api.yml
example:
  listUsers:
    url: /users?page={page}
api-gw:
  resource:
    url: "{@api-gw-pv}/resource?statusCode={statusCode}"
```

When configured, `sf-rest` auto-config exports beans:
- `DomainProperties` (reads `domain.yml` services)
- `DomainApiProperties` (reads `domain-api.yml` and resolves `{@domain.api}` in URIs)


## Automatic URI placeholder resolution
When domain configuration is provided, `sf-rest` will automatically resolve URI placeholders in `RestTemplate` calls:
- `{@domain}` → resolved using `domain.yml` from `sample-framework.rest.domain.config`
- `{@domain.api}` → resolved using `domain-api.yml` from `sample-framework.rest.domain.api.config`
- `${...}` → standard Spring Environment placeholders are resolved as well

This resolution happens transparently via a `UriTemplateHandler` attached to the auto-configured `RestTemplate`.

Prerequisites:
- Provide a domain YAML and point to it:
  ```properties
  sample-framework.rest.domain.config=classpath:config/domain.yml
  ```
  Example YAML:
  ```yaml
  services:
    demo:
      url: http://localhost:8081
    apim-pv:
      url: http://localhost:8081/apim
  ```
- Optionally, provide a domain API mapping YAML for `{@domain.api}` shortcuts:
  ```properties
  sample-framework.rest.domain.api.config=classpath:config/domain-api.yml
  ```
  Example YAML:
  ```yaml
  apim:
    resource:
      url: "{@apim-pv}/resource?statusCode={statusCode}"
  ```

Usage examples:
```java
// 1) Using {@domain.api}
URI uri = restTemplate.getUriTemplateHandler()
    .expand("{@apim.resource}", Map.of("statusCode", "200"));
// -> http://localhost:8081/apim/resource?statusCode=200

// 2) Using {@domain} + path
URI uri2 = restTemplate.getUriTemplateHandler()
    .expand("{@demo}/users/{id}", Map.of("id", 10));
// -> http://localhost:8081/users/10

// 3) Environment placeholders are also resolved
URI uri3 = restTemplate.getUriTemplateHandler()
    .expand("http://example.org/ping?x=${test.value}");
```

Notes:
- If a placeholder cannot be resolved (unknown domain or API), a clear `DomainUriMappingException` is thrown indicating which configuration to check.
- For bulk requests (URIs containing ":bulkProcess"), if a service defines a `bsvUrl`, it will be used automatically.



## Advanced business error handler (optional)
This module can detect business-level errors embedded in 2xx JSON responses and raise a consistent exception instead of returning a "successful" response with an error payload.

- When enabled, a lightweight `ClientHttpRequestInterceptor` parses JSON responses and checks a configured code field.
- If the code is not in the allowed success list, a `BusinessErrorException` (extends `RestClientException`) is thrown. The exception carries the HTTP status, the parsed business code/message, and the raw body (truncated for safety).

Properties (all optional; feature is disabled by default):
- `sample-framework.rest.error-handler.enabled` (boolean; default `false`)
- `sample-framework.rest.error-handler.json-path.code` (string; default `code`) — dot-path to code field
- `sample-framework.rest.error-handler.json-path.message` (string; default `message`) — dot-path to message field
- `sample-framework.rest.error-handler.success-codes` (set; default `OK,SUCCESS,0000,0`)
- `sample-framework.rest.error-handler.inspect-content-types` (set; default includes `application/json`, `application/*+json`)
- `sample-framework.rest.error-handler.only-on-2xx` (boolean; default `true`)
- `sample-framework.rest.error-handler.empty-body-is-success` (boolean; default `true`)

Example configuration:
```yaml
sample-framework:
  rest:
    error-handler:
      enabled: true
      json-path:
        code: $.code        # simple dot path; `$` is optional
        message: $.message
      success-codes: OK,SUCCESS,0000,0
```

Example behavior:
```java
// Response body: {"code":"ERROR","message":"invalid argument"}
// HTTP status: 200 OK

try {
  ResponseEntity<String> res = restTemplate.getForEntity("https://example.org", String.class);
} catch (BusinessErrorException ex) {
  // ex.getBusinessCode() == "ERROR"
  // ex.getBusinessMessage() == "invalid argument"
}
```

Notes:
- JSON-only: non-JSON responses are ignored by the interceptor.
- The interceptor buffers the response so the body remains readable by message converters when it is not an error.
- The interceptor is attached automatically to the auto-configured `RestTemplate` when the feature is enabled.



## HTTP client–based retry (HttpClient5)
This module supports an opt-in retry mechanism implemented at the HTTP client layer using Apache HttpClient 5. It does not use Spring Retry.

- Disabled by default for backward compatibility.
- Retries occur only for selected HTTP methods (idempotent by default) and for network I/O errors and/or configured HTTP statuses (5xx by default).
- Backoff can be fixed or exponential, with an optional cap and `Retry-After` header support.

Properties (all optional; prefix `sample-framework.rest.http-client.retry`):
```yaml
sample-framework:
  rest:
    http-client:
      retry:
        enabled: true                 # default: false
        max-attempts: 3               # total attempts including the first
        interval: 200ms               # base delay
        backoff:
          strategy: exponential       # fixed | exponential (default: exponential)
          multiplier: 2.0             # for exponential
        max-interval: 2s              # optional cap
        retry-on-statuses: 500,502,503,504  # default: all 5xx when unspecified
        retry-on-io-exceptions: true  # default: true
        methods: GET,HEAD,OPTIONS     # idempotent by default
        respect-retry-after: true     # honor Retry-After header if present
        retry-sent-nonidempotent: false # keep false; unsafe to retry POST/PUT typically
```

Notes:
- When enabled, `sf-rest` switches the `RestTemplate` request factory to an `HttpComponentsClientHttpRequestFactory` backed by a pooled `CloseableHttpClient` with the configured retry strategy.
- Interceptor order remains unchanged; retries happen at the client layer. The advanced business error handler (if enabled) observes the final response after retries.
- Use conservative defaults (idempotent methods only) unless you have strong guarantees about server behavior and request repeatability.

## Circuit breaker (Resilience4j)
Provides an opt-in Circuit Breaker around outbound HTTP calls using Resilience4j. Disabled by default.

- Instance naming strategy:
  - `domain-api` (default): if the URI template used `{@domain.api}`, that identifier (e.g., `apim.resource`) becomes the circuit breaker instance id.
  - `uri`: fallback naming `METHOD host[:port]` (e.g., `GET example.org:8080`).
- Ordering: Retry (HttpClient5) → Circuit Breaker → Business Error Handler.
- Configuration is neutral and cooperates with standard `resilience4j.circuitbreaker.*` properties.

Properties (prefix `sample-framework.rest.circuitbreaker`):
```yaml
sample-framework:
  rest:
    circuitbreaker:
      enabled: true              # default: false
      instance-from: domain-api  # domain-api | uri
      default-config: default    # optional base config name in resilience4j registry
      ignore-exceptions: client.rest.framework.sf.com.springboot.craftkit.BusinessErrorException
      # record-exceptions: java.io.IOException,org.springframework.web.client.ResourceAccessException

resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 2
        minimumNumberOfCalls: 2
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
    # Instances can inherit the base config
    instances:
      apim.resource:
        baseConfig: default
```

Notes:
- If `default-config` is set but not found in the registry, the interceptor falls back to default CB config.
- Exceptions listed in `ignore-exceptions` are treated as success from the CB’s perspective (not increasing failure rate).
- You can still fully control per-instance behavior via `resilience4j.circuitbreaker.instances.<id>.*` keys.
