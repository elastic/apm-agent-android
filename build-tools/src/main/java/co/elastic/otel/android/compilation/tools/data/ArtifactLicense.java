package co.elastic.otel.android.compilation.tools.data;

public class ArtifactLicense {
    public final String uri;
    public final String licenseId;

    public ArtifactLicense(String displayName, String licenseName) {
        this.uri = displayName;
        this.licenseId = licenseName;
    }

    public String serialize() {
        return uri + "|" + licenseId;
    }

    public static ArtifactLicense parse(String line) {
        String[] parts = line.split("\\|");
        String displayName = parts[0];
        String licenseName = parts[1];

        return new ArtifactLicense(displayName, licenseName);
    }
}
