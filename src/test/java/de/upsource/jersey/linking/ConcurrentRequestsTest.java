package de.upsource.jersey.linking;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by meiko on 30.12.14.
 */
public class ConcurrentRequestsTest extends JerseyTest {

    private static Logger logger = LoggerFactory.getLogger(ConcurrentRequestsTest.class);

    @Override
    protected Application configure() {
        logger.info("configure application");
        return new TestResourceConfig();
    }

    @Produces("application/json")
    @Path("a")
    public static class TestResourceA {

        @GET
        public HashMap get(@QueryParam("str") String str) {
            logger.info("get a: {}", str);
            HashMap<String, String> result = new HashMap<>();
            result.put("str", str);
            return result;
        }
    }

    @Produces("application/json")
    @Path("/")
    public static class TestResourceB {

        @GET
        @Path("b")
        public HashMap get(@DefaultValue("BlaBla") @QueryParam("str") String str) {
            logger.info("get b: {}", str);
            HashMap<String, String> result = new HashMap<>();
            result.put("str", str);
            return result;
        }
    }

    @Produces("application/json")
    @Path("/")
    public static class TestResourceC {

        @GET
        @Path("c")
        public HashMap get(@QueryParam("str") String str) {
            logger.info("get c: {}", str);
            HashMap<String, String> result = new HashMap<>();
            result.put("str", str);
            return result;
        }

        @GET
        @Path("d/{p1}/{p2}")
        public HashMap get2(@PathParam("p1") String p1, @PathParam("p2") String p2, @QueryParam("str") String str) {
            logger.info("get d: {} {}", p1, p2);
            HashMap<String, String> result = new HashMap<>();
            result.put("p1", p1);
            result.put("p2", p2);
            result.put("str", str);
            return result;
        }
    }

    @Produces("application/json")
    @Path("/")
    public static class TestResourceE {

        @GET
        @Path("c2")
        public HashMap get(@DefaultValue("BlaBla") @QueryParam("str") String str) {
            logger.info("get b: {}", str);
            HashMap<String, String> result = new HashMap<>();
            result.put("str", str);
            return result;
        }
    }

    @Produces("application/json")
    @Path("/")
    public static class TestResourceRoot {

        @Inject
        Linker linker;

        @GET
        public HashMap get(@QueryParam("thread") Long thread) {
            logger.info("get overview");
            HashMap<String, String> result = new HashMap<>();
            result.put("0", linker.link(TestResourceA.class, obj -> obj.get(thread.toString())));
            result.put("1", linker.link(TestResourceB.class, obj -> obj.get(thread.toString())));
            result.put("2", linker.link(TestResourceC.class, obj -> obj.get(thread.toString())));
            result.put("3", linker.link(TestResourceE.class, obj -> obj.get(thread.toString())));
            result.put("4", linker.link(TestResourceC.class, obj -> obj.get2(thread.toString(), thread.toString(), thread.toString())));
            logger.info("result form thread{} : {}", Thread.currentThread().getId(), result);
            return result;
        }
    }

    @Test
    public void testThreads() throws ExecutionException, InterruptedException {
        test(1000, 5, () -> {
            long tid = Thread.currentThread().getId();
            return new Result(
                    "{\"0\":\"/api/a?str=" + tid + "\",\"1\":\"/api/b?str=" + tid +
                            "\",\"2\":\"/api/c?str=" + tid + "\",\"3\":\"/api/c2?str=" + tid + "\",\"4\":\"/api/d/" + tid + "/" + tid + "?str=" + tid + "\"}",
                    target("/").queryParam("thread", tid).request().get(String.class));
        });
    }

    private class Result {
        public String expected;
        public String result;

        public Result(String expected, String result) {
            this.expected = expected;
            this.result = result;
        }
    }


    private void test(final int threadCount, final int repeatCalls, Callable<Result> task) throws InterruptedException, ExecutionException {

        List<Callable<Result>> tasks = Collections.nCopies(threadCount * repeatCalls, task);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<Result>> futures = executorService.invokeAll(tasks);
        for (Future<Result> future : futures) {
            Result result = future.get();
            Assert.assertEquals(result.expected, result.result);
        }
    }
}
