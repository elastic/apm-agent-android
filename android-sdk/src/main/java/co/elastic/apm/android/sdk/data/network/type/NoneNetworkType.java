package co.elastic.apm.android.sdk.data.network.type;

enum NoneNetworkType implements NetworkType {
    INSTANCE;

    @Override
    public String getName() {
        return "unavailable";
    }

    @Override
    public String getSubTypeName() {
        return null;
    }
}
