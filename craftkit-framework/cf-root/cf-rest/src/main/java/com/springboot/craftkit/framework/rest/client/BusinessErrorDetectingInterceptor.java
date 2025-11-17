package com.springboot.craftkit.framework.rest.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.craftkit.framework.rest.setting.ErrorHandlerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * Intercepts responses and inspects 2xx JSON bodies for business error signals.
 * Throws {@link BusinessErrorException} when detected.
 */
public class BusinessErrorDetectingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(BusinessErrorDetectingInterceptor.class);

    private final ErrorHandlerProperties properties;
    private final ObjectMapper objectMapper;

    public BusinessErrorDetectingInterceptor(ErrorHandlerProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
    }

    @Override
    public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        if (!properties.isEnabled()) {
            return response;
        }

        HttpStatusCode status = response.getStatusCode();
        if (properties.isOnlyOn2xx() && !status.is2xxSuccessful()) {
            return response; // let DefaultResponseErrorHandler handle non-2xx
        }

        HttpHeaders headers = response.getHeaders();
        MediaType contentType = headers.getContentType();
        byte[] bytes = toByteArray(response.getBody());
        String bodyString = bytesToString(bytes, contentType);

        if (bytes.length == 0 || bodyString.trim().isEmpty()) {
            if (properties.isEmptyBodyIsSuccess()) {
                return new BufferedClientHttpResponse(response, bytes);
            }
        }

        if (!isInspectable(contentType)) {
            return new BufferedClientHttpResponse(response, bytes);
        }

        try {
            JsonNode root = objectMapper.readTree(bodyString);
            if (root == null || root.isMissingNode()) {
                return new BufferedClientHttpResponse(response, bytes);
            }
            String code = extractByDotPath(root, properties.getJsonPathCode());
            if (code == null) {
                return new BufferedClientHttpResponse(response, bytes);
            }
            if (!isSuccessCode(code, properties.getSuccessCodes())) {
                String msg = extractByDotPath(root, properties.getJsonPathMessage());
                throw new BusinessErrorException(status, code, msg, safeTruncate(bodyString));
            }
        } catch (BusinessErrorException e) {
            throw e; // rethrow
        } catch (Exception e) {
            // Parsing failed; be conservative and pass through
            if (log.isDebugEnabled()) {
                log.debug("Failed to parse response body for business error detection: {}", e.toString());
            }
        }

        return new BufferedClientHttpResponse(response, bytes);
    }

    private static boolean isSuccessCode(String code, Set<String> whitelist) {
        if (whitelist == null || whitelist.isEmpty()) return true; // no rule -> treat as success
        String norm = code == null ? "" : code.trim();
        String upper = norm.toUpperCase(Locale.ROOT);
        return whitelist.stream().map(s -> s.toUpperCase(Locale.ROOT)).anyMatch(upper::equals);
    }

    private static String extractByDotPath(JsonNode node, String path) {
        if (node == null || path == null || path.isBlank()) return null;
        String[] parts = path.split("\\.");
        JsonNode cur = node;
        for (String p : parts) {
            if (cur == null) return null;
            cur = cur.get(p);
        }
        if (cur == null || cur.isNull()) return null;
        if (cur.isValueNode()) return cur.asText();
        return cur.toString();
    }

    private static boolean isInspectable(MediaType contentType) {
        if (contentType == null) return false;
        String type = contentType.toString().toLowerCase(Locale.ROOT);
        return type.contains("json"); // covers application/json and +json
    }

    private static String bytesToString(byte[] bytes, MediaType mediaType) {
        Charset cs = StandardCharsets.UTF_8;
        if (mediaType != null && mediaType.getCharset() != null) {
            cs = mediaType.getCharset();
        }
        return new String(bytes, cs);
    }

    private static byte[] toByteArray(InputStream is) throws IOException {
        if (is == null) return new byte[0];
        try {
            byte[] buffer = is.readAllBytes();
            return buffer == null ? new byte[0] : buffer;
        } finally {
            try { is.close(); } catch (IOException ignored) {}
        }
    }

    private static String safeTruncate(String s) {
        if (s == null) return null;
        int max = 2048;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    /**
     * Simple buffering wrapper to allow downstream message converters to read the body again.
     */
    static class BufferedClientHttpResponse implements ClientHttpResponse {
        private final ClientHttpResponse delegate;
        private final byte[] body;

        BufferedClientHttpResponse(ClientHttpResponse delegate, byte[] body) {
            this.delegate = delegate;
            this.body = body == null ? new byte[0] : body;
        }

        @Override
        public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }


        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public void close() {
            delegate.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }
    }
}
