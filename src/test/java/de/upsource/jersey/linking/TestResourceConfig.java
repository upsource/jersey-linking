package de.upsource.jersey.linking;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by meiko on 30.12.14.
 */
public class TestResourceConfig extends ResourceConfig {

    private static Logger logger = LoggerFactory.getLogger(TestResourceConfig.class);

    public TestResourceConfig() {

        property(LinkerFeature.BASE_PATH, "/api");
        //property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, true);
        packages(true, getClass().getPackage().getName());
        register(LinkerFeature.class);
        register(JacksonFeature.class);
        logger.info("application configured");
    }

}
