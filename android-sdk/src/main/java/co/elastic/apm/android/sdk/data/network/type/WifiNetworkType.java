package co.elastic.apm.android.sdk.data.network.type;

public class WifiNetworkType implements NetworkType {
    private final String subType;

    public WifiNetworkType(String subType) {
        this.subType = subType;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getSubTypeName() {
        return null;
    }
}
