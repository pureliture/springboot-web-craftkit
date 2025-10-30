package com.teststrategy.multimodule.maven.sf.framework.rest.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClientException;

/**
 * Thrown when a 2xx HTTP response body contains a business-level error according to
 * configured rules. Carries the HTTP status, parsed business code/message, and the raw body.
 */
public class BusinessErrorException extends RestClientException {

    private final HttpStatusCode statusCode;
    private final String businessCode;
    private final String businessMessage;
    private final String responseBody;

    public BusinessErrorException(HttpStatusCode statusCode, String businessCode, String businessMessage, String responseBody) {
        super(buildMessage(statusCode, businessCode, businessMessage));
        this.statusCode = statusCode;
        this.businessCode = businessCode;
        this.businessMessage = businessMessage;
        this.responseBody = responseBody;
    }

    private static String buildMessage(HttpStatusCode status, String code, String message) {
        StringBuilder sb = new StringBuilder("Business error detected in HTTP response");
        sb.append(" status=").append(status);
        if (code != null) sb.append(" code=").append(code);
        if (message != null) sb.append(" message=").append(message);
        return sb.toString();
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public String getBusinessMessage() {
        return businessMessage;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
