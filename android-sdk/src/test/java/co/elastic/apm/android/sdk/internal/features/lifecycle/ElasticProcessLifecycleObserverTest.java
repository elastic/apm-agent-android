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
package co.elastic.apm.android.sdk.internal.features.lifecycle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import androidx.lifecycle.LifecycleOwner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.events.EventBuilder;

public class ElasticProcessLifecycleObserverTest {

    private ElasticProcessLifecycleObserver instance;
    private LifecycleOwner owner;
    private EventBuilder eventEmitter;

    @Before
    public void setUp() {
        owner = mock(LifecycleOwner.class);
        eventEmitter = mock(EventBuilder.class);
        doReturn(eventEmitter).when(eventEmitter).setAttributes(any());
        instance = new ElasticProcessLifecycleObserver(eventEmitter);
    }

    @Test
    public void verifyCreatedEvent() {
        instance.onCreate(owner);

        verifyLifecycleEvent("created");
    }

    @Test
    public void verifyStartedEvent() {
        instance.onStart(owner);

        verifyLifecycleEvent("started");
    }

    @Test
    public void verifyResumedEvent() {
        instance.onResume(owner);

        verifyLifecycleEvent("resumed");
    }

    @Test
    public void verifyPausedEvent() {
        instance.onPause(owner);

        verifyLifecycleEvent("paused");
    }

    @Test
    public void verifyStoppedEvent() {
        instance.onStop(owner);

        verifyLifecycleEvent("stopped");
    }

    private void verifyLifecycleEvent(String state) {
        verify(eventEmitter).setAttributes(Attributes.of(AttributeKey.stringKey("lifecycle.state"), state));
        verify(eventEmitter).emit();
    }

    @After
    public void tearDown() {
        verifyNoInteractions(owner);
    }
}