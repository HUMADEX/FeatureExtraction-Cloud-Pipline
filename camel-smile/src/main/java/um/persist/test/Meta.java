package um.persist.test;

import um.persist.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta {

    private String id;
    private String version_id;
    private String last_updated;

    public Meta() {
    }

    public String getId() {
        return this.id;
    }

    public String getVersionId() {
        return this.version_id;
    }

    public String getLastUpdated() {
        return this.last_updated;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVersionId(String version_id) {
        this.version_id = version_id;
    }

    public void setLastUpdated(String last_updated) {
        this.last_updated = last_updated;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", versionId=" + version_id +
                ", lastUpdated=" + last_updated +
                '}';
    }
}
