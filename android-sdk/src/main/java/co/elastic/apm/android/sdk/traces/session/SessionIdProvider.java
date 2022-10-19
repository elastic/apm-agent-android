package co.elastic.apm.android.sdk.traces.session;

/**
 * Provides an identifier for all the {@link io.opentelemetry.api.trace.Span}s created during a
 * period of time. The idea of a session is to provide a context that covers many transactions
 * that a user did in order to fulfil their needs using an application. For most apps, a session
 * could start when the user opens the app, and end when the user closes the app, or when the
 * app is forced to get closed due to an unexpected error. But for other apps, such as a ticketing
 * app for a queue in a bank for example, the app will always be open, but a session might start when
 * a person starts the process to get a new ticket, and end when the ticket is printed.
 */
public interface SessionIdProvider {
    String getSessionId();
}
