package co.elastic.apm.android.sdk.data.network.type;

public class WifiNetworkType implements NetworkType {

    @Override
    public String getName() {
        return "wifi";
    }

    @Override
    public String getSubTypeName() {
        return null;
    }
}
