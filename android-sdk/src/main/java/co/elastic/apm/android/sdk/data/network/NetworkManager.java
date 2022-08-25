package co.elastic.apm.android.sdk.data.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import co.elastic.apm.android.sdk.data.network.type.NetworkType;
import co.elastic.apm.android.sdk.data.network.utils.CellSubTypeProvider;

public class NetworkManager extends ConnectivityManager.NetworkCallback {
    private final ConnectivityManager connectivityManager;
    private final TelephonyManager telephonyManager;
    private NetworkType networkType = NetworkType.none();

    public NetworkManager(Context context) {
        Context appContext = context.getApplicationContext();
        connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
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

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        networkType = getNetworkType(networkCapabilities);
    }

    private NetworkType getNetworkType(NetworkCapabilities networkCapabilities) {
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetworkType.cell(CellSubTypeProvider.getSubtypeName(telephonyManager));
        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkType.wifi();
        } else {
            return NetworkType.unknown();
        }
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        networkType = NetworkType.none();
    }
}