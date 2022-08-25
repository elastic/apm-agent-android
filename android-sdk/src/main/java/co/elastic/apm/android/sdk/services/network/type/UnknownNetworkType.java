package co.elastic.apm.android.sdk.services.network.type;

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
