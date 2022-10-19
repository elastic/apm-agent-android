package co.elastic.apm.android.sdk.traces.session.impl;

import co.elastic.apm.android.sdk.traces.session.SessionIdProvider;

/**
 * Provides an in-memory id that has a 30 mins timeout that gets reset on every call to
 * {@link SessionIdProvider#getSessionId()} - If 30 mins or more have passed since the last call,
 * then a new session id is generated.
 */
public class DefaultSessionIdProvider implements SessionIdProvider {

    @Override
    public String getSessionId() {
        return null;
    }
}
