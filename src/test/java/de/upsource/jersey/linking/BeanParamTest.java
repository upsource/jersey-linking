package de.upsource.jersey.linking;

import org.glassfish.jersey.test.JerseyTest;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import java.util.HashMap;

/**
 * Created by meiko on 02.01.15.
 */
public class BeanParamTest extends JerseyTest {

    private static Logger logger = LoggerFactory.getLogger(BeanParamTest.class);

    @Override
    protected Application configure() {
        logger.info("configure application");
        return new TestResourceConfig();
    }

    public static class Bean {

        @QueryParam("num")
        private int num;

        @PathParam("nump")
        private int nump;


        @QueryParam("obj")
        private JSONObject obj;

        public Bean() {
        }

        public Bean(int num, JSONObject obj) {
            this.num = num;
            this.obj = obj;
        }

        public Bean(int num, int nump, JSONObject obj) {
            this(num, obj);
            this.nump = nump;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getNump() {
            return nump;
        }

        public void setNump(int nump) {
            this.nump = nump;
        }

        public JSONObject getObj() {
            return obj;
        }

        public void setObj(JSONObject obj) {
            this.obj = obj;
        }
    }

    public static class Bean2 {

        @QueryParam("num")
        private int num;

        @PathParam("nump")
        private int nump;

        private JSONObject obj;

        public Bean2() {
        }

        public Bean2(int num, JSONObject obj) {
            this.num = num;
            this.obj = obj;
        }

        public Bean2(int num, int nump, JSONObject obj) {
            this(num, obj);
            this.nump = nump;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getNump() {
            return nump;
        }

        public void setNump(int nump) {
            this.nump = nump;
        }

        @QueryParam("obj")
        public JSONObject getObj() {
            return obj;
        }

        public void setObj(JSONObject obj) {
            this.obj = obj;
        }
    }

    @Produces("application/json")
    @Path("bean")
    public static class TestResourceA {

        @GET
        @Path("{nump}")
        public Bean get(@BeanParam Bean bean) {
            logger.info("get a: {}", bean);
            return bean;
        }
    }

    @Produces("application/json")
    @Path("bean2")
    public static class TestResourceB {

        @GET
        @Path("{nump}")
        public Bean2 get(@BeanParam Bean2 bean) {
            logger.info("get a: {}", bean);
            return bean;
        }
    }

    @Produces("application/json")
    @Path("bean")
    public static class TestResource {

        @Inject
        Linker linker;

        @GET
        @Path("1")
        public HashMap get() {
            HashMap<String, String> result = new HashMap<>();
            JSONObject json = new JSONObject();
            json.put("a", 11);
            json.put("b", 22);
            result.put("0", linker.link(TestResourceA.class, obj -> obj.get(new Bean(42, 12, json))));
            return result;
        }

        @GET
        @Path("2")
        public HashMap get2() {
            HashMap<String, String> result = new HashMap<>();
            JSONObject json = new JSONObject();
            json.put("a", 11);
            json.put("b", 22);
            result.put("0", linker.link(TestResourceB.class, obj -> obj.get(new Bean2(42, 12, json))));
            return result;
        }
    }

    @Test
    public void test() {
        //String response = target("/bean/11/").queryParam("num", 1).queryParam("obj", new Bean(42, new JSONObject())).request().get(String.class);
        String response = target("/bean/1").request().get(String.class);
        Assert.assertEquals("{\"0\":\"/api/bean/12?num=42&obj=%7B%22a%22%3A11%2C%22b%22%3A22%7D\"}", response);
        logger.info("result: {}", response);
    }

    @Test
    public void test2() {
        //String response = target("/bean/11/").queryParam("num", 1).queryParam("obj", new Bean(42, new JSONObject())).request().get(String.class);
        String response = target("/bean/2").request().get(String.class);
        Assert.assertEquals("{\"0\":\"/api/bean2/12?num=42&obj=%7B%22a%22%3A11%2C%22b%22%3A22%7D\"}", response);
        logger.info("result: {}", response);
    }
}
