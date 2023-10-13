/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Properties;

import co.elastic.apm.android.common.ApmInfo;

public class CompilationConfigTest extends BaseAssetsVerificationTest {

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Rule
    public TemporaryFolder projectTemporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        addPlugin("com.android.application");
        addPlugin("co.elastic.apm.android");
    }

    @Test
    public void compileConfig_whenNoServerUrlIsSet_fail() {
        setUpProject();

        runFailedGradle("assembleDebug");

        verifyOutputContains("'serverUrl' because it has no value available");
    }

    @Test
    public void compileConfig_verifyDefaultValues() {
        String serverUrl = "http://some.url";
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
        assertNull(properties.getProperty(ApmInfo.KEY_SERVER_SECRET_TOKEN));
    }

    @Test
    public void compileConfig_whenNoServerUrlProvidedInGradle_useServerUrlFromEnvironmentVariable() {
        String serverUrlEnv = "http://some.url";
        environmentVariables.set("ELASTIC_APM_SERVER_URL", serverUrlEnv);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverUrlEnv, properties.getProperty(ApmInfo.KEY_SERVER_URL));
        assertNull(properties.getProperty(ApmInfo.KEY_SERVER_SECRET_TOKEN));
    }

    @Test
    public void compileConfig_whenNoSecretTokenProvidedInGradle_useSecretTokenFromEnvironmentVariable() {
        String serverUrl = "http://some.url";
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        String secretTokenEnv = "Some env secret token";
        environmentVariables.set("ELASTIC_APM_SECRET_TOKEN", secretTokenEnv);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
        assertEquals(secretTokenEnv, properties.getProperty(ApmInfo.KEY_SERVER_SECRET_TOKEN));
    }

    @Test
    public void compileConfig_verifyOverridingServerUrl_withEnvironmentVariable() {
        String serverUrl = "http://some.url";
        String serverUrlEnv = "http://some.env.url";
        environmentVariables.set("ELASTIC_APM_SERVER_URL", serverUrlEnv);
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverUrlEnv, properties.getProperty(ApmInfo.KEY_SERVER_URL));
        assertNull(properties.getProperty(ApmInfo.KEY_SERVER_SECRET_TOKEN));
    }

    @Test
    public void compileConfig_verifySettingApiKey() {
        String serverUrl = "http://server.url";
        String apiKey = "some.apiKey";
        getDefaultElasticBlockBuilder().setApiKey(apiKey);
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(apiKey, properties.getProperty(ApmInfo.KEY_SERVER_API_KEY));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
    }

    @Test
    public void compileConfig_verifyOverridingApiKey_withEnvironmentVariable() {
        String serverUrl = "http://server.url";
        String apiKey = "some.apiKey";
        String apiKeyEnv = "some.environment-provided.token";
        environmentVariables.set("ELASTIC_APM_API_KEY", apiKeyEnv);
        getDefaultElasticBlockBuilder().setApiKey(apiKey);
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(apiKeyEnv, properties.getProperty(ApmInfo.KEY_SERVER_API_KEY));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
    }


    @Test
    public void compileConfig_verifySettingSecretToken() {
        String serverUrl = "http://server.url";
        String serverToken = "some.token";
        getDefaultElasticBlockBuilder().setSecretToken(serverToken);
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverToken, properties.getProperty(ApmInfo.KEY_SERVER_SECRET_TOKEN));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
    }

    @Test
    public void compileConfig_verifyOverridingSecretToken_withEnvironmentVariable() {
        String serverUrl = "http://server.url";
        String serverToken = "some.token";
        String serverTokenEnv = "some.environment-provided.token";
        environmentVariables.set("ELASTIC_APM_SECRET_TOKEN", serverTokenEnv);
        getDefaultElasticBlockBuilder().setSecretToken(serverToken);
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverTokenEnv, properties.getProperty(ApmInfo.KEY_SERVER_SECRET_TOKEN));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
    }

    @Test
    public void compileConfig_verifyOverridingServiceName() {
        String serverUrl = "http://server.url";
        String serviceName = "My App";
        getDefaultElasticBlockBuilder().setServiceName(serviceName);
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(serviceName, properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
    }

    @Test
    public void compileConfig_verifyOverridingServiceName_withEnvironmentVariable() {
        String serverUrl = "http://server.url";
        String serviceName = "My App";
        String serviceNameEnv = "My App name from env";
        environmentVariables.set("ELASTIC_APM_SERVICE_NAME", serviceNameEnv);
        getDefaultElasticBlockBuilder().setServiceName(serviceName);
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(serviceNameEnv, properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals("1.0", properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
    }

    @Test
    public void compileConfig_verifyOverridingServiceVersion() {
        String serverUrl = "http://server.url";
        String serviceVersion = "1.0.0";
        getDefaultElasticBlockBuilder().setServiceVersion(serviceVersion);
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals(serviceVersion, properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
    }

    @Test
    public void compileConfig_verifyOverridingServiceVersion_withEnvironmentVariable() {
        String serverUrl = "http://server.url";
        String serviceVersion = "1.0.0";
        String serviceVersionEnv = "1.0.0 from env";
        environmentVariables.set("ELASTIC_APM_SERVICE_VERSION", serviceVersionEnv);
        getDefaultElasticBlockBuilder().setServiceVersion(serviceVersion);
        getDefaultElasticBlockBuilder().setServerUrl(serverUrl);
        setUpProject();

        runGradle("assembleDebug");

        verifyTaskIsSuccessful(":debugGenerateApmInfo");
        Properties properties = getGeneratedProperties("debug");
        assertEquals(getAndroidAppId(), properties.getProperty(ApmInfo.KEY_SERVICE_NAME));
        assertEquals(serviceVersionEnv, properties.getProperty(ApmInfo.KEY_SERVICE_VERSION));
        assertEquals("debug", properties.getProperty(ApmInfo.KEY_SERVICE_ENVIRONMENT));
        assertEquals(serverUrl, properties.getProperty(ApmInfo.KEY_SERVER_URL));
    }

    @Override
    protected File getProjectDir() {
        return projectTemporaryFolder.getRoot();
    }

    @Override
    protected String getAndroidGradlePluginVersion() {
        return "7.4.0";
    }
}
