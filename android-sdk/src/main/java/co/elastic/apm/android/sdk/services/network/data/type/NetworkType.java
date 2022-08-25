package co.elastic.apm.android.sdk.services.network.data.type;

public interface NetworkType {

    static NetworkType wifi() {
        return WifiNetworkType.INSTANCE;
    }

    static NetworkType none() {
        return NoneNetworkType.INSTANCE;
    }

    static NetworkType unknown() {
        return UnknownNetworkType.INSTANCE;
    }

    static NetworkType cell(String subTypeName) {
        return new CellNetworkType(subTypeName);
    }

    String getName();

    String getSubTypeName();
}
