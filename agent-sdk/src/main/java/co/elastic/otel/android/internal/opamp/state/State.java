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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 * <p>
 * Provides a request field value in its {@link #get()} method, and it also notifies the OpAMP
 * client when a new value is available by calling its own {@link #notifyUpdate()} method.
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

    public final void notifyUpdate() {
        synchronized (listeners) {
            for (Listener listener : listeners) {
                listener.onUpdate(getType());
            }
        }
    }

    public final void set(@Nonnull T value) {
        if (storage.set(value)) {
            notifyUpdate();
        }
    }

    @Nullable
    @Override
    public final T get() {
        return storage.get();
    }

    @Nonnull
    public final T mustGet() {
        return Objects.requireNonNull(get());
    }

    public abstract FieldType getType();

    public interface Listener {
        void onUpdate(FieldType type);
    }

    private State(Storage<T> storage) {
        this.storage = storage;
    }

    public static final class InstanceUid extends State<byte[]> {
        public static InstanceUid createRandomInMemory() {
            UUID uuid = UuidCreator.getTimeOrderedEpoch();
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(uuid.getMostSignificantBits());
            buffer.putLong(uuid.getLeastSignificantBits());
            return createInMemory(buffer.array());
        }

        public static InstanceUid createInMemory(byte[] value) {
            return new InstanceUid(Storage.inMemory(value, Arrays::equals));
        }

        public InstanceUid(Storage<byte[]> storage) {
            super(storage);
        }

        @Override
        public FieldType getType() {
            return FieldType.INSTANCE_UID;
        }
    }

    public static final class SequenceNum extends State<Integer> {
        public static SequenceNum createInMemory(int value) {
            return new SequenceNum(Storage.inMemory(value));
        }

        public SequenceNum(Storage<Integer> storage) {
            super(storage);
        }

        public void increment() {
            set(mustGet() + 1);
        }

        @Override
        public FieldType getType() {
            return FieldType.SEQUENCE_NUM;
        }
    }

    public static final class AgentDescription extends State<opamp.proto.AgentDescription> {
        public static AgentDescription createInMemory(opamp.proto.AgentDescription value) {
            return new AgentDescription(Storage.inMemory(value));
        }

        public AgentDescription(Storage<opamp.proto.AgentDescription> storage) {
            super(storage);
        }

        @Override
        public FieldType getType() {
            return FieldType.AGENT_DESCRIPTION;
        }
    }

    public static final class Capabilities extends State<Long> {
        public static Capabilities createInMemory(long value) {
            return new Capabilities(Storage.inMemory(value));
        }

        public Capabilities(Storage<Long> storage) {
            super(storage);
        }

        public void add(long capabilities) {
            set(mustGet() | capabilities);
        }

        public void remove(long capabilities) {
            set(mustGet() & ~capabilities);
        }

        @Override
        public FieldType getType() {
            return FieldType.CAPABILITIES;
        }
    }

    public static final class EffectiveConfig extends State<opamp.proto.EffectiveConfig> {
        public EffectiveConfig(Storage<opamp.proto.EffectiveConfig> storage) {
            super(storage);
        }

        @Override
        public FieldType getType() {
            return FieldType.EFFECTIVE_CONFIG;
        }
    }

    public static final class RemoteConfigStatus extends State<opamp.proto.RemoteConfigStatus> {
        public static RemoteConfigStatus createInMemory(opamp.proto.RemoteConfigStatus value) {
            return new RemoteConfigStatus(Storage.inMemory(value));
        }

        public RemoteConfigStatus(Storage<opamp.proto.RemoteConfigStatus> storage) {
            super(storage);
        }

        @Override
        public FieldType getType() {
            return FieldType.REMOTE_CONFIG_STATUS;
        }
    }

    public static final class Flags extends State<Integer> {
        public static Flags createInMemory(int value) {
            return new Flags(Storage.inMemory(value));
        }

        public Flags(Storage<Integer> storage) {
            super(storage);
        }

        @Override
        public FieldType getType() {
            return FieldType.FLAGS;
        }
    }
}
