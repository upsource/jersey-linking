package de.upsource.jersey.linking;

/**
 * Created by meiko on 25.03.15.
 */
public class Link {

    private String httpMethod;
    private String path;

    public Link(String httpMethod, String path) {
        this.httpMethod = httpMethod;
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }
}
