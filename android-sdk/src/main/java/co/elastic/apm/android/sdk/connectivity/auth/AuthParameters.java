package co.elastic.apm.android.sdk.connectivity.auth;

public interface AuthParameters {

    String asHeader();

    Type getType();

    enum Type {
        SECRET_TOKEN("secretToken"),
        API_KEY("apiKey");

        public final String typeName;

        Type(String typeName) {
            this.typeName = typeName;
        }
    }
}
