package services;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import play.libs.F;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * WSRequestFactory - factory method which will produce instrumented Play WSRequestHandler.
 * The instrumentation is performed by JavaProxy mechanism. The call to underlying execute is proxied and adds timing to
 * metricsRegistry, which has to be passed before using this Factory.
 *
 * The used metrics will have following name: "clientRequests.{givenName}".
 */
public class WSRequestFactory {
    private static MetricRegistry metrics;

    public static void setMetrics(MetricRegistry metris) {
        WSRequestFactory.metrics = Objects.requireNonNull(metris, "Metrics object is null.");
    }

    public static play.libs.ws.WSRequestHolder getInstrumentedWSRequestHolder(String url, String name) {
        Objects.requireNonNull(url, "Url has to be specified.");
        Objects.requireNonNull(name, "Name has to be specified.");
        play.libs.ws.WSRequestHolder requestHolder = WS.url(url);

        return (WSRequestHolder) Proxy.newProxyInstance(requestHolder.getClass().getClassLoader(),
                requestHolder.getClass().getInterfaces(),
                new InstrumentationProxy(requestHolder, name));
    }

    private static class InstrumentationProxy implements InvocationHandler {

        private final play.libs.ws.WSRequestHolder wsRequestHolder;
        private final String name;


        public InstrumentationProxy(WSRequestHolder requestHolder, String name) {
            this.wsRequestHolder = requestHolder;
            this.name = name;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


            if (method.equals(WSRequestHolder.class.getMethod("execute"))) {
                return method.invoke(wsRequestHolder, args);
            }
            else {
                final Timer.Context context = metrics.timer("clientRequests." + name).time();

                try {
                    @SuppressWarnings("unchecked")
                    F.Promise<WSResponse> promiseWsResponse = (F.Promise<WSResponse>) method.invoke(wsRequestHolder, args);


                    promiseWsResponse.onFailure(new F.Callback<Throwable>() {
                        @Override
                        public void invoke(Throwable throwable) throws Throwable {
                            context.stop();
                        }
                    });

                    promiseWsResponse.onRedeem(new F.Callback<WSResponse>() {
                        @Override
                        public void invoke(WSResponse wsResponse) throws Throwable {
                            context.stop();
                        }
                    });

                    return promiseWsResponse;
                }
                catch (Throwable t) {
                    context.stop();
                    throw t;
                }
            }
        }
    }
}
