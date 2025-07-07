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
package co.elastic.otel.android.internal.opamp.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.elastic.otel.android.internal.opamp.state.State;

@ExtendWith(MockitoExtension.class)
class OpampClientStateTest {
    @Mock
    private State.RemoteConfigStatus remoteConfigStatus;
    @Mock
    private State.SequenceNum sequenceNum;
    @Mock
    private State.AgentDescription agentDescription;
    @Mock
    private State.Capabilities capabilities;
    @Mock
    private State.InstanceUid instanceUid;
    @Mock
    private State.Flags flags;
    @Mock
    private State.EffectiveConfig effectiveConfig;
    @InjectMocks
    private OpampClientState state;

    @Test
    void verifyAllFields() {
        assertThat(state.getAll())
                .containsExactlyInAnyOrder(remoteConfigStatus,
                        sequenceNum,
                        agentDescription,
                        capabilities,
                        instanceUid,
                        flags,
                        effectiveConfig);
    }
}