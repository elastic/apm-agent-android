package co.elastic.apm.android.test.attributes.traces;

import static org.mockito.Mockito.doReturn;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;

import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowTelephonyManager;

import java.util.ArrayList;
import java.util.List;

import co.elastic.apm.android.test.common.spans.Spans;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTest;
import io.opentelemetry.sdk.trace.data.SpanData;

public class GlobalAttributeTest extends BaseRobolectricTest {

    @Test
    public void whenASpanIsCreated_verifyItHasSessionIdAsParam() {
        SpanData customSpan = getSpanData();

        Spans.verify(customSpan)
                .hasAttribute("session.id");
    }

    @Test
    public void whenASpanIsCreated_verifyItHasTypeMobileAsParam() {
        SpanData customSpan = getSpanData();

        Spans.verify(customSpan)
                .hasAttribute("type", "mobile");
    }

    @Config(application = AppWithCarrierInfo.class)
    @Test
    public void whenASpanIsCreated_verifyItHasCarrierInfoParams() {
        SpanData customSpan = getSpanData();

        Spans.verify(customSpan)
                .hasAttribute("net.host.carrier.name", AppWithCarrierInfo.SIM_OPERATOR_NAME)
                .hasAttribute("net.host.carrier.mcc", "123")
                .hasAttribute("net.host.carrier.mnc", "456")
                .hasAttribute("net.host.carrier.icc", AppWithCarrierInfo.SIM_COUNTRY_ISO);
    }

    @Config(application = AppWithWifiConnectivity.class)
    @Test
    public void whenASpanIsCreated_andThereIsAWifiConnection_verifyItHasWifiConnectivityParams() {
        SpanData span = getSpanData();

        Spans.verify(span)
                .hasAttribute("net.host.connection.type", "wifi");
    }

    @Config(application = AppWithMobileConnectivity.class)
    @Test
    public void whenASpanIsCreated_andThereIsAMobileConnection_verifyItHasMobileConnectivityParams() {
        SpanData span = getSpanData();

        Spans.verify(span)
                .hasAttribute("net.host.connection.type", "cell");
    }

    @Config(application = AppWithMobileConnectivityAndSubtype.class)
    @Test
    public void whenASpanIsCreated_andThereIsAMobileConnectionWithSubtype_verifyItHasMobileConnectivityParams() {
        SpanData span = getSpanData();

        Spans.verify(span)
                .hasAttribute("net.host.connection.type", "cell")
                .hasAttribute("net.host.connection.subtype", "EDGE");
    }

    private SpanData getSpanData() {
        SpanAttrHost host = new SpanAttrHost();

        host.methodWithSpan();

        List<SpanData> spans = getRecordedSpans(1);
        return spans.get(0);
    }

    private static class AppWithCarrierInfo extends MainApp {
        private static final String SIM_OPERATOR = "123456";
        private static final String SIM_OPERATOR_NAME = "elasticphone";
        private static final String SIM_COUNTRY_ISO = "us";

        @Override
        public void onCreate() {
            ShadowTelephonyManager shadowTelephonyManager = Shadows.shadowOf((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
            shadowTelephonyManager.setSimOperator(SIM_OPERATOR);
            shadowTelephonyManager.setSimState(TelephonyManager.SIM_STATE_READY);
            shadowTelephonyManager.setSimOperatorName(SIM_OPERATOR_NAME);
            shadowTelephonyManager.setSimCountryIso(SIM_COUNTRY_ISO);
            super.onCreate();
        }
    }

    private static class AppWithWifiConnectivity extends MainApp {
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

    private static class AppWithMobileConnectivity extends MainApp {
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

    private static class AppWithMobileConnectivityAndSubtype extends MainApp {
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
