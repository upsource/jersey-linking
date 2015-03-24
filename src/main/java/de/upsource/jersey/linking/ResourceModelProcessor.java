package de.upsource.jersey.linking;

import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.server.model.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * on boot the resource model is analyzed and a map is created (ResourceMap),
 * which is used to find information on Parameters on link creation
 */
@Provider
class ResourceModelProcessor implements ModelProcessor {

    private final static Map<Class<?>, Class<?>> primitiveToClassMap = new HashMap<>();

    static {
        primitiveToClassMap.put(boolean.class, Boolean.class);
        primitiveToClassMap.put(byte.class, Byte.class);
        primitiveToClassMap.put(short.class, Short.class);
        primitiveToClassMap.put(char.class, Character.class);
        primitiveToClassMap.put(int.class, Integer.class);
        primitiveToClassMap.put(long.class, Long.class);
        primitiveToClassMap.put(float.class, Float.class);
        primitiveToClassMap.put(double.class, Double.class);
    }

    @Inject
    private ParamConverterProvider converterProvider;

    @Inject
    private ResourceMethodRefRegistry registry;

    @Override
    public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
        List<Resource> resources = resourceModel.getResources();
        ConcurrentHashMap<Integer, ResourceMethodRef> resourceMap = new ConcurrentHashMap<>();
        for (Resource resource : resources) {
            try {
                handleResource(resource, resourceMap);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        registry.setResourceMap(resourceMap);
        return resourceModel;
    }

    @Override
    public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        // no modification
        return subResourceModel;
    }

    private void handleResource(Resource resource, Map<Integer, ResourceMethodRef> resourceMap) throws InvocationTargetException, IllegalAccessException {
        List<ResourceMethod> resourceMethods = resource.getAllMethods();
        for (ResourceMethod resourceMethod : resourceMethods) {
            Invocable invocable = resourceMethod.getInvocable();
            Method definitionMethod = invocable.getDefinitionMethod();
            resourceMap.put(definitionMethod.hashCode(), handleResourceMethod(resourceMethod));
        }
        List<Resource> childResourceMethods = resource.getChildResources();
        for (Resource childResource : childResourceMethods) {
            handleResource(childResource, resourceMap);
        }
    }

    private ResourceMethodRef handleResourceMethod(ResourceMethod resourceMethod) throws InvocationTargetException, IllegalAccessException {

        Invocable invocable = resourceMethod.getInvocable();
        UriBuilder templateBuilder = UriBuilder.fromPath(LinkerFeature.basePath);
        Resource resource = resourceMethod.getParent();

        if (resource.getParent() != null) {
            templateBuilder.path(resource.getParent().getPath());
        }
        templateBuilder.path(resource.getPath());

        List<Parameter> parametersDefinition = invocable.getParameters();
        StringBuilder queryParams = new StringBuilder();
        Map<Integer, ParamRef> paramRefs = new HashMap<>();
        for (int i = 0; i < parametersDefinition.size(); i++) {
            Parameter param = parametersDefinition.get(i);
            handleParam(param, paramRefs, queryParams, i);
        }

        StringBuilder queryParamTemplate = new StringBuilder();
        if (queryParams.length() > 0) {
            queryParamTemplate.append("{?");
            queryParamTemplate.append(queryParams.subSequence(0, queryParams.length() - 1));
            queryParamTemplate.append("}");
        }

        String httpMethod = resourceMethod.getHttpMethod();
        return new ResourceMethodRef(paramRefs, templateBuilder, queryParamTemplate.toString(), httpMethod);
    }

    private Boolean handleParam(Parameter param, Map<Integer, ParamRef> paramRefs, StringBuilder queryParams, Integer index) {
        Parameter.Source source = param.getSource();
        switch (source) {
            case BEAN_PARAM:
                paramRefs.put(index, createParamRefFromBean(param, paramRefs, queryParams, index));
                break;
            case QUERY:
                queryParams.append(param.getSourceName());
                queryParams.append(',');
                paramRefs.put(index, createParamRef(param));
                break;
            case MATRIX:
                paramRefs.put(index, createParamRef(param));
                break;
            case PATH:
                paramRefs.put(index, createParamRef(param));
                break;
            default:
                return false;
        }
        return true;
    }

