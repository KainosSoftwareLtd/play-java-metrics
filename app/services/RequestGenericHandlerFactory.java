package services;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;
import config.Global;
import play.mvc.Result;

import java.lang.reflect.Method;

/**
 * This factory produces one of two handlers depending if the endpoint should be instrumented or not.
 * <ol>
 * <li>ActiveAndAllRequestHandler - this handler will calculate all requests and active requests in the global metrics</li>
 * <li>TimeRequestHandler - this handler will calculate the above and apart from that will calculate processing times for the endpoint</li>
 * </ol>
 */
public class RequestGenericHandlerFactory {

    public static RequestGenericHandler getHandlerForEndpoint(final Counter activeCounters, final Meter allRequests, final Method actionMethod) {

        if (actionMethod.isAnnotationPresent(Timed.class)) {
            return new TimeRequestHandler(actionMethod, activeCounters, allRequests);
        }
        else {
            return new ActiveAndAllRequestHandler(activeCounters, allRequests);
        }
    }

    private static class ActiveAndAllRequestHandler extends RequestGenericHandler {
        private final Counter activeCounters;
        private final Meter allRequests;

        public ActiveAndAllRequestHandler(Counter activeCounters, Meter allRequests) {
            this.activeCounters = activeCounters;
            this.allRequests = allRequests;
        }

        @Override
        protected void onStart() {
            activeCounters.inc();
            allRequests.mark();
        }

        @Override
        protected void handleEndpointCallingErrors(Throwable t) {
            activeCounters.dec();
        }

        @Override
        protected void handlePromiseErrors(Throwable throwable) {
            activeCounters.dec();
        }

        @Override
        protected void onFinish(Result result) {
            activeCounters.dec();
        }
    }

    private static class TimeRequestHandler extends ActiveAndAllRequestHandler {
        private final Method actionMethod;
        private Timer.Context context;

        public TimeRequestHandler(Method actionMethod, Counter activeCounters, Meter allRequests) {
            super(activeCounters, allRequests);
            this.actionMethod = actionMethod;
        }

        @Override
        protected void onStart() {
            super.onStart();
            Timer timer = Global.metrics.timer(MetricRegistry.name(actionMethod.getDeclaringClass(), actionMethod.getName()));
            context = timer.time();
        }

        @Override
        protected void handleEndpointCallingErrors(Throwable throwable) {
            super.handleEndpointCallingErrors(throwable);
            context.stop();
        }

        @Override
        protected void handlePromiseErrors(Throwable throwable) {
            super.handlePromiseErrors(throwable);
            context.stop();
        }

        @Override
        protected void onFinish(Result result) {
            super.onFinish(result);
            context.stop();
        }
    }

}
