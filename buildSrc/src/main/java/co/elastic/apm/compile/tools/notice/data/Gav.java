package co.elastic.apm.compile.tools.notice.data;

public class Gav {
    public final String group;
    public final String artifactName;
    public final String version;

    public Gav(String group, String artifactName, String version) {
        this.group = group;
        this.artifactName = artifactName;
        this.version = version;
    }

    public static Gav parseUri(String uri) {
        String[] parts = uri.split(":");
        return new Gav(parts[0], parts[1], parts[2]);
    }
}
