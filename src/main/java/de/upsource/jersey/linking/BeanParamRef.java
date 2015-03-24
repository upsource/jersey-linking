package de.upsource.jersey.linking;

import org.glassfish.jersey.server.model.Parameter;

import java.util.List;

/**
 * this references a BeanParam argument of a resource method
 * it contains a list of fields (properties of related bean) which are annotated with a Path-, Query or MatrixParam
 */
class BeanParamRef extends ParamRef {

    protected List<BeanParamRefField> fields;

    /**
     * construct a reference to a BeanParam (resource method argument)
     *
     * @param param  param information
     * @param fields fields of related bean (fields are annotated with a Path-, Query or MatrixParam)
     */
    BeanParamRef(ParamRef param, List<BeanParamRefField> fields) {
        super(param.getName(), param.getDefaultValue(), Parameter.Source.BEAN_PARAM);
        this.fields = fields;
    }

    /**
     * return the fields param of related bean
     *
     * @return bean fields
     */
    List<BeanParamRefField> getFields() {
        return fields;
    }
}
