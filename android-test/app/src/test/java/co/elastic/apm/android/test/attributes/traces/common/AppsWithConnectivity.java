package co.elastic.apm.android.test.attributes.traces.common;

import static org.mockito.Mockito.doReturn;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;

import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowTelephonyManager;

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.test.testutils.MainApp;

public final class AppsWithConnectivity {

    public static class WithWifi extends MainApp {
        @Override
        public void onCreate() {
            super.onCreate();
            ShadowConnectivityManager shadowConnectivityManager = Shadows.shadowOf((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
            List<ConnectivityManager.NetworkCallback> callbacks = new ArrayList<>(shadowConnectivityManager.getNetworkCallbacks());
            ConnectivityManager.NetworkCallback defaultNetworkCallback = callbacks.get(0);

            NetworkCapabilities capabilities = Mockito.mock(NetworkCapabilities.class);
            doReturn(true).when(capabilities).hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            defaultNetworkCallback.onCapabilitiesChanged(Mockito.mock(Network.class), capabilities);
        }
    }

    public static class WithCellular extends MainApp {
        @Override
        public void onCreate() {
            super.onCreate();
            ShadowConnectivityManager shadowConnectivityManager = Shadows.shadowOf((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
            List<ConnectivityManager.NetworkCallback> callbacks = new ArrayList<>(shadowConnectivityManager.getNetworkCallbacks());
            ConnectivityManager.NetworkCallback defaultNetworkCallback = callbacks.get(0);

            NetworkCapabilities capabilities = Mockito.mock(NetworkCapabilities.class);
            doReturn(true).when(capabilities).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            defaultNetworkCallback.onCapabilitiesChanged(Mockito.mock(Network.class), capabilities);
        }
    }

    public static class WithCellularAndSubtype extends MainApp {
        @Override
        public void onCreate() {
            super.onCreate();
            ShadowConnectivityManager shadowConnectivityManager = Shadows.shadowOf((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
            ShadowTelephonyManager shadowTelephonyManager = Shadows.shadowOf((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
            ShadowApplication shadowContext = Shadows.shadowOf(this);
            shadowContext.grantPermissions(Manifest.permission.READ_PHONE_STATE);
            List<ConnectivityManager.NetworkCallback> callbacks = new ArrayList<>(shadowConnectivityManager.getNetworkCallbacks());
            ConnectivityManager.NetworkCallback defaultNetworkCallback = callbacks.get(0);

            NetworkCapabilities capabilities = Mockito.mock(NetworkCapabilities.class);
            doReturn(true).when(capabilities).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            shadowTelephonyManager.setDataNetworkType(TelephonyManager.NETWORK_TYPE_EDGE);

            defaultNetworkCallback.onCapabilitiesChanged(Mockito.mock(Network.class), capabilities);
        }
    }
}
