package co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher;

public final class FetchResult {
    public final Integer maxAgeInSeconds;
    public final boolean hasChanged;

    public FetchResult(Integer maxAgeInSeconds, boolean hasChanged) {
        this.maxAgeInSeconds = maxAgeInSeconds;
        this.hasChanged = hasChanged;
    }
}
