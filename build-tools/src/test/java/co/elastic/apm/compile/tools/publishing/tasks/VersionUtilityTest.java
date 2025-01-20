package co.elastic.apm.compile.tools.publishing.tasks;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

import co.elastic.otel.android.compilation.tools.publishing.tasks.VersionUtility;

public class VersionUtilityTest {

    @Test
    public void verifyMinorVersionBump() {
        verifyBumpFromTo("1.2.0", "1.3.0");
        verifyBumpFromTo("1.1.0", "1.2.0");
        verifyBumpFromTo("1.1.1", "1.2.0");
        verifyBumpFromTo("1.4.1.2.3", "1.5.0");
    }

    private void verifyBumpFromTo(String from, String to) {
        assertEquals(to, VersionUtility.bumpMinorVersion(from));
    }
}