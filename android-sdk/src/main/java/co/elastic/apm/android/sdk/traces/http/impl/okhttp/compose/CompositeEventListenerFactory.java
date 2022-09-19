/* 
Licensed to Elasticsearch B.V. under one or more contributor
license agreements. See the NOTICE file distributed with
this work for additional information regarding copyright
ownership. Elasticsearch B.V. licenses this file to you under
the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License. 
*/
package co.elastic.apm.android.sdk.traces.http.impl.okhttp.compose;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.EventListener;

public class CompositeEventListenerFactory implements EventListener.Factory {
    private final List<EventListener.Factory> factories;

    public CompositeEventListenerFactory(EventListener.Factory... factories) {
        this.factories = Arrays.asList(factories);
    }

    @NonNull
    @Override
    public EventListener create(@NonNull Call call) {
        List<EventListener> listeners = new ArrayList<>();

        for (EventListener.Factory factory : factories) {
            listeners.add(factory.create(call));
        }

        return new CompositeEventListener(listeners);
    }
}
