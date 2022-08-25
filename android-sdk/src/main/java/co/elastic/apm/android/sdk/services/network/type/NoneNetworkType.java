package co.elastic.apm.android.sdk.services.network.type;

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
