package sf.framework.hystrix.shutdown;

import com.netflix.hystrix.Hystrix;
import com.springboot.craftkit.framework.application.shutdown.ShutdownHelper;
import rx.schedulers.Schedulers;

public class HystrixDownHelper implements ShutdownHelper {

    @Override
    public void cleanup() {
        Hystrix.reset();
        Schedulers.shutdown();
    }
}
