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
package co.elastic.apm.android.common.internal.logging;

import androidx.annotation.NonNull;

import org.slf4j.Logger;

public class Elog {

    private static ELoggerFactory loggerFactory = new ELoggerFactory.Noop();
    private static boolean initialized = false;

    public static synchronized void init(ELoggerFactory factory) {
        if (initialized) {
            return;
        }
        loggerFactory = factory;
        initialized = true;
    }

    public static Logger getLogger(@NonNull String name) {
        return loggerFactory.getLogger(name);
    }

    public static Logger getLogger(@NonNull Class<?> type) {
        return loggerFactory.getLogger(type);
    }

    public static Logger getLogger() {
        return loggerFactory.getDefaultLogger();
    }
}
