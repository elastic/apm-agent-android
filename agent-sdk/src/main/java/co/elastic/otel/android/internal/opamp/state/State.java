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

import java.util.Objects;
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
        public InstanceUid(byte[] initialValue) {
            super(initialValue);
        }

        @Override
        public Field getFieldType() {
            return Field.INSTANCE_UID;
        }
    }

    final class SequenceNum extends InMemoryState<Integer> {
        public SequenceNum(Integer initialValue) {
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
        public AgentDescription(opamp.proto.AgentDescription initialValue) {
            super(initialValue);
        }

        @Override
        public Field getFieldType() {
            return Field.AGENT_DESCRIPTION;
        }
    }

    final class Capabilities extends InMemoryState<Long> {
        public Capabilities(Long initialValue) {
            super(initialValue);
        }

        @Override
        public Field getFieldType() {
            return Field.CAPABILITIES;
        }
    }

    final class RemoteConfigStatus extends InMemoryState<opamp.proto.RemoteConfigStatus> {

        public RemoteConfigStatus(opamp.proto.RemoteConfigStatus initialValue) {
            super(initialValue);
        }

        @Override
        public Field getFieldType() {
            return Field.REMOTE_CONFIG_STATUS;
        }
    }

    final class Flags extends InMemoryState<Integer> {

        public Flags(Integer initialValue) {
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
