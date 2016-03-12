package controllers;

import com.codahale.metrics.annotation.Timed;
import config.Global;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.RandomProvider;
import services.WSRequestFactory;
import views.html.index;

import java.util.concurrent.TimeUnit;

/**
 * This is sample controller having examples how the Codehale monitoring can be used:
 * <ol>
 *     <li>metrics - method is jsoning the metrics and passing this to admin to use, potentially request to this endpoint should be blocked</li>
 *     <li>index - method is timed (it has randomm delays up to 5 seconds so it is really easy to observe timing metrics</li>
 *     <li>helloWorld - method for checking performance impact of Timed annotation</li>
 *     <li>client - method using WS instrumented client</li>
 *     <li>client - method using WS not instrumented client</li>
 *     <li>notInstrumented - method which is not instrumented by @Timed annotation (the request still will be calculated)</li>
 *     <li>instrumentedWithException - example method throwing exception - showing it will be calculated properly as well</li>
 * </ol>
 */
public class Application extends Controller {

    private static RandomProvider randomProvider;

    public static void setRandomProvider(RandomProvider randomProvider) {
        Application.randomProvider = randomProvider;
    }


    public static F.Promise<Result> metrics() {
        return F.Promise.<Result>pure(Results.ok(Json.toJson(Global.metrics)));
    }

    @Timed
    public static F.Promise<Result> index() {
        return F.Promise.delayed(new F.Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                return ok(index.render("Timed."));
            }
        }, randomProvider.provideNextRandom(5000), TimeUnit.MILLISECONDS);
    }

    @Timed
    public static F.Promise<Result> helloWorld() {
        return F.Promise.<Result>pure(ok("Hello world!"));
    }


    public static F.Promise<Result> client() {
        WSRequestHolder instrumentedWSRequestHolder = WSRequestFactory.getInstrumentedWSRequestHolder("http://www.google.co.uk", "google");

        return instrumentedWSRequestHolder.get().map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {
                return ok(wsResponse.getBody());
            }
        });
    }

    public static F.Promise<Result> clientNotInstrumented() {
        WSRequestHolder instrumentedWSRequestHolder = WSRequestFactory.getInstrumentedWSRequestHolder("http://www.google.co.uk", "google");

        return instrumentedWSRequestHolder.get().map(new F.Function<WSResponse, Result>() {
            @Override
            public Result apply(WSResponse wsResponse) throws Throwable {
                return ok(wsResponse.getBody());
            }
        });
    }

    public static F.Promise<Result> notInstrumented() {
        return F.Promise.<Result>pure(ok("Not timed!"));
    }

    @Timed
    public static F.Promise<Result> instrumentedWithException() {
        return F.Promise.throwing(new UnsupportedOperationException("Promise will throw exception"));
    }

}
