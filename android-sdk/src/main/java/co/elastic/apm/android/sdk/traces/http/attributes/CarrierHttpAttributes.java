package co.elastic.apm.android.sdk.traces.http.attributes;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.services.Service;
import co.elastic.apm.android.sdk.services.network.NetworkService;
import co.elastic.apm.android.sdk.services.network.data.CarrierInfo;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class CarrierHttpAttributes implements HttpAttributesVisitor {
    private final NetworkService networkService;

    public CarrierHttpAttributes() {
        networkService = (NetworkService) ElasticApmAgent.get().getService(Service.Names.NETWORK);
    }

    @Override
    public void visit(AttributesBuilder builder, HttpRequest request) {
        CarrierInfo carrierInfo = networkService.getCarrierInfo();
        if (carrierInfo != null) {
            builder.put(SemanticAttributes.NET_HOST_CARRIER_NAME, carrierInfo.name);
            builder.put(SemanticAttributes.NET_HOST_CARRIER_MCC, carrierInfo.mcc);
            builder.put(SemanticAttributes.NET_HOST_CARRIER_MNC, carrierInfo.mnc);
            builder.put(SemanticAttributes.NET_HOST_CARRIER_ICC, carrierInfo.icc);
        }
    }
}
