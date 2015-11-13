package services;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * This abstract class is responsible for delegating request to another play action with given context and providing some instrumentation architecture.
 * Subclasses should override:
 * <ol>
 * <li>onStart - which happens immediately when the requests is being processed (default context thread, before promise is created)</li>
 * <li>handlePromiseErrors - this will be called whenever there is error when resolving the promise code</li>
 * <li>handleEndpointCallingErrors - this will be called whenever there is error when then endpoint is called (the promise was not created for some reason or the logic is not in promise</li>
 * <li>onFinish - called when promise is successfully redeemed and request processing has been finished</li>
 *</ol>
 */
public abstract class RequestGenericHandler {

    protected abstract void onStart();

    public F.Promise<Result> handle(Action<?> delegate, Http.Context context) throws Throwable {
        onStart();

        try {
            F.Promise<Result> call = delegate.call(context);

            call.onFailure(new F.Callback<Throwable>() {
                @Override
                public void invoke(Throwable throwable) throws Throwable {
                    handlePromiseErrors(throwable);
                }
            });

            call.onRedeem(new F.Callback<Result>() {
                @Override
                public void invoke(Result result) throws Throwable {
                    onFinish(result);
                }
            });

            return call;
        } catch (Throwable t) {
            handleEndpointCallingErrors(t);
            throw t;
        }
    }

    protected abstract void handleEndpointCallingErrors(Throwable t);

    protected abstract void handlePromiseErrors(Throwable throwable);

    protected abstract void onFinish(Result result);

}
