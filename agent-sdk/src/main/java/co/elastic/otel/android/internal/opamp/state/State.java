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
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import co.elastic.otel.android.internal.opamp.request.Field;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public interface State<T> extends Supplier<T> {

    Field getFieldType();

    @Nonnull
    default T mustGet() {
        return Objects.requireNonNull(get());
    }

    final class InstanceUid extends InMemoryState<byte[]> {
        public static InstanceUid createRandom() {
            UUID uuid = UuidCreator.getTimeOrderedEpoch();
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(uuid.getMostSignificantBits());
            buffer.putLong(uuid.getLeastSignificantBits());
            return create(buffer.array());
        }

        public static InstanceUid create(byte[] value) {
            return new InstanceUid(value);
        }

        private InstanceUid(byte[] initialValue) {
            super(initialValue);
        }

        @Override
        public Field getFieldType() {
            return Field.INSTANCE_UID;
        }
    }

    final class SequenceNum extends InMemoryState<Integer> {
        public static SequenceNum create(int value) {
            return new SequenceNum(value);
        }

        private SequenceNum(Integer initialValue) {
            super(initialValue);
        }

        public void increment() {
            set(mustGet() + 1);
        }

        @Override
        public Field getFieldType() {
            return Field.SEQUENCE_NUM;
        }
    }

    final class AgentDescription extends InMemoryState<opamp.proto.AgentDescription> {
        public static AgentDescription create(opamp.proto.AgentDescription value) {
            return new AgentDescription(value);
        }

        private AgentDescription(opamp.proto.AgentDescription initialValue) {
            super(initialValue);
        }

        @Override
        public Field getFieldType() {
            return Field.AGENT_DESCRIPTION;
        }
    }

    final class Capabilities extends InMemoryState<Long> {
        public static Capabilities create(long value) {
            return new Capabilities(value);
        }

        private Capabilities(Long initialValue) {
            super(initialValue);
        }

        public void add(long capabilities) {
            set(mustGet() | capabilities);
        }

        public void remove(long capabilities) {
            set(mustGet() & ~capabilities);
        }

        @Override
        public Field getFieldType() {
            return Field.CAPABILITIES;
        }
    }

    final class RemoteConfigStatus extends InMemoryState<opamp.proto.RemoteConfigStatus> {

        public static RemoteConfigStatus create(opamp.proto.RemoteConfigStatus value) {
            return new RemoteConfigStatus(value);
        }

        private RemoteConfigStatus(opamp.proto.RemoteConfigStatus initialValue) {
            super(initialValue);
        }

        @Override
        public Field getFieldType() {
            return Field.REMOTE_CONFIG_STATUS;
        }
    }

    final class Flags extends InMemoryState<Integer> {

        public static Flags create(int value) {
            return new Flags(value);
        }

        private Flags(Integer initialValue) {
            super(initialValue);
        }

        @Override
        public Field getFieldType() {
            return Field.FLAGS;
        }
    }

    abstract class EffectiveConfig extends ObservableState<opamp.proto.EffectiveConfig> {
        @Override
        public final Field getFieldType() {
            return Field.EFFECTIVE_CONFIG;
        }
    }
}
