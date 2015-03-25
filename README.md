## imperative Linking !!! Caution: experimental library !!!

Goal: we need a comfortable imperative way to create hypermedia-links (between jersey resources)
Instead of using strings to specify links and uri-templates, the new cool feature "Method references" (Java 8) is used.

Alternatives:
 * Uri-Builder: https://jersey.java.net/documentation/latest/uris-and-links.html
 * Declarative-Linking: https://jersey.java.net/documentation/latest/declarative-linking.html

TODOs:
 * MatrixParam: uri-template do not support MatrixParams now
 * BeanParams: implement support for constructor injected parameters
 * UriTemplate: test and fix full template
 * BeanParams: test nested BeanParam support

How to use:
 * see test classes here: https://github.com/upsource/jersey-linking/tree/master/src/test/java/de/upsource/jersey/linking
  (or the incomplete example below)

```java
//a transfer object, containing lists of links and uri-templates
import de.upsource.jersey.linking.ResourceResultDelegate;
public class ADto {

    //hypermedia links
    public Map<String, String> links;

    //uri-templates
    public Map<String, String> templates;

    //the selfProxy provides a hypermedia link (and/or a uri-template) to particular resource
    public ResourceDTO(ResourceResultDelegate selfProxy) {
        links = new HashMap<>();
        templates = new HashMap<>();
        links.put("self", selfProxy.getLink());
        templates.put("self", selfProxy.getFullTemplate());
    }
}

//the resource
@Path("/")
public class AResource {

    @Inject
    private Linker linker;

    @Path("a") @GET
    public ADto getA() {

        //self link per constructor parameter (/a)
        ADto result = new ADto(linker.proxy(AResource.class, (o) -> o.getA()));

        //link to resource b  (/b?name=specificB)
        result.links.add("b", linker.link(AResource.class, (o) -> o.getB("specificB")));

        //also BeanParams are supported
        result.links.add("a", linker.link(AResource.class, (o) -> o.getC(new BeanX(...))));

        //null is given as an argument -> default parameter is used /d?str=BlaBla)
        result.links.add("d", linker.link(AResource.class, (o) -> o.getD(null)));

        result.templates.add("d" linker.template(AResource.class, (o) -> o.getD(null)));

        return result;
    }

    @Path("b") @GET
    public BDto get(@QueryParam("name") String name) {
        //...
    }

    @Path("c") @GET
    public CDto get(@BeanParam BeanX bean) {
        //...
    }

    @Path("d") @GET
    public DDto get(@DefaultValue("BlaBla") @QueryParam("str") String str) {
        //...
    }

}
```

How it works:
 * the library uses objenesis (http://objenesis.org/)
 * proxies for resource methods are created (return type is exchanged)
