package co.elastic.apm.android.test.testutils;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.test.testutils.base.BaseTestApplication;

public class MainApp extends BaseTestApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmAgent.initialize(this, getConnectivity());
    }
}