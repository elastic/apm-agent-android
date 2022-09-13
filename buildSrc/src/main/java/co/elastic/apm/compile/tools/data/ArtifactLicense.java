package co.elastic.apm.compile.tools.data;

public class ArtifactLicense {
    public final String displayName;
    public final String licenseName;

    public ArtifactLicense(String displayName, String licenseName) {
        this.displayName = displayName;
        this.licenseName = licenseName;
    }

    public String serialize() {
        return displayName + "|" + licenseName;
    }

    public static ArtifactLicense parse(String line) {
        String[] parts = line.split("\\|");
        String displayName = parts[0];
        String licenseName = parts[1];

        return new ArtifactLicense(displayName, licenseName);
    }
}
