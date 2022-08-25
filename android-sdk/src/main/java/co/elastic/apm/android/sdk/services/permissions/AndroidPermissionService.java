package co.elastic.apm.android.sdk.services.permissions;

import android.content.Context;
import android.content.pm.PackageManager;

import co.elastic.apm.android.sdk.services.Service;

public class AndroidPermissionService implements Service {
    private final Context appContext;

    public AndroidPermissionService(Context appContext) {
        this.appContext = appContext;
    }

    public boolean isPermissionGranted(String permissionName) {
        return appContext.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void start() {
        // No operation.
    }

    @Override
    public void stop() {
        // No operation.
    }

    @Override
    public String name() {
        return Service.Names.ANDROID_PERMISSIONS;
    }
}
