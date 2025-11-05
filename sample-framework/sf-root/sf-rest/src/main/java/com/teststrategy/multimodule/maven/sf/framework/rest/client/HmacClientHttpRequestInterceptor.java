package com.teststrategy.multimodule.maven.sf.framework.rest.client;

import com.teststrategy.multimodule.maven.sf.framework.rest.setting.HmacAuthProperties;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Adds a simple HMAC signature header to outbound requests.
 *
 * Scheme (neutral, example):
 *   signingString = METHOD + "\n" + pathWithQuery + "\n" + sha256Hex(body) + "\n" + epochSeconds
 *   signature = Base64(HmacSHA256(signingString, secret))
 *   headerValue = "HMAC " + keyId + ":" + signature + ":" + epochSeconds
 */
public class HmacClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final HmacAuthProperties properties;

    public HmacClientHttpRequestInterceptor(HmacAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (properties.isEnabled() && hasText(properties.getKeyId()) && hasText(properties.getSecret())) {
            String method = request.getMethod() != null ? request.getMethod().name() : "GET";
            URI uri = request.getURI();
            String pathWithQuery = uri.getRawPath() + (uri.getRawQuery() != null ? ("?" + uri.getRawQuery()) : "");
            String bodySha256 = sha256Hex(body);
            String epoch = String.valueOf(Instant.now().getEpochSecond());
            String signingString = method + "\n" + pathWithQuery + "\n" + bodySha256 + "\n" + epoch;
            String signature = hmacSha256Base64(signingString, properties.getSecret());
            String headerValue = "HMAC " + properties.getKeyId() + ":" + signature + ":" + epoch;
            String headerName = properties.getHeaderName();
            if (!request.getHeaders().containsKey(headerName)) {
                request.getHeaders().add(headerName, headerValue);
            }
        }
        return execution.execute(request, body);
    }

    private String sha256Hex(byte[] data) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data == null ? new byte[0] : data);
            return Hex.encodeHexString(hash);
        } catch (Exception e) {
            throw new IOException("Failed to compute SHA-256", e);
        }
    }

    private String hmacSha256Base64(String message, String secret) throws IOException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new IOException("Failed to compute HMAC-SHA256", e);
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
