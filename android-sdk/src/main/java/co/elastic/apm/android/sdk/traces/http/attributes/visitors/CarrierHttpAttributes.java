package co.elastic.apm.android.sdk.traces.http.attributes.visitors;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.providers.LazyProvider;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.network.NetworkService;
import co.elastic.apm.android.sdk.internal.services.network.data.CarrierInfo;
import co.elastic.apm.android.sdk.traces.http.attributes.HttpAttributesVisitor;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class CarrierHttpAttributes implements HttpAttributesVisitor {
    private final LazyProvider<NetworkService> networkServiceProvider;

    public CarrierHttpAttributes() {
        networkServiceProvider = LazyProvider.of(() -> ElasticApmAgent.get().getService(Service.Names.NETWORK));
    }

    @Override
    public void visit(AttributesBuilder builder, HttpRequest request) {
        CarrierInfo carrierInfo = networkServiceProvider.get().getCarrierInfo();
        if (carrierInfo != null) {
            builder.put(SemanticAttributes.NET_HOST_CARRIER_NAME, carrierInfo.name);
            builder.put(SemanticAttributes.NET_HOST_CARRIER_MCC, carrierInfo.mcc);
            builder.put(SemanticAttributes.NET_HOST_CARRIER_MNC, carrierInfo.mnc);
            builder.put(SemanticAttributes.NET_HOST_CARRIER_ICC, carrierInfo.icc);
        }
    }
}
