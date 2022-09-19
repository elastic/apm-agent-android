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
package co.elastic.apm.android.sdk.traces.http.impl.okhttp.compose;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import co.elastic.apm.android.common.MethodCaller;
import okhttp3.EventListener;

public class CompositeEventListener extends EventListener implements MethodCaller {
    private final List<EventListener> listeners;

    public CompositeEventListener(List<EventListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void doCall(@NonNull Method method, @NonNull Object[] params) {
        for (EventListener listener : listeners) {
            try {
                method.invoke(listener, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
