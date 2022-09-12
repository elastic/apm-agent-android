package co.elastic.apm.android.sdk.internal.logging;

import org.slf4j.Logger;

public class Elog {

    public static Logger getLogger(String name) {
        return new AndroidLogger(name);
    }
}
