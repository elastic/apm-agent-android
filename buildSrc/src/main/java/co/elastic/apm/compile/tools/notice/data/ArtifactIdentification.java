package co.elastic.apm.compile.tools.notice.data;

public class ArtifactIdentification {
    public final String name;
    public final String url;
    public final String gradleUri;

    public ArtifactIdentification(String name, String url, String gradleUri) {
        this.name = name;
        this.url = url;
        this.gradleUri = gradleUri;
    }

    public String getDisplayName() {
        String displayName = name;

        if (url != null) {
            displayName += " (" + url + ")";
        }

        return displayName;
    }
}
