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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.elastic.otel.android.internal.opamp.state.State;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class OpampClientState {
    public final State.RemoteConfigStatus remoteConfigStatus;
    public final State.SequenceNum sequenceNum;
    public final State.AgentDescription agentDescription;
    public final State.Capabilities capabilities;
    public final State.InstanceUid instanceUid;
    public final State.Flags flags;
    public final State.EffectiveConfig effectiveConfig;
    private final List<State<?>> items;

    public OpampClientState(State.RemoteConfigStatus remoteConfigStatus,
                            State.SequenceNum sequenceNum,
                            State.AgentDescription agentDescription,
                            State.Capabilities capabilities,
                            State.InstanceUid instanceUid,
                            State.Flags flags,
                            State.EffectiveConfig effectiveConfig) {
        this.remoteConfigStatus = remoteConfigStatus;
        this.sequenceNum = sequenceNum;
        this.agentDescription = agentDescription;
        this.capabilities = capabilities;
        this.instanceUid = instanceUid;
        this.flags = flags;
        this.effectiveConfig = effectiveConfig;

        List<State<?>> providedItems = new ArrayList<>();
        providedItems.add(remoteConfigStatus);
        providedItems.add(sequenceNum);
        providedItems.add(agentDescription);
        providedItems.add(capabilities);
        providedItems.add(instanceUid);
        providedItems.add(flags);
        providedItems.add(effectiveConfig);

        items = Collections.unmodifiableList(providedItems);
    }

    public List<State<?>> getAll() {
        return items;
    }
}
