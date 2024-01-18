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
package co.elastic.apm.android.sdk.internal.services.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.appinfo.AppInfoService;
import co.elastic.apm.android.sdk.internal.services.network.data.CarrierInfo;
import co.elastic.apm.android.sdk.internal.services.network.data.type.NetworkType;
import co.elastic.apm.android.sdk.internal.services.network.utils.CellSubTypeProvider;

public class NetworkService extends ConnectivityManager.NetworkCallback implements Service {
    private final ConnectivityManager connectivityManager;
    private final TelephonyManager telephonyManager;
    private NetworkType networkType = NetworkType.none();
    private AppInfoService appInfoService;

    public static NetworkService create(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return new NetworkService(connectivityManager, telephonyManager);
    }

    NetworkService(ConnectivityManager connectivityManager, TelephonyManager telephonyManager) {
        this.connectivityManager = connectivityManager;
        this.telephonyManager = telephonyManager;
    }

    @Override
    public void start() {
        connectivityManager.registerDefaultNetworkCallback(this);
    }

    @Override
    public void stop() {
        connectivityManager.unregisterNetworkCallback(this);
    }

    @Override
    public String name() {
        return Service.Names.NETWORK;
    }

    @NotNull
    public synchronized NetworkType getType() {
        return networkType;
    }

    private synchronized void setType(NetworkType networkType) {
        this.networkType = networkType;
    }

    @Nullable
    public CarrierInfo getCarrierInfo() {
        String simOperator = getSimOperator();
        if (simOperator == null) {
            return null;
        }

        String mcc = simOperator.substring(0, 3);
        String mnc = simOperator.substring(3);
        return new CarrierInfo(telephonyManager.getSimOperatorName(),
                mcc,
                mnc,
                telephonyManager.getSimCountryIso());
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        setType(getNetworkType(networkCapabilities));
    }

    private NetworkType getNetworkType(NetworkCapabilities networkCapabilities) {
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetworkType.cell(CellSubTypeProvider.getSubtypeName(telephonyManager, getAppInfoService()));
        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkType.wifi();
        } else {
            return NetworkType.unknown();
        }
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        setType(NetworkType.none());
    }

    private String getSimOperator() {
        if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
            String simOperator = telephonyManager.getSimOperator();
            if (simOperator != null && simOperator.length() > 3) {
                return simOperator;
            }
        }

        return null;
    }

    private AppInfoService getAppInfoService() {
        if (appInfoService == null) {
            appInfoService = ServiceManager.get().getService(Names.APP_INFO);
        }

        return appInfoService;
    }
}