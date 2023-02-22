package co.elastic.apm.android.sdk.internal.features.centralconfig;

import java.util.Map;

public interface CentralConfigurationListener {

    void onUpdate(Map<String, String> configs);
}
