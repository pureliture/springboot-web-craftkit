package com.springboot.craftkit.framework.request;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

/**
 * HTTP response wrapper that captures the response output.
 */
public class HttpResponseWrapper extends HttpServletResponseWrapper {
    private final CharArrayWriter output;

    public String toString() {
        return output.toString();
    }

    public HttpResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new CharArrayWriter();
    }

    public PrintWriter getWriter() {
        return new PrintWriter(output);
    }
}
