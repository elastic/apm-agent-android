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
package co.elastic.otel.android.internal.opamp.impl.recipe;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.AgentDescriptionAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.AgentDisconnectAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.AgentToServerAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.CapabilitiesAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.EffectiveConfigAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.FlagsAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.InstanceUidAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.RemoteConfigStatusAppender;
import co.elastic.otel.android.internal.opamp.impl.recipe.appenders.SequenceNumberAppender;
import co.elastic.otel.android.internal.opamp.request.Field;

@ExtendWith(MockitoExtension.class)
class AgentToServerAppendersTest {
    @Mock
    private AgentDescriptionAppender agentDescriptionAppender;
    @Mock
    private EffectiveConfigAppender effectiveConfigAppender;
    @Mock
    private RemoteConfigStatusAppender remoteConfigStatusAppender;
    @Mock
    private SequenceNumberAppender sequenceNumberAppender;
    @Mock
    private CapabilitiesAppender capabilitiesAppender;
    @Mock
    private FlagsAppender flagsAppender;
    @Mock
    private InstanceUidAppender instanceUidAppender;
    @Mock
    private AgentDisconnectAppender agentDisconnectAppender;
    @InjectMocks
    private AgentToServerAppenders appenders;

    @Test
    void verifyAppenderList() {
        verifyMapping(Field.AGENT_DESCRIPTION, agentDescriptionAppender);
        verifyMapping(Field.EFFECTIVE_CONFIG, effectiveConfigAppender);
        verifyMapping(Field.REMOTE_CONFIG_STATUS, remoteConfigStatusAppender);
        verifyMapping(Field.SEQUENCE_NUM, sequenceNumberAppender);
        verifyMapping(Field.CAPABILITIES, capabilitiesAppender);
        verifyMapping(Field.INSTANCE_UID, instanceUidAppender);
        verifyMapping(Field.FLAGS, flagsAppender);
        verifyMapping(Field.AGENT_DISCONNECT, agentDisconnectAppender);
    }

    private void verifyMapping(Field type, AgentToServerAppender appender) {
        assertThat(appenders.getForField(type)).isEqualTo(appender);
    }
}
