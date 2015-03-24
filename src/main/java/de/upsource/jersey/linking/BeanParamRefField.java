package de.upsource.jersey.linking;

import org.glassfish.jersey.server.model.Parameter;

import java.lang.reflect.Field;

/**
 * this object references a field of a BeanParam
 */
class BeanParamRefField extends ParamRef {

    protected Field field;

    /**
     * construct a bean field param reference (related to a Field of a BeanParam)
     *
     * @param name         name of the param (value of annotation)
     * @param defaultValue defaultValue if exist
     * @param source       type of param (Matrix, Query or Path)
     * @param field        related Field
     */
    BeanParamRefField(String name, Object defaultValue, Parameter.Source source, Field field) {
        super(name, defaultValue, source);
        this.field = field;
    }

    /**
     * @return the Field of the bean
     */
    Field getField() {
        return field;
    }
}
