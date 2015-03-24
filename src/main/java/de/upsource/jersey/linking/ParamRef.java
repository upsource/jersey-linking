package de.upsource.jersey.linking;


import org.glassfish.jersey.server.model.Parameter;

/**
 * information on a resource method parameter (annotated with MatrixParam, QueryParam or PathParam)
 */
class ParamRef {

    protected String name;
    protected Object defaultValue;
    protected Parameter.Source source;

    /**
     * Creates a resource parameter reference
     *
     * @param name         name of the parameter (value of annotation)
     * @param defaultValue default value of this parameter (see DefaultValue Annotation), null if no defaultValue was specified
     * @param source       type of Parameter: Matrix, Query or Path
     */
    ParamRef(String name, Object defaultValue, Parameter.Source source) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.source = source;
    }

    /**
     * @return name of the parameter (value of annotation)
     */
    String getName() {
        return name;
    }

    /**
     * @return default value of this parameter (see DefaultValue Annotation), null if no defaultValue was specified
     */
    Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return type of Parameter: Matrix, Query or Path
     */
    Parameter.Source getSource() {
        return source;
    }

}