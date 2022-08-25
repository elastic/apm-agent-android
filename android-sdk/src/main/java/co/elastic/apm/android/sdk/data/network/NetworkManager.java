package co.elastic.apm.android.sdk.data.network;

import android.content.Context;
import android.net.ConnectivityManager;

import co.elastic.apm.android.sdk.data.network.type.NetworkType;

public class NetworkManager extends ConnectivityManager.NetworkCallback {
    private final ConnectivityManager connectivityManager;
    private NetworkType networkType = NetworkType.None.INSTANCE;

    public NetworkManager(Context context) {
        Context appContext = context.getApplicationContext();
        connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void init() {
        connectivityManager.registerDefaultNetworkCallback(this);
    }

    public void cleanUp() {
        connectivityManager.unregisterNetworkCallback(this);
    }

    public NetworkType getType() {
        return networkType;
    }
}