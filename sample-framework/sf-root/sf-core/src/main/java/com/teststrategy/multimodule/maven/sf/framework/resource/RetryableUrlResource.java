package com.teststrategy.multimodule.maven.sf.framework.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;


@Slf4j
public class RetryableUrlResource extends UrlResource {

    private RetryTemplate retryTemplate;

    private static int _maxAttempts = 6;
    private static long _initialInterval = 1000;
    private static double _multiplier = 1.1;
    private static long _maxInterval = 2000;

    public static void setMaxAttempts(int maxAttempts) {
        _maxAttempts = maxAttempts;
    }

    public static void setInitialInterval(long initialInterval) {
        _initialInterval = initialInterval;
    }

    public static void setMultiplier(double multiplier) {
        _multiplier = multiplier;
    }

    public static void setMaxInterval(long maxInterval) {
        _maxInterval = maxInterval;
    }


    public RetryableUrlResource(URI uri) throws MalformedURLException {
        super(uri);
        init();
    }

    public RetryableUrlResource(URL url) {
        super(url);
        init();
    }

    public RetryableUrlResource(String path) throws MalformedURLException {
        super(path);
        init();
    }

    public RetryableUrlResource(String protocol, String location) throws MalformedURLException {
        super(protocol, location);
        init();
    }

    public RetryableUrlResource(String protocol, String location, String fragment) throws MalformedURLException {
        super(protocol, location, fragment);
        init();
    }

    private void init() {

        ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(_initialInterval);
        policy.setMultiplier(_multiplier);
        policy.setMaxInterval(_maxInterval);

        this.retryTemplate = RetryTemplate.builder()
                .retryOn(RetryIOException.class)
                .maxAttempts(_maxAttempts)
                .customBackoff(policy)
                .build();

    }

    @Override
    public InputStream getInputStream() throws IOException {
        URL url = getURL();
        if (url.getProtocol().startsWith("http")) {
            try {
                return retryTemplate.execute(context -> getHttpUrlInputStream(url));
            } catch (Exception exception) {
                log.error("Http URL Resource error", exception);
                throw exception;
            }
        } else {
            return super.getInputStream();
        }
    }

    private InputStream getHttpUrlInputStream(URL url) throws IOException {
        HttpURLConnection httpCon = null;
        try {
            URLConnection con = url.openConnection();
            ResourceUtils.useCachesIfNecessary(con);
            httpCon = (HttpURLConnection) con;

            int code = httpCon.getResponseCode();
            if (code >= 500) {
                httpCon.disconnect();
                log.error("Resource Not Found {}, Http Status={}", url, code);
                // retry
                throw new RetryIOException(String.format("Resource IOException %s, Http Status=%d", url, code));
            } else if (code >= 200 && code < 300) {
                return httpCon.getInputStream();
            } else {
                log.error("Resource Not Found {}, Http Status={}", url, code);
                // not retry
                throw new FileNotFoundException(String.format("Resource Not Found %s, Http Status=%s", url, code));
            }

        } catch (FileNotFoundException fileNotFoundException) {
            // no retry
            if (httpCon != null) httpCon.disconnect();
            throw fileNotFoundException;
        } catch (IOException ioException) {
            // retry
            log.error("Resource {} Read Error {}", url.toString(), ioException.getMessage());
            if (httpCon != null) httpCon.disconnect();
            throw new RetryIOException(ioException);
        } catch (Throwable throwable) {
            // else no retry
            log.error("Resource {} Read Error {}", url.toString(), throwable.getMessage());
            if (httpCon != null) httpCon.disconnect();
            throw throwable;
        }
    }

    public static class RetryIOException extends IOException {
        public RetryIOException(String message) {super(message);}
        public RetryIOException(Throwable cause) {super(cause);}
    }
}
