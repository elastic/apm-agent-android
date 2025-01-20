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
package co.elastic.otel.android.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import co.elastic.otel.android.internal.logging.SimpleLoggingPolicy;
import co.elastic.otel.android.logging.LogLevel;
import co.elastic.otel.android.logging.LoggingPolicy;

public class SimpleLoggingPolicyTest {

    @Test
    public void verifyProvidedValues() {
        LoggingPolicy policy = new SimpleLoggingPolicy(true, LogLevel.INFO);
        LoggingPolicy policy2 = new SimpleLoggingPolicy(false, LogLevel.ERROR);

        assertTrue(policy.isEnabled());
        assertEquals(LogLevel.INFO, policy.getMinimumLevel());
        assertFalse(policy2.isEnabled());
        assertEquals(LogLevel.ERROR, policy2.getMinimumLevel());
    }
}