package de.upsource.jersey.linking;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.glassfish.hk2.api.PerThread;
import org.objenesis.Objenesis;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.function.Function;

/**
 * this class is NOT thread safe, need instantiation per thread for performance reasons
 */
@PerThread
public class Linker {

    @Inject
    private ResourceMethodRefRegistry registry;

    @Inject
    private Objenesis objenesis;

    private HashMap<Class, Proxy> proxies;

    public Linker() {
        this.proxies = new HashMap<>();
    }

    public <R, O> ResourceResultDelegate proxy(final Class<R> clazz, Function<R, O> producer) {

        ResourceResultDelegate[] delegate = new ResourceResultDelegate[1];
        Proxy proxy;
        if (proxies.containsKey(clazz)) {
            proxy = proxies.get(clazz);
        } else {

            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setUseCache(false);
            proxyFactory.setSuperclass(clazz);
            proxyFactory.setFilter((Method m) ->
                    m.getModifiers() == Modifier.PUBLIC && !m.getDeclaringClass().equals(Object.class));
            proxy = (Proxy) objenesis.newInstance(proxyFactory.createClass());
            proxies.put(clazz, proxy);
        }


        proxy.setHandler((self, thisMethod, proceed, args) -> {
            ResourceMethodRef resourceMethodRef = registry.get(thisMethod);
            delegate[0] = new ResourceResultDelegate(resourceMethodRef, args);
            return null;
        });
        producer.apply((R) proxy);

        return delegate[0];
    }

    public <R, O> Link linkFull(final Class<R> clazz, Function<R, O> producer) {

        ResourceResultDelegate delegate = proxy(clazz, producer);
        return new Link(delegate.getHttpMethod(), delegate.getLink());
    }

    public <R, O> String link(final Class<R> clazz, Function<R, O> producer) {

        return proxy(clazz, producer).getLink();
    }

    public <R, O> String template(final Class<R> clazz, Function<R, O> producer) {

        return proxy(clazz, producer).getTemplate();
    }

    public <R, O> String fullTemplate(final Class<R> clazz, Function<R, O> producer) {

        return proxy(clazz, producer).getFullTemplate();
    }
}
