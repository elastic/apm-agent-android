package co.elastic.apm.android.sdk.okhttp;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.EventListener;

public class OkhttpEventListener extends EventListener {

    @Override
    public void callStart(@NonNull Call call) {
        super.callStart(call);
        Log.d("cesar", "CALL STARTED");
    }

    @Override
    public void callEnd(@NonNull Call call) {
        super.callEnd(call);
        Log.d("cesar", "CALL ENDED");
    }

    @Override
    public void callFailed(@NonNull Call call, @NonNull IOException ioe) {
        super.callFailed(call, ioe);
        Log.d("cesar", "Call failed");
    }
}
