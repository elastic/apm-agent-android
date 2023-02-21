package co.elastic.apm.android.sdk.connectivity;

import co.elastic.apm.android.sdk.connectivity.auth.AuthConfiguration;

final class DefaultConnectivity implements Connectivity {
    private final String endpoint;
    private final AuthConfiguration authConfiguration;

    DefaultConnectivity(String endpoint, AuthConfiguration authConfiguration) {
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
}
