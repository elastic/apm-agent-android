package co.elastic.apm.android.sdk.services.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import co.elastic.apm.android.sdk.services.Service;
import co.elastic.apm.android.sdk.services.ServiceNames;
import co.elastic.apm.android.sdk.services.network.type.NetworkType;
import co.elastic.apm.android.sdk.services.network.utils.CellSubTypeProvider;

public class NetworkService extends ConnectivityManager.NetworkCallback implements Service {
    private final ConnectivityManager connectivityManager;
    private final TelephonyManager telephonyManager;
    private NetworkType networkType = NetworkType.none();

    public NetworkService(Context context) {
        Context appContext = context.getApplicationContext();
        connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public void start() {
        connectivityManager.registerDefaultNetworkCallback(this);
    }

    @Override
    public void stop() {
        connectivityManager.unregisterNetworkCallback(this);
    }

    @Override
    public String name() {
        return ServiceNames.NETWORK;
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