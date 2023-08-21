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
package co.elastic.apm.android.sdk.features.persistence;

import co.elastic.apm.android.sdk.features.persistence.scheduler.ExportScheduler;

public final class PersistenceConfiguration {
    public final boolean enabled;
    public final int maxCacheSize;
    public final ExportScheduler exportScheduler;

    private PersistenceConfiguration(Builder builder) {
        this.maxCacheSize = builder.maxCacheSize;
        this.enabled = builder.enabled;
        exportScheduler = builder.exportScheduler;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public boolean enabled = false;
        private int maxCacheSize = 60 * 1024 * 1024; // 60 MB

        public ExportScheduler exportScheduler;

        private Builder() {
        }

        /**
         * Enables/disables the feature to store signal items in disk and exporting them later.
         * Disabled by default.
         */
        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the maximum amount of bytes that this tool can use to store cached signals in disk.
         * A smaller amount of space will be used if there's not enough space in disk to allocate
         * the value set in here.
         */
        public Builder setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        /**
         * Sets a scheduler that will take care of periodically read data stored in disk and
         * export it.
         */
        public Builder setExportScheduler(ExportScheduler exportScheduler) {
            this.exportScheduler = exportScheduler;
            return this;
        }

        public PersistenceConfiguration build() {
            if (exportScheduler == null) {
                exportScheduler = ExportScheduler.getDefault(60 * 1000); // Every minute by default.
            }
            return new PersistenceConfiguration(this);
        }
    }
}
