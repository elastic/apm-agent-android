package co.elastic.apm.android.sdk.data.network.type;

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
