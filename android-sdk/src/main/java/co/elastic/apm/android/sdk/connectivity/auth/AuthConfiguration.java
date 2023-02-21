package co.elastic.apm.android.sdk.connectivity.auth;

import co.elastic.apm.android.sdk.connectivity.auth.impl.SecretTokenConfiguration;

public interface AuthConfiguration {

    static AuthConfiguration secretToken(String token) {
        return new SecretTokenConfiguration(token);
    }

    String asAuthorizationHeaderValue();

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
