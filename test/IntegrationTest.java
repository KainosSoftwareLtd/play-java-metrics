import com.codahale.metrics.Timer;
import config.Global;
import controllers.Application;
import controllers.routes;
import org.junit.Assert;
import org.junit.Test;
import play.test.FakeApplication;
import play.test.FakeRequest;
import play.test.Helpers;
import services.RandomProvider;

import java.util.concurrent.TimeUnit;

public class IntegrationTest {

    @Test
    public void testIfEndpointIsProperlyInstumented() {
        FakeApplication fakeApplication = Helpers.fakeApplication(new Global());

        Helpers.running(fakeApplication, new Runnable() {
            @Override
            public void run() {

                Application.setRandomProvider(new RandomProvider() {
                    @Override
                    public int provideNextRandom(int bound) {
                        return 5;
                    }
                });

                // Initial request take some time as the server is starting and spinning up
                Helpers.callAction(
                        controllers.routes.ref.Application.index(),
                        new FakeRequest("GET", "/")
                );


                Helpers.callAction(
                    controllers.routes.ref.Application.index(),
                    new FakeRequest("GET", "/")
                );

                Application.setRandomProvider(new RandomProvider() {
                    @Override
                    public int provideNextRandom(int bound) {
                        return 50;
                    }
                });

                Helpers.callAction(
                    controllers.routes.ref.Application.index(),
                    new FakeRequest("GET", "/")
                );

                Timer timer = Global.metrics.getTimers().get("controllers.Application.index");

                long[] values = timer.getSnapshot().getValues();

                // The values list is sorted in ascending order and in nanos
                long shortMs = TimeUnit.MILLISECONDS.convert(values[0], TimeUnit.NANOSECONDS);
                long longMs = TimeUnit.MILLISECONDS.convert(values[1], TimeUnit.NANOSECONDS);

                // 3 Requests as one is during the setup
                Assert.assertEquals("There should be 3 requests total to the endpoint.", 3, timer.getCount());

                Assert.assertTrue("One request is short and should finish in less than 10ms. But it finished in: " + shortMs, shortMs < 10);
                Assert.assertTrue("One request is long and should finish in more than 30ms. It finished in: " + longMs, longMs > 30);

            }
        });
    }

    @Test (expected = java.lang.UnsupportedOperationException.class)
    public void testIfTimerIsAppliedOnExceptionWhenCallToControllerFailsImmediately() {
        FakeApplication fakeApplication = Helpers.fakeApplication(new Global());

        Helpers.running(fakeApplication, new Runnable() {

            @Override
            public void run() {
                Application.setRandomProvider(new RandomProvider() {
                    @Override
                    public int provideNextRandom (int bound) {
                        throw new UnsupportedOperationException("Breaking call to make sure instrumentation happened properly");
                    }
                });

                try {
                    Helpers.callAction(
                        routes.ref.Application.index(),
                        new FakeRequest("GET", "/")
                    );
                } finally {
                    Timer timer = Global.metrics.getTimers().get("controllers.Application.index");

                    Assert.assertEquals("There should be 1 request1 total to the endpoint.", 1, timer.getCount());
                }
            }
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIfTimerIsAppliedOnExceptionWhenPromiseFails() {
        FakeApplication fakeApplication = Helpers.fakeApplication(new Global());

        Helpers.running(fakeApplication, new Runnable() {

            @Override
            public void run() {

                try {
                    Helpers.callAction(
                        routes.ref.Application.instrumentedWithException(),
                        new FakeRequest("GET", "/instrumentedWithException")
                    );
                } finally {
                    Timer timer = Global.metrics.getTimers().get("controllers.Application.instrumentedWithException");

                    Assert.assertEquals("There should be 1 request1 total to the endpoint.", 1, timer.getCount());
                }

            }
        });
    }


    @Test
    public void testIfMemoryIsProperlyInstrumented() {
        FakeApplication fakeApplication = Helpers.fakeApplication(new Global());

        Helpers.running(fakeApplication, new Runnable() {
            @Override
            public void run() {
                long maxHeap = (long) Global.metrics.getGauges().get("heap.max").getValue();
                long initHeap = (long) Global.metrics.getGauges().get("heap.init").getValue();

                Assert.assertTrue("memory allocated should be created than 0", initHeap > 0);
                Assert.assertTrue("max memory allocated should be created than 0", maxHeap > 0);
                Assert.assertTrue("max memory should be greater or equal to init", initHeap <= maxHeap);
            }
        });
    }

    @Test
    public void testIfNotInstrumentedEndpointIsNotInstrumented() {
        FakeApplication fakeApplication = Helpers.fakeApplication(new Global());

        Helpers.running(fakeApplication, new Runnable() {
            @Override
            public void run() {
                Helpers.callAction(
                    routes.ref.Application.notInstrumented(),
                    new FakeRequest("GET", "/notInstrumented")
                );

                Timer timer = Global.metrics.getTimers().get("controllers.Application.notInstrumented");

                Assert.assertNull("There should be no timer for not instrumented endpoint", timer);
            }
        });
    }


}
