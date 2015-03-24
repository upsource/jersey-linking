package de.upsource.jersey.linking;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * this map contains information of resources referenced by Methods, used by link creation
 */
class ResourceMethodRefRegistry {

    private ConcurrentHashMap<Integer, ResourceMethodRef> resources;

    public void setResourceMap(ConcurrentHashMap<Integer, ResourceMethodRef> r) {
        resources = r;
    }

    public ResourceMethodRef get(Method method) {
        return resources.get(method.hashCode());
    }

    public boolean containsKey(Method method) {
        return resources.containsKey(method.hashCode());
    }
}
