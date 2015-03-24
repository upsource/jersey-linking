package de.upsource.jersey.linking;

import org.glassfish.jersey.test.JerseyTest;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;

/**
 * Created by meiko on 01.01.15.
 */
public class PerformanceTest extends JerseyTest {


    private static Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    @Override
    protected Application configure() {
        logger.info("configure application");
        return new TestResourceConfig();
    }

    @Produces("application/json")
    @Path("/performace/a")
    public static class TestResourceA {

        @GET
        public JSONObject get(@QueryParam("str") String str) {
            logger.info("get a: {}", str);
            JSONObject obj = new JSONObject();
            obj.put("str", str);
            return obj;
        }

    }

    @Produces("application/json")
    @Path("/performace/b")
    public static class TestResourceB {

        @GET
        public JSONObject get(@DefaultValue("BlaBla") @QueryParam("str") String str) {
            logger.info("get b: {}", str);
            JSONObject obj = new JSONObject();
            obj.put("str", str);
            return obj;
        }

    }

    @Path("/performace/c")
    public static class TestResourceC {

        @GET
        public String get(@QueryParam("str") String str) {
            logger.info("get c: {}", str);
            JSONObject obj = new JSONObject();
            obj.put("str", str);
            return "";
        }

    }

    @Produces("application/json")
    @Path("/performace/")
    public static class TestResourceRoot {

        @Inject
        Linker linker;

        @GET
        public JSONObject get() {
            logger.info("get overview");
            JSONObject obj = new JSONObject();


            long startTime = System.currentTimeMillis();
            obj.put("a", linker.link(TestResourceA.class, (obj2) -> obj2.get("xa")));
            obj.put("b", linker.link(TestResourceB.class, (obj2) -> obj2.get("xb")));
            obj.put("c", linker.link(TestResourceC.class, (obj2) -> obj2.get("xc")));

            long estimatedTime = System.currentTimeMillis() - startTime;
            logger.info("first run, ms per run {}", estimatedTime / (1.0));


            startTime = System.currentTimeMillis();
            obj.put("a", linker.link(TestResourceA.class, (obj2) -> obj2.get("xa")));
            obj.put("b", linker.link(TestResourceB.class, (obj2) -> obj2.get("xb")));
            obj.put("c", linker.link(TestResourceC.class, (obj2) -> obj2.get("xc")));

            estimatedTime = System.currentTimeMillis() - startTime;
            logger.info("second run, ms per run {}", estimatedTime / (1.0));


            startTime = System.currentTimeMillis();
            int i = 0;
            for (; i < 400000; i++) {

                obj.put("a", linker.link(TestResourceA.class, obj2 -> obj2.get("xa")));
                obj.put("b", linker.link(TestResourceB.class, (obj2) -> obj2.get("xb")));
                obj.put("c", linker.link(TestResourceC.class, (obj2) -> obj2.get("xc")));
            }
            estimatedTime = System.currentTimeMillis() - startTime;
            logger.info("runs {} times, ms per run {}", i, estimatedTime / (i * 1.0));
            return obj;
        }
    }

    @Test
    public void test() {
        String response = target("/performace/").request().get(String.class);
        logger.info("result: {}", response);
    }
}
