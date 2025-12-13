package com.springboot.craftkit.framework.request;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;
import org.springframework.lang.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * HTTP request wrapper that caches the request body for re-reading.
 */
public class HttpRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] bodyData;

    public HttpRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        InputStream is = super.getInputStream();
        bodyData = IOUtils.toByteArray(is);
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bis = new ByteArrayInputStream(bodyData);
        return new ServletImpl(bis);
    }

}

class ServletImpl extends ServletInputStream {
    private final InputStream is;

    public ServletImpl(InputStream bis) {
        is = bis;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int read(@NonNull byte[] b) throws IOException {
        return is.read(b);
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setReadListener(ReadListener arg0) {
    }
}
