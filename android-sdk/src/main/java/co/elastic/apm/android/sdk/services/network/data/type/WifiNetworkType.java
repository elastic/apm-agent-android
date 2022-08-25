package co.elastic.apm.android.sdk.services.network.data.type;

enum WifiNetworkType implements NetworkType {
    INSTANCE;

    @Override
    public String getName() {
        return "wifi";
    }

    @Override
    public String getSubTypeName() {
        return null;
    }
}
