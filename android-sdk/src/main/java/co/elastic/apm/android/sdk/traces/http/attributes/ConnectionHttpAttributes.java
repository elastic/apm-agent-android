package co.elastic.apm.android.sdk.traces.http.attributes;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.services.Service;
import co.elastic.apm.android.sdk.services.network.NetworkService;
import co.elastic.apm.android.sdk.services.network.data.type.NetworkType;
import co.elastic.apm.android.sdk.traces.http.data.HttpRequest;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class ConnectionHttpAttributes implements HttpAttributesVisitor {
    private final NetworkService networkService;

    public ConnectionHttpAttributes() {
        networkService = (NetworkService) ElasticApmAgent.get().getService(Service.Names.NETWORK);
    }

    @Override
    public void visit(AttributesBuilder builder, HttpRequest request) {
        NetworkType networkType = networkService.getType();
        builder.put(SemanticAttributes.NET_HOST_CONNECTION_TYPE, networkType.getName());
        if (networkType.getSubTypeName() != null) {
            builder.put(SemanticAttributes.NET_HOST_CONNECTION_SUBTYPE, networkType.getSubTypeName());
        }
    }
}
