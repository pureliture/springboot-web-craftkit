package sf.framework.hystrix;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import com.teststrategy.multimodule.maven.sf.framework.application.constant.CommonConstant;
import com.teststrategy.multimodule.maven.sf.framework.scope.RequestScopeUtil;
import com.teststrategy.multimodule.maven.sf.framework.scope.ScopeAttribute;
import com.teststrategy.multimodule.maven.sf.framework.scope.ScopeStore;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import org.slf4j.MDC;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RequestScopeHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private final HystrixConcurrencyStrategy delegate;

    public RequestScopeHystrixConcurrencyStrategy() {

        delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();
        HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins.getInstance().getCommandExecutionHook();
        HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
        HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
        HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance().getPropertiesStrategy();

        HystrixPlugins.reset();
        HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
        HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
        HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
        HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
        HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
    }

    @Override
    public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
        return delegate != null ? delegate.getBlockingQueue(maxQueueSize) : super.getBlockingQueue(maxQueueSize);
    }

    @Override
    public <T> HystrixRequestVariable<T> getRequestVariable(HystrixRequestVariableLifecycle<T> rv) {
        return delegate != null ? delegate.getRequestVariable(rv) : super.getRequestVariable(rv);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixProperty<Integer> corePoolSize, HystrixProperty<Integer> maximumPoolSize, HystrixProperty<Integer> keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return delegate != null ? delegate.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue) : super.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolProperties threadPoolProperties) {
        return delegate != null ? delegate.getThreadPool(threadPoolKey, threadPoolProperties) : super.getThreadPool(threadPoolKey, threadPoolProperties);
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        RequestContextHolderCallable<T> delegatingCallable = new RequestContextHolderCallable<>(callable, (ScopeAttribute) RequestScopeUtil.getAttribute());
        return delegate != null ? delegate.wrapCallable(delegatingCallable) : super.wrapCallable(delegatingCallable);
    }

    public static class RequestContextHolderCallable<V> implements Callable<V> {
        private final Callable<V> delegate;
        private final ScopeAttribute attribute;
        private final ContextSnapshot contextSnapshot;

        public RequestContextHolderCallable(Callable<V> delegate, ScopeAttribute attribute) {
            this.delegate = delegate;
            this.attribute = attribute;
            this.contextSnapshot = ContextSnapshotFactory.builder().build().captureAll();
        }

        @Override
        public V call() throws Exception {
            ScopeStore store = RequestScopeUtil.getScopeStore();
            if (attribute != null) {
                String gtid = attribute.getGtid();
                MDC.put(CommonConstant.LOG_MDC_GTID, gtid);
                store.setAttributeIntoThreadLocal(attribute);
            }
            //log.debug("attribute : {}", RequestScopeUtil.getAttribute());
            try {
                return this.contextSnapshot.wrap(delegate).call();
            } finally {
                MDC.clear();
                store.removeAttributeFromThreadLocal();
            }
        }
    }
}
