package co.elastic.apm.android.sdk.metrics.tools;

import co.elastic.apm.android.sdk.internal.api.filter.Filter;
import io.opentelemetry.sdk.metrics.data.MetricData;

public interface MetricFilter extends Filter<MetricData> {
}
