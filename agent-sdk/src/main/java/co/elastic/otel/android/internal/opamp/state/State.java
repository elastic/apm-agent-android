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
package co.elastic.otel.android.internal.opamp.state;

import com.github.f4b6a3.uuid.UuidCreator;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import opamp.proto.AgentCapabilities;
import opamp.proto.AgentToServerFlags;
import opamp.proto.RemoteConfigStatuses;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 * <p>
 * Provides a request field value in its {@link #get()} method, and it also notifies the OpAMP
 * client when a new value is available by calling its own {@link #notifyListeners()} method.
 *
 * @param <T> The type of value it provides.
 */
public abstract class State<T> implements Supplier<T> {
    private final Storage<T> storage;
    private final Set<Listener> listeners = Collections.synchronizedSet(new HashSet<>());

    public final void addListener(Listener listener) {
        listeners.add(listener);
    }

    public final void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    protected final void notifyListeners() {
        synchronized (listeners) {
            for (Listener listener : listeners) {
                listener.onUpdate(getType());
            }
        }
    }

    public void set(@Nonnull T value) {
        if (storage.set(value)) {
            notifyListeners();
        }
    }

    @Nonnull
    @Override
    public T get() {
        return storage.get();
    }

    public abstract FieldType getType();

    public interface Listener {
        void onUpdate(FieldType type);
    }

    private State(Storage<T> storage) {
        this.storage = storage;
    }

    public static class InstanceUid extends State<byte[]> {
        public static InstanceUid createRandomInMemory() {
            UUID uuid = UuidCreator.getTimeOrderedEpoch();
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(uuid.getMostSignificantBits());
            buffer.putLong(uuid.getLeastSignificantBits());
            return createInMemory(buffer.array());
        }

        public static InstanceUid createInMemory(byte[] value) {
            return new InstanceUid(Storage.inMemory(value));
        }

        public InstanceUid(Storage<byte[]> storage) {
            super(storage);
        }

        @Override
        public final FieldType getType() {
            return FieldType.INSTANCE_UID;
        }
    }

    public static class SequenceNum extends State<Integer> {
        public static SequenceNum createInMemory() {
            return new SequenceNum(Storage.inMemory(1));
        }

        public SequenceNum(Storage<Integer> storage) {
            super(storage);
        }

        public void increment() {
            set(get() + 1);
        }

        @Override
        public final FieldType getType() {
            return FieldType.SEQUENCE_NUM;
        }
    }

    public static class AgentDescription extends State<opamp.proto.AgentDescription> {
        public static AgentDescription createInMemory() {
            return new AgentDescription(Storage.inMemory(new opamp.proto.AgentDescription.Builder().build()));
        }

        public AgentDescription(Storage<opamp.proto.AgentDescription> storage) {
            super(storage);
        }

        @Override
        public final FieldType getType() {
            return FieldType.AGENT_DESCRIPTION;
        }
    }

    public static class Capabilities extends State<Integer> {
        public static Capabilities createInMemory() {
            return new Capabilities(Storage.inMemory(AgentCapabilities.AgentCapabilities_ReportsStatus.getValue()));
        }

        public Capabilities(Storage<Integer> storage) {
            super(storage);
        }

        public void add(int capabilities) {
            set(get() | capabilities);
        }

        public void remove(int capabilities) {
            set(get() & ~capabilities);
        }

        @Override
        public final FieldType getType() {
            return FieldType.CAPABILITIES;
        }
    }

    public static class EffectiveConfig extends State<EffectiveConfig> {
        public EffectiveConfig(Storage<EffectiveConfig> storage) {
            super(storage);
        }

        @Override
        public final FieldType getType() {
            return FieldType.EFFECTIVE_CONFIG;
        }
    }

    public static class RemoteConfigStatus extends State<opamp.proto.RemoteConfigStatus> {
        public static RemoteConfigStatus createInMemory() {
            return new RemoteConfigStatus(Storage.inMemory(
                    new opamp.proto.RemoteConfigStatus.Builder()
                            .status(RemoteConfigStatuses.RemoteConfigStatuses_UNSET)
                            .build()));
        }

        public RemoteConfigStatus(Storage<opamp.proto.RemoteConfigStatus> storage) {
            super(storage);
        }

        @Override
        public final FieldType getType() {
            return FieldType.REMOTE_CONFIG_STATUS;
        }
    }

    public static class Flags extends State<Integer> {
        public static Flags createInMemory() {
            return new Flags(Storage.inMemory(AgentToServerFlags.AgentToServerFlags_Unspecified.getValue()));
        }

        public Flags(Storage<Integer> storage) {
            super(storage);
        }

        @Override
        public final FieldType getType() {
            return FieldType.FLAGS;
        }
    }
}
