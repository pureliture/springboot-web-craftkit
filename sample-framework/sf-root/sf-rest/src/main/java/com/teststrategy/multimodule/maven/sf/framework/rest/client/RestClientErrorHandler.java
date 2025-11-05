package com.teststrategy.multimodule.maven.sf.framework.rest.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * Neutral error handler for RestTemplate.
 *
 * Delegates to Spring's DefaultResponseErrorHandler to raise exceptions on non-2xx
 * and adds minimal debug logging without leaking sensitive data.
 */
public class RestClientErrorHandler extends DefaultResponseErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(RestClientErrorHandler.class);

    @Override
    public void handleError(ClientHttpResponse response, HttpStatusCode statusCode) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("RestTemplate error: status={} ({})", statusCode.value(), statusCode);
        }
        super.handleError(response, statusCode);
    }
}
