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
package co.elastic.apm.android.sdk.configuration.logging;

import co.elastic.apm.android.sdk.configuration.logging.impl.DefaultLoggingPolicy;
import co.elastic.apm.android.sdk.configuration.logging.impl.SimpleLoggingPolicy;

/**
 * Defines the internal logging behavior of this library.
 */
public interface LoggingPolicy {

    /**
     * Provides the default logging policy which will log all the {@link LogLevel}s on debuggable applications
     * and only logs from level INFO and above for non-debuggable applications.
     * <p>
     * No logs will be created until the Agent is initialized.
     */
    static LoggingPolicy getDefault() {
        return DefaultLoggingPolicy.create();
    }

    /**
     * Convenience method for creating a static logging policy.
     *
     * @param isEnabled    - Whether the logs are enabled or not.
     * @param minimumLevel - The minimum {@link LogLevel}, all the logs with this level and above will get printed, others will be ignored.
     */
    static LoggingPolicy create(boolean isEnabled, LogLevel minimumLevel) {
        return new SimpleLoggingPolicy(isEnabled, minimumLevel);
    }

    /**
     * Whether logging in general is enabled or not. This value will be checked before the log level.
     */
    boolean isEnabled();

    /**
     * If logging is enabled, this value will be checked later to filter which logs will
     * get printed. Logs with at least the level provided here or higher will pass, other ones (below the level provided here) will be ignored.
     */
    LogLevel getMinimumLevel();
}
