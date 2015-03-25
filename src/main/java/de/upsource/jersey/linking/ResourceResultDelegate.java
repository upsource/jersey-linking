package de.upsource.jersey.linking;

import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by meiko on 29.12.14.
 */
public class ResourceResultDelegate {

    private String template;
    private String fullTemplate;
    private String link;
    private String httpMethod;

    public ResourceResultDelegate(ResourceMethodRef resourceMethodRef,
                                  Object[] methodArguments) throws InvocationTargetException, IllegalAccessException, UnsupportedEncodingException {

        template = resourceMethodRef.getTemplate() + resourceMethodRef.getTemplateQueryParams();
        UriBuilder linkUriBuilder = UriBuilder.fromPath(resourceMethodRef.getTemplate());


        for (int i = 0; i < methodArguments.length; i++) {
            Object argument = methodArguments[i];
            ParamRef param = resourceMethodRef.getParam(i);
            handleArgument(param, argument, linkUriBuilder);
        }
        URI linkUri = linkUriBuilder.build();
        link = linkUri.toString();
        httpMethod = resourceMethodRef.getHttpMethod();
        fullTemplate = linkUri.getPath() + resourceMethodRef.getTemplateQueryParams();
    }

    private void handleArgument(ParamRef param, Object argument, UriBuilder linkUriBuilder) throws IllegalAccessException, UnsupportedEncodingException {
        if (param == null) {
            return;
        }
        if (argument == null) {
            argument = param.getDefaultValue();
        }
        if (argument == null) {
            return;
        }
        if (param instanceof BeanParamRef) {
            BeanParamRef beanParamRef = (BeanParamRef) param;
            List<BeanParamRefField> valueResolvers = beanParamRef.getFields();
            for (BeanParamRefField paramRef : valueResolvers) {
                Object arg = paramRef.getField().get(argument);
                handleArgument(paramRef, arg, linkUriBuilder);
            }
        } else {
            handleParam(param, argument, linkUriBuilder);
        }
    }

    private void handleParam(ParamRef param, Object argument, UriBuilder linkUriBuilder) throws UnsupportedEncodingException {

        switch (param.getSource()) {
            case QUERY:
                linkUriBuilder.queryParam(param.getName(), URLEncoder.encode(argument.toString(), "UTF-8"));
                break;
            case PATH:
                linkUriBuilder.resolveTemplate(param.getName(), URLEncoder.encode(argument.toString(), "UTF-8"));
                break;
            case MATRIX:
                linkUriBuilder.matrixParam(param.getName(), URLEncoder.encode(argument.toString(), "UTF-8"));
                break;
            default:
                throw new RuntimeException("nested beanParam");
        }
    }

    public String getTemplate() {
        return template;
    }

    public String getFullTemplate() {
        return fullTemplate;
    }

    public String getLink() {
        return link;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
}
