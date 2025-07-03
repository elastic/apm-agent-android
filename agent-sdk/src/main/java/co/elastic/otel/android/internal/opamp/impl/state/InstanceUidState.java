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
package co.elastic.otel.android.internal.opamp.impl.state;

import com.github.f4b6a3.uuid.UuidCreator;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

import co.elastic.otel.android.internal.opamp.state.InMemoryState;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class InstanceUidState extends InMemoryState<byte[]> {

    public static InstanceUidState createRandom() {
        UUID uuid = UuidCreator.getTimeOrderedEpoch();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return create(buffer.array());
    }

    public static InstanceUidState create(byte[] uuid) {
        return new InstanceUidState(uuid);
    }

    private InstanceUidState(byte[] initialState) {
        super(initialState);
    }

    @Override
    protected boolean areEqual(byte[] first, byte[] second) {
        return Arrays.equals(first, second);
    }
}
