package com.elastic.apm.android.plugin;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class ApmAndroidAgentPluginTest {

    @Rule
    public TemporaryFolder projectTemporaryFolder;

    @Before
    public void setUp() {
        projectTemporaryFolder = new TemporaryFolder();
    }
}
