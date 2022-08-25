package co.elastic.apm.android.sdk.services.network.data.type;

enum UnknownNetworkType implements NetworkType {
    INSTANCE;

    @Override
    public String getName() {
        return "unknown";
    }

    @Override
    public String getSubTypeName() {
        return null;
    }
}
