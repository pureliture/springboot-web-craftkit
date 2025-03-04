package sf.framework.hystrix

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext
import com.teststrategy.multimodule.maven.sf.framework.scope.RequestScopeUtil
import com.teststrategy.multimodule.maven.sf.framework.scope.ScopeAttribute
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.concurrent.Callable
import java.util.concurrent.Executors


class RequestScopeHystrixConcurrencyStrategyTest: BehaviorSpec({

    Given("ScopeAttribute가 설정되어 있을 때") {

        // HystrixRequestContext 초기화
        if (!HystrixRequestContext.isCurrentThreadInitialized()) {
            HystrixRequestContext.initializeContext()
        }
        val strategy = RequestScopeHystrixConcurrencyStrategy()

        // 기존 ScopeAttribute 설정
        val originalAttribute = ScopeAttribute().apply {
            userId = "testUser"
            gtid = "testGTID-1234"
        }

        RequestScopeUtil.getScopeStore().setAttributeIntoThreadLocal(originalAttribute)

        When("RequestScopeHystrixConcurrencyStrategy의 wrapCallable을 사용하여 새로운 Hystrix 스레드에서 실행하면") {

            val callable = Callable { RequestScopeUtil.getAttribute() }
            val wrappedCallable = strategy.wrapCallable(callable)!!

            val executor = Executors.newSingleThreadExecutor()
            val future = executor.submit(wrappedCallable)
            val resultAttribute = future.get()
            executor.shutdown()

            Then("새로운 Hystrix 스레드에서도 ScopeAttribute가 유지되어야 한다") {
                resultAttribute.shouldNotBeNull()
                resultAttribute.gtid shouldBe originalAttribute.gtid
                resultAttribute.userId shouldBe originalAttribute.userId
            }
        }
    }
})