package co.elastic.apm.android.test.testutils;

import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;

public interface AgentDependenciesProvider {

    NtpManager getNtpManager();
}
