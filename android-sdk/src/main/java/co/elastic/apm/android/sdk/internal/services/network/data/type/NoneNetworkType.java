package co.elastic.apm.android.sdk.internal.services.network.data.type;

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
