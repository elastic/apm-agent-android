package co.elastic.otel.android.internal.opamp.impl.state;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import opamp.proto.AgentCapabilities;

class CapabilitiesStateTest {
    private CapabilitiesState capabilitiesState;

    @BeforeEach
    void setUp() {
        capabilitiesState = CapabilitiesState.create();
    }

    @Test
    void verifyDefaultValue() {
        assertThat(hasFlag(AgentCapabilities.AgentCapabilities_ReportsStatus.getValue())).isTrue();
    }

    @Test
    void verifyAddingAndRemovingCapabilities() {
        int capability = AgentCapabilities.AgentCapabilities_AcceptsPackages.getValue();
        assertThat(hasFlag(capability)).isFalse();

        capabilitiesState.add(capability);
        assertThat(hasFlag(capability)).isTrue();
        assertThat(hasFlag(AgentCapabilities.AgentCapabilities_ReportsStatus.getValue())).isTrue();

        capabilitiesState.remove(capability);
        assertThat(hasFlag(capability)).isFalse();
        assertThat(hasFlag(AgentCapabilities.AgentCapabilities_ReportsStatus.getValue())).isTrue();
    }

    private boolean hasFlag(long flag) {
        return (capabilitiesState.get() & flag) == flag;
    }
}
