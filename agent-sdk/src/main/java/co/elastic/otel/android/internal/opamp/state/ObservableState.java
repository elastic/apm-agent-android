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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import co.elastic.otel.android.internal.opamp.request.Field;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public abstract class ObservableState<T> implements State<T> {
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
                listener.onStateUpdate(getFieldType());
            }
        }
    }

    public interface Listener {
        void onStateUpdate(Field type);
    }
}
