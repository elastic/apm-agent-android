package co.elastic.apm.compile.tools.data;

public class ArtifactIdentification {
    public final String name;
    public final String url;

    public ArtifactIdentification(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getDisplayName() {
        String displayName = name;

        if (url != null) {
            displayName += " (" + url + ")";
        }

        return displayName;
    }
}
