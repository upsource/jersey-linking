package de.upsource.jersey.linking;

import javax.ws.rs.core.UriBuilder;
import java.util.Map;

/**
 * information related to parameters of a resource method,
 * used in link creation
 */
class ResourceMethodRef {

    private Map<Integer, ParamRef> params;
    private String template;
    private String templateQueryParams;
    private String httpMethod;

    ResourceMethodRef(Map<Integer, ParamRef> params,
                      UriBuilder templateBuilder,
                      String templateQueryParams,
                      String httpMethod) {
        this.params = params;
        this.template = templateBuilder.toTemplate();
        this.templateQueryParams = templateQueryParams;
        this.httpMethod = httpMethod;
    }

    /**
     * @return map of parameter references, key is the index of the related argument in the Method
     */
    ParamRef getParam(Integer index) {
        return params.get(index);
    }

    /**
     * @return the template of the path to the resource method
     */
    String getTemplate() {
        return template;
    }

    /**
     * @return template of query parameter
     */
    String getTemplateQueryParams() {
        return templateQueryParams;
    }

    /**
     * @return the http method
     */
    String getHttpMethod() {
        return httpMethod;
    }
}
