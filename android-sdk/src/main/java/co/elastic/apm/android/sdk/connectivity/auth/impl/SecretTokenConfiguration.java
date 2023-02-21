package co.elastic.apm.android.sdk.connectivity.auth.impl;

import co.elastic.apm.android.sdk.connectivity.auth.AuthConfiguration;

public final class SecretTokenConfiguration implements AuthConfiguration {
    private final String token;

    public SecretTokenConfiguration(String token) {
        this.token = token;
    }

    @Override
    public String asAuthorizationHeaderValue() {
        return "Bearer " + token;
    }

    @Override
    public Type getType() {
        return Type.SECRET_TOKEN;
    }
}
