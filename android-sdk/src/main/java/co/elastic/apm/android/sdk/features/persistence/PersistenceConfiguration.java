package co.elastic.apm.android.sdk.features.persistence;

public final class PersistenceConfiguration {
    public final int maxCacheSize;

    private PersistenceConfiguration(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxCacheSize = 60 * 1024 * 1024; // 60 MB

        private Builder() {
        }

        public Builder setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public PersistenceConfiguration build() {
            return new PersistenceConfiguration(maxCacheSize);
        }
    }
}
