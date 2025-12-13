package com.springboot.craftkit.framework.listener;

import com.springboot.craftkit.framework.application.ApplicationContextUtil;
import com.springboot.craftkit.framework.application.shutdown.ShutdownHelper;
import com.springboot.craftkit.framework.application.shutdown.ShutdownSupport;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application context listener for handling application lifecycle events and shutdown.
 */
@WebListener
@Slf4j
public class ApplicationContextListener implements ServletContextListener, ShutdownSupport {

    final Set<ShutdownHelper> shutdownHelpers = ConcurrentHashMap.newKeySet();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Application initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Application Custom Shutdown processing");
        cleanupThreads();
    }

    @Override
    public void addShutdownHelper(ShutdownHelper helper) {
        shutdownHelpers.add(helper);
    }

    /**
     * Cleanup thread locals.
     */
    private void cleanupThreads() {
        try {
            helpShutdown();
            ((ConfigurableApplicationContext) ApplicationContextUtil.getApplicationContext()).close();
        } catch (Exception e) {
            log.warn("{}", e.getMessage());
        }
    }

    private void helpShutdown() {
        try {
            shutdownHelpers.forEach(ShutdownHelper::cleanup);
        } catch (Exception e) {
            log.error("ShutdownHelper cleanup call error {}", e.getMessage());
        }
    }

}
