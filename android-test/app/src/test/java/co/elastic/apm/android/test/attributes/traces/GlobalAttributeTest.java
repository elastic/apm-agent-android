package co.elastic.apm.android.test.attributes.traces;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowTelephonyManager;

import java.util.List;

import co.elastic.apm.android.test.attributes.traces.common.AppsWithConnectivity;
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
                .hasAttribute("network.carrier.name", AppWithCarrierInfo.SIM_OPERATOR_NAME)
                .hasAttribute("network.carrier.mcc", "123")
                .hasAttribute("network.carrier.mnc", "456")
                .hasAttribute("network.carrier.icc", AppWithCarrierInfo.SIM_COUNTRY_ISO);
    }

    @Config(application = AppsWithConnectivity.WithWifi.class)
    @Test
    public void whenASpanIsCreated_andThereIsAWifiConnection_verifyItHasWifiConnectivityParams() {
        SpanData span = getSpanData();

        Spans.verify(span)
                .hasAttribute("network.connection.type", "wifi");
    }

    @Config(application = AppsWithConnectivity.WithCellular.class)
    @Test
    public void whenASpanIsCreated_andThereIsAMobileConnection_verifyItHasMobileConnectivityParams() {
        SpanData span = getSpanData();

        Spans.verify(span)
                .hasAttribute("network.connection.type", "cell");
    }

    @Config(application = AppsWithConnectivity.WithCellularAndSubtype.class)
    @Test
    public void whenASpanIsCreated_andThereIsAMobileConnectionWithSubtype_verifyItHasMobileConnectivityParams() {
        SpanData span = getSpanData();

        Spans.verify(span)
                .hasAttribute("network.connection.type", "cell")
                .hasAttribute("network.connection.subtype", "EDGE");
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
}
