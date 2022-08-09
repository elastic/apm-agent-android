package co.elastic.apm.android.sdk.traces.http.filtering;

enum SpanAllRule implements HttpSpanRule {
    INSTANCE;

    @Override
    public boolean isSpannable(HttpRequest request) {
        return true;
    }
}
