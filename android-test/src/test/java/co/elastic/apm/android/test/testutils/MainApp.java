package co.elastic.apm.android.test.testutils;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;

public class MainApp extends BaseRobolectricTestApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmAgent.initialize(this, getConnectivity());
    }
}