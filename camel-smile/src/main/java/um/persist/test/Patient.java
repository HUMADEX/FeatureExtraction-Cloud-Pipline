package um.persist.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Patient {

    private String id;
    private Meta meta;
    private String resource_type;

    public Patient() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public String getResourceType() {
        return resource_type;
    }

    public void setResourceType(String resource_type) {
        this.resource_type = resource_type;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", meta=" + meta +
                ", resourceType=" + resource_type +
                '}';
    }
}
