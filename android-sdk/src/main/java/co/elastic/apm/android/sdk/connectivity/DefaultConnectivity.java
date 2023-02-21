package co.elastic.apm.android.sdk.connectivity;

import co.elastic.apm.android.sdk.connectivity.auth.AuthConfiguration;

final class DefaultConnectivity implements Connectivity {
    private final String endpoint;
    private final AuthConfiguration authConfiguration;

    private DefaultConnectivity(String endpoint, AuthConfiguration authConfiguration) {
        this.endpoint = endpoint;
        this.authConfiguration = authConfiguration;
    }

    @Override
    public String endpoint() {
        return endpoint;
    }

    @Override
    public AuthConfiguration authConfiguration() {
        return authConfiguration;
    }

    public static class Builder {
        private final String endpoint;
        private AuthConfiguration authConfiguration;

        Builder(String endpoint) {
            this.endpoint = endpoint;
        }

        public Connectivity buildWithSecretToken(String secretToken) {
            authConfiguration = AuthConfiguration.secretToken(secretToken);
            return build();
        }

        public Connectivity build() {
            return new DefaultConnectivity(endpoint, authConfiguration);
        }
    }
}
