package config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import services.RandomProviderImpl;
import services.RequestGenericHandler;
import services.RequestGenericHandlerFactory;

import java.lang.reflect.Method;

/**
 * This expands Playframework default GlobalSettings to provide instrumentation for Memory, Garbage collector, number of active and all requests.
 * Additionaly it gives capability of annotating endpoints with @Timed so the stat for this endpoint will be collected.
 * There is /admin/metrics endpoint which dumps all the metrics to Json.
 */
public class Global extends GlobalSettings {

    public static MetricRegistry metrics;

    @Override
    public void onStart(Application app) {
        metrics = new MetricRegistry();
        // Line below creates console reporting which is not really useful apart from dev.
        // Additional Ganglia / Graphite reporters can be configured and added here.
//        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
//        reporter.start(45, TimeUnit.SECONDS);

        metrics.registerAll(new MemoryUsageGaugeSet());
        metrics.registerAll(new GarbageCollectorMetricSet());

        controllers.Application.setRandomProvider(new RandomProviderImpl());

        super.onStart(app);
    }

    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {

        final RequestGenericHandler handlerForEndpoint = RequestGenericHandlerFactory.getHandlerForEndpoint(Global.metrics.counter("activeRequests"), metrics.meter("allRequests"), actionMethod);

        return new Action.Simple() {

            @Override
            public F.Promise<Result> call(Http.Context ctx) throws Throwable {
                return handlerForEndpoint.handle(delegate, ctx);
            }
        };

    }
}
