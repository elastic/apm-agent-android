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
package co.elastic.apm.android.sdk.internal.features.centralconfig.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.slf4j.Logger;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;

public class CentralConfigFetchWorker extends Worker {
    private static final String ARG_TIMEOUT_INTERVAL_NAME = "timeout_interval_seconds";

    public static Data createInputData(int timeoutIntervalInSeconds) {
        return new Data.Builder().putInt(ARG_TIMEOUT_INTERVAL_NAME, timeoutIntervalInSeconds).build();
    }

    public CentralConfigFetchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Logger logger = Elog.getLogger(CentralConfigFetchWorker.class);
        logger.info("Starting central config worker");
        try {
            CentralConfigurationManager configurationManager = new CentralConfigurationManager(getApplicationContext());
            Integer maxAgeInSeconds = configurationManager.sync();
            rescheduleIfNeeded(logger, maxAgeInSeconds);
            logger.info("Central config worker succeeded");
            return Result.success();
        } catch (Throwable t) {
            logger.error("Central config worker failed", t);
            return Result.retry();
        }
    }

    private void rescheduleIfNeeded(Logger logger, Integer maxAgeInSeconds) {
        if (maxAgeInSeconds != null && maxAgeInSeconds != getCurrentTimeoutInSeconds()) {
            logger.debug("Rescheduling central config worker to run every {} seconds", maxAgeInSeconds);
            CentralConfigurationManager.scheduleSync(getApplicationContext(), maxAgeInSeconds);
        }
    }

    private int getCurrentTimeoutInSeconds() {
        return getInputData().getInt(ARG_TIMEOUT_INTERVAL_NAME, 0);
    }
}
