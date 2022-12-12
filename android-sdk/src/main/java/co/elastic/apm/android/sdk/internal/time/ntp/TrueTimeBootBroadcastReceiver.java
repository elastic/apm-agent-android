package co.elastic.apm.android.sdk.internal.time.ntp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import co.elastic.apm.android.common.internal.logging.Elog;

public class TrueTimeBootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Elog.getLogger().debug("Clearing TrueTime disk cache as we've detected a boot");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            TrueTimeWrapper trueTimeWrapper = new TrueTimeWrapper(context);
            trueTimeWrapper.withSharedPreferencesCache();
            trueTimeWrapper.clearCachedInfo();
        }
    }
}
