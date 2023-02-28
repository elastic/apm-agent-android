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
package co.elastic.apm.android.sdk.internal.features.centralconfig.initializer;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;

import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.testutils.ImmediateBackgroundExecutor;

public class CentralConfigurationInitializerTest {
    private CentralConfigurationManager manager;
    private Context context;
    private CentralConfigurationInitializer initializer;

    @Before
    public void setUp() {
        manager = mock(CentralConfigurationManager.class);
        context = mock(Context.class);
        initializer = new CentralConfigurationInitializer(context, new ImmediateBackgroundExecutor(), manager);
    }

    @Test
    public void verifyInitialization() throws IOException {
        initializer.initialize();

        InOrder inOrder = inOrder(manager);
        inOrder.verify(manager).publishCachedConfig();
        inOrder.verify(manager).sync();
    }
}