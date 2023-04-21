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
package co.elastic.apm.android.sdk.internal.services.appinfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.services.Service;

public class AppInfoService implements Service {
    private final Context appContext;

    public AppInfoService(Context appContext) {
        this.appContext = appContext;
    }

    public boolean isPermissionGranted(String permissionName) {
        return appContext.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isInDebugMode() {
        return (appContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public int getVersionCode() {
        try {
            PackageInfo packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Elog.getLogger().error("Error providing versionCode", e);
            return 0;
        }
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
        return Service.Names.APP_INFO;
    }
}
