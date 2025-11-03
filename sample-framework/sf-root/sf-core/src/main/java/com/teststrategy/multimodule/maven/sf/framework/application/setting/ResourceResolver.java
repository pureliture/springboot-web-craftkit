package com.teststrategy.multimodule.maven.sf.framework.application.setting;

import java.io.InputStream;

class ResourceResolver {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResourceResolver.class);
    static InputStream openStream(String path) {
        try {
            // Try classpath:
            if (path.startsWith("classpath:")) {
                String cp = path.substring("classpath:".length());
                return ResourceResolver.class.getResourceAsStream(cp.startsWith("/") ? cp : "/" + cp);
            }
            // Try file system
            java.io.File f = new java.io.File(path);
            if (f.exists() && f.isFile()) {
                return new java.io.FileInputStream(f);
            }
            // Try as URL
            try {
                java.net.URL url = new java.net.URL(path);
                return url.openStream();
            } catch (Exception ignored) {}
        } catch (Exception e) {
            log.debug("Unable to open resource: {}", path, e);
        }
        return null;
    }
}
