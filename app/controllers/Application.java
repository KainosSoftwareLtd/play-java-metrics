package controllers;

import com.codahale.metrics.annotation.Timed;
import config.Global;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.RandomProvider;
import views.html.index;

import java.util.concurrent.TimeUnit;

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
                return ok(index.render("Your new application is ready."));
            }
        }, randomProvider.provideNextRandom(5000), TimeUnit.MILLISECONDS);
    }

    public static Result notInstrumented() {
        return ok("IT WORKS!");
    }

    @Timed
    public static F.Promise<Result> instrumentedWithException() {
        return F.Promise.throwing(new UnsupportedOperationException("Promise will throw exception"));
    }
}
