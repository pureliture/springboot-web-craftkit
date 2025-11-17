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
import com.springboot.craftkit.framework.application.constant.CommonConstant;
import com.springboot.craftkit.framework.scope.RequestScopeUtil;
import com.springboot.craftkit.framework.scope.ScopeAttribute;
import com.springboot.craftkit.framework.scope.ScopeStore;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import org.slf4j.MDC;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RequestScopeHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    private final HystrixConcurrencyStrategy delegate;


    /**
     * Hystrix의 기본 동시성 전략을 확장하여 Request Scope를 유지할 수 있도록 하는 전략 클래스입니다.
     *
     * <p>이 클래스는 기존 {@link HystrixConcurrencyStrategy}를 대체하여 사용됩니다.
     * 기존 Hystrix의 동작을 유지하면서, Request Scope를 보존하는 기능을 추가합니다.</p>
     *
     * <h3>설계 개요:</h3>
     * <ul>
     *     <li>기존 Hystrix 동시성 전략을 {@code delegate} 변수에 저장하여 유지</li>
     *     <li>{@link HystrixPlugins#reset()} 호출을 통해 기존 전략을 초기화</li>
     *     <li>초기화 후, {@code this} 인스턴스를 새로운 동시성 전략으로 등록</li>
     *     <li>다른 Hystrix 플러그인(Hook, Notifier, Metrics 등)을 다시 등록하여 일관성 유지</li>
     * </ul>
     *
     * <h3>생성자 동작:</h3>
     * <ol>
     *     <li>Hystrix의 기존 동시성 전략을 가져와 {@code delegate}에 저장합니다.</li>
     *     <li>{@link HystrixPlugins#reset()}을 호출하여 기존 등록된 전략을 초기화합니다.</li>
     *     <li>이 클래스를 새로운 Hystrix 동시성 전략으로 등록합니다.</li>
     *     <li>기존의 {@link HystrixCommandExecutionHook}, {@link HystrixEventNotifier},
     *         {@link HystrixMetricsPublisher}, {@link HystrixPropertiesStrategy}를 다시 등록하여
     *         Hystrix 내부 상태가 일관성을 유지하도록 합니다.</li>
     * </ol>
     *
     * <h3>왜 {@code reset()}을 호출하는가?</h3>
     * <p>Hystrix는 {@code registerConcurrencyStrategy()}를 여러 번 호출해도 기존 전략을 덮어쓰지 않고
     * 중복하여 추가하는 방식으로 동작합니다. 따라서, 새로운 동시성 전략을 정상적으로 적용하기 위해서는
     * 반드시 {@link HystrixPlugins#reset()}을 호출하여 기존 설정을 초기화해야 합니다.</p>
     *
     * <h3>왜 {@code delegate}를 사용하는가?</h3>
     * <p>기존의 동시성 전략을 유지하면서 추가 기능을 구현하기 위해 {@code delegate}를 사용합니다.
     * 만약 {@code delegate}를 사용하지 않고 {@code super}만 호출한다면,
     * 기존 Hystrix 동시성 전략의 동작이 유지되지 않을 수 있습니다.</p>
     *
     * @throws IllegalStateException 기존 Hystrix 플러그인을 재등록하는 과정에서 문제가 발생할 경우 예외가 발생할 수 있습니다.
     */
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


    /**
     * Hystrix의 비동기 실행을 위한 {@link Callable} 객체를 감싸서 요청 스코프(Request Scope)를 유지하는 기능을 수행합니다.
     * <p>
     * Hystrix가 새로운 스레드에서 작업을 실행할 경우 기본적으로 {@link RequestScopeUtil}과 같은
     * 요청 스코프 관련 데이터가 전파되지 않습니다. 이를 해결하기 위해 이 메서드는 원본 Callable을
     * {@link RequestContextHolderCallable}로 감싸서 요청 스코프를 보존한 상태로 실행되도록 합니다.
     * </p>
     */
    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        RequestContextHolderCallable<T> delegatingCallable = new RequestContextHolderCallable<>(callable, (ScopeAttribute) RequestScopeUtil.getAttribute());
        return delegate != null ? delegate.wrapCallable(delegatingCallable) : super.wrapCallable(delegatingCallable);
    }

    /**
     * 요청 스코프(Request Scope)를 유지하기 위해 {@link Callable}을 감싸는 클래스입니다.
     * <p>
     * Hystrix의 비동기 작업 실행 시 별도의 스레드에서 실행되므로, 기본적으로 {@link RequestScopeUtil}과 같은
     * 스레드 로컬(ThreadLocal) 기반의 데이터가 새로운 스레드에서 유지되지 않습니다.
     * </p>
     * <p>
     * 이 클래스는 Hystrix가 새로운 스레드에서 실행할 때 요청 스코프 데이터(예: GTID)를 유지하도록 도와줍니다.
     * 요청 스코프는 Hystrix의 별도 스레드에서도 올바르게 전달되어야 하며, 실행이 완료된 후 정리됩니다.
     * </p>
     */
    public static class RequestContextHolderCallable<V> implements Callable<V> {
        private final Callable<V> delegate;      // 원본 Callable
        private final ScopeAttribute attribute;     // 요청 스코프에서 가져온 속성
        private final ContextSnapshot contextSnapshot;      // Micrometer의 ContextSnapshot (컨텍스트 전파용)

        public RequestContextHolderCallable(Callable<V> delegate, ScopeAttribute attribute) {
            this.delegate = delegate;
            this.attribute = attribute;
            this.contextSnapshot = ContextSnapshotFactory.builder().build().captureAll();
        }

        /**
         * 요청 스코프(Request Scope)를 유지한 상태로 원본 {@link Callable}을 실행합니다.
         * <p>
         * 실행 전에 요청 스코프 데이터를 현재 스레드에 설정하고, 실행이 완료되면 해당 데이터를 정리합니다.
         * </p>
         */
        @Override
        public V call() throws Exception {
            ScopeStore store = RequestScopeUtil.getScopeStore();
            if (attribute != null) {
                // MDC (Mapped Diagnostic Context)에 GTID 저장 (로그 추적을 위한 ID)
                String gtid = attribute.getGtid();
                MDC.put(CommonConstant.LOG_MDC_GTID, gtid);
                store.setAttributeIntoThreadLocal(attribute);
            }
            try {
                // Micrometer Context Snapshot을 활용하여 컨텍스트를 유지한 채로 Callable 실행
                return this.contextSnapshot.wrap(delegate).call();
            } finally {
                // 실행 완료 후 MDC 및 요청 스코프 정리
                MDC.clear();
                store.removeAttributeFromThreadLocal();
            }
        }
    }
}
