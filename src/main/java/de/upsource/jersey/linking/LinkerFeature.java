package de.upsource.jersey.linking;

import org.glassfish.hk2.api.PerThread;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * the Linker feature
 * TODO: implement MatrixParam in Template
 * TODO: test and fix full template
 * TODO: write unit tests
 * TODO: check nested BeanParams is this necessary
 * TODO: implement beanparam constructor injected params
 */
public class LinkerFeature implements Feature {

    /**
     * configuration key for the base path of created links and templates
     */
    public static final String BASE_PATH = "server.basePath";

    /**
     * base path of created links and templates
     */
    static String basePath = "/";

    @Override
    public boolean configure(final FeatureContext context) {
        String base = (String) context.getConfiguration().getProperty(BASE_PATH);
        if (base != null) {
            basePath = base;
        }

        context.register(ResourceModelProcessor.class);
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ObjenesisStd.class).to(Objenesis.class).in(Singleton.class);
                bindAsContract(ResourceMethodRefRegistry.class).in(Singleton.class);
                bindAsContract(Linker.class).in(PerThread.class);
            }
        });
        return true;
    }
}
