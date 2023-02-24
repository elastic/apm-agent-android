package co.elastic.apm.android.test.testutils;

import co.elastic.apm.android.sdk.internal.features.centralconfig.initializer.CentralConfigurationInitializer;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;

public interface AgentDependenciesProvider {

    NtpManager getNtpManager();

    CentralConfigurationInitializer getCentralConfigurationInitializer();
}
