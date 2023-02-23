package co.elastic.apm.android.sdk.internal.features.centralconfig.fetcher;

public final class FetchResult {
    public final Integer maxAgeInSeconds;
    public final boolean configurationHasChanged;

    public FetchResult(Integer maxAgeInSeconds, boolean configurationHasChanged) {
        this.maxAgeInSeconds = maxAgeInSeconds;
        this.configurationHasChanged = configurationHasChanged;
    }
}
