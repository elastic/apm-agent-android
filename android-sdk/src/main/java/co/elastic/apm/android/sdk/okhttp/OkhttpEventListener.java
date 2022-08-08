package co.elastic.apm.android.sdk.okhttp;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.EventListener;

public class OkhttpEventListener extends EventListener {

    @Override
    public void callStart(Call call) {
        super.callStart(call);
        Log.d("cesar", "CALL STARTED");
    }

    @Override
    public void callEnd(Call call) {
        super.callEnd(call);
        Log.d("cesar", "CALL ENDED");
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
        super.callFailed(call, ioe);
        Log.d("cesar", "Call failed");
    }
}
