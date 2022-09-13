package co.elastic.apm.compile.tools.data;

public class ArtifactLicense {
    public final String displayName;
    public final String licenseId;

    public ArtifactLicense(String displayName, String licenseName) {
        this.displayName = displayName;
        this.licenseId = licenseName;
    }

    public String serialize() {
        return displayName + "|" + licenseId;
    }

    public static ArtifactLicense parse(String line) {
        String[] parts = line.split("\\|");
        String displayName = parts[0];
        String licenseName = parts[1];

        return new ArtifactLicense(displayName, licenseName);
    }
}