    private ParamRef createParamRef(Parameter param) {
        Object defaultObject = null;
        if (param.hasDefaultValue()) {
            String defaultValue = param.getDefaultValue();
            Class paramClass = param.getRawType();
            if (ReflectionHelper.isPrimitive(param.getRawType())) {
                paramClass = primitiveToClassMap.get(paramClass);
            }
            ParamConverter<?> converter = converterProvider.getConverter(paramClass, null, param.getAnnotations());
            defaultObject = converter.fromString(defaultValue);
        }
        return new ParamRef(param.getSourceName(), defaultObject, param.getSource());
    }

    private BeanParamRef createParamRefFromBean(Parameter param, Map<Integer, ParamRef> paramRefs, StringBuilder queryParams, Integer index) {

        List<BeanParamRefField> valueResolvers = new ArrayList<>();

        Class paramType = param.getRawType();
        Field[] fields = ReflectionHelper.getAllFieldsPA(paramType).run();
        for (Field field : fields) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            if (hasParameterAnnotations(annotations)) {
                handleField(valueResolvers, field, param, paramRefs, queryParams, index, annotations);
            }
        }

        Method[] methods = ReflectionHelper.getMethodsPA(paramType).run();
        for (Method m : methods) {
            Annotation[] annotations = m.getDeclaredAnnotations();
            if (hasParameterAnnotations(annotations)) {
                if (ReflectionHelper.isGetter(m) || ReflectionHelper.isSetter(m)) {
                    try {
                        Field field = paramType.getDeclaredField(ReflectionHelper.getPropertyName(m));
                        handleField(valueResolvers, field, param, paramRefs, queryParams, index, annotations);
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (hasBeanParameterAnnotations(annotations)) {
                throw new RuntimeException("nested BeanParam is not supported");
            }
        }

        return new BeanParamRef(createParamRef(param), valueResolvers);
    }

    private boolean hasParameterAnnotations(final Annotation[] as) {
        for (final Annotation annotation : as) {
            Class<? extends Annotation> a = annotation.annotationType();
            if (a.equals(MatrixParam.class) || a.equals(QueryParam.class) || a.equals(PathParam.class)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBeanParameterAnnotations(final Annotation[] as) {
        for (final Annotation annotation : as) {
            Class<? extends Annotation> a = annotation.annotationType();
            if (a.equals(BeanParam.class)) {
                return true;
            }
        }
        return false;
    }

    private Object getDefaultValue(final Annotation[] as, Class paramType, Type genericType) {
        for (final Annotation annotation : as) {
            Class<? extends Annotation> a = annotation.annotationType();
            if (a.equals(DefaultValue.class)) {
                String defaultValue = ((DefaultValue) annotation).value();
                if (ReflectionHelper.isPrimitive(paramType)) {
                    paramType = primitiveToClassMap.get(paramType);
                }
                ParamConverter<?> converter = converterProvider.getConverter(paramType, genericType, as);
                return converter.fromString(defaultValue);
            }
        }
        return null;
    }

    private void handleField(List<BeanParamRefField> valueResolvers, Field field, Parameter param, Map<Integer, ParamRef> paramRefs,
                             StringBuilder queryParams, Integer index, Annotation[] annotations) {
        Class paramType = param.getRawType();
        Class fieldType = field.getType();
        Type fieldGenType = field.getGenericType();
        Parameter p = Parameter.create(paramType, paramType, param.isEncoded(),
                fieldType, fieldGenType, annotations);
        if (handleParam(p, paramRefs, queryParams, index)) {
            Object defaultValue = getDefaultValue(annotations, fieldType, fieldGenType);
            field.setAccessible(true);
            valueResolvers.add(new BeanParamRefField(p.getSourceName(), defaultValue, p.getSource(), field));
        }
    }
}