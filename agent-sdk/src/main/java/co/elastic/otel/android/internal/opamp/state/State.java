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

import java.util.function.Supplier;

import co.elastic.otel.android.internal.opamp.state.observer.Observable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 * </b>
 * Provides a request field value in its {@link #get()} method, and it also notifies the OpAMP
 * client when a new value is available by calling its own {@link #notifyObservers()} method.
 *
 * @param <T> The type of value it provides.
 */
public abstract class State<T> extends Observable implements Supplier<T> {
    public static <T> State<T> createInMemory(T initialValue) {
        return new InMemoryState<>(initialValue);
    }
}
